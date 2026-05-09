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

@Service
public class KeycloakService {

    private final Keycloak keycloakAdmin;
    private final String realm;
    private final Logger log = LoggerFactory.getLogger(KeycloakService.class);


    public KeycloakService(Keycloak keycloakAdmin, @Value("${keycloak.realm}") String realm) {
        this.keycloakAdmin = keycloakAdmin;
        this.realm = realm;
    }

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


            RoleRepresentation roleToAdd = realmResource.roles()
                    .get(newRole.name())
                    .toRepresentation();

            userResource.roles().realmLevel().add(Collections.singletonList(roleToAdd));

            log.info("Role {} adicionada com sucesso no Keycloak para o usuário {}",
                    newRole.name(), keycloakUserId);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
