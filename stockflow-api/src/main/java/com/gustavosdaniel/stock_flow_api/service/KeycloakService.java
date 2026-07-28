package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;

/**
 * Service for communicating with the Keycloak Admin API to manage user realm roles.
 */
@Service
public class KeycloakService {

    private final Keycloak keycloakAdmin;
    private final String realm;
    private final Logger log = LoggerFactory.getLogger(KeycloakService.class);


    public KeycloakService(Keycloak keycloakAdmin, @Value("${keycloak.realm}") String realm) {
        this.keycloakAdmin = keycloakAdmin;
        this.realm = realm;
    }

    /**
     * Replaces the current realm roles of a Keycloak user with the given role.
     * Existing roles that match a {@link UserRole} enum value are removed first.
     *
     * @param keycloakUserId the Keycloak user ID
     * @param newRole        the role to assign
     * @return a Mono that completes when the role update is persisted in Keycloak
     */
    public Mono<Void> updateUserRoleInKeycloak(String keycloakUserId, UserRole newRole){

        return Mono.fromRunnable(() -> {
            log.info("Iniciando comunicação com Keycloak para atualizar role do usuário {}",
                    keycloakUserId);

            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);


            List<RoleRepresentation> currentRoles = userResource.roles().realmLevel().listAll();

            List<RoleRepresentation> rolesToRemove = currentRoles.stream()
                    .filter(role -> {
                        try {
                            UserRole.valueOf(role.getName().toUpperCase());
                            return true;
                        }catch (IllegalArgumentException e){
                            return false;
                        }
                    })
                    .toList();

            if (!rolesToRemove.isEmpty()){
                userResource.roles().realmLevel().remove(rolesToRemove);

                log.info("Roles antigas removidas do usuário {}: {}",
                        keycloakUserId,
                        rolesToRemove.stream().map(RoleRepresentation::getName).toList());
            }


            RoleRepresentation roleToAdd = realmResource.roles().list().stream()
                    .filter(r -> r.getName().equalsIgnoreCase(newRole.name()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Role '" + newRole.name() + "' não encontrada no realm Keycloak. "
                                    + "Certifique-se de que a role existe no painel do Keycloak (Realm Roles)."));

            userResource.roles().realmLevel().add(Collections.singletonList(roleToAdd));

            log.info("Role {} adicionada com sucesso no Keycloak para o usuário {}",
                    newRole.name(), keycloakUserId);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
