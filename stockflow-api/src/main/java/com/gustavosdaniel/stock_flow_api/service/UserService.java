package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.UserResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;
import com.gustavosdaniel.stock_flow_api.domain.mapping.UserMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.User;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import com.gustavosdaniel.stock_flow_api.exception.UnauthorizedException;
import com.gustavosdaniel.stock_flow_api.exception.UserNotFoundException;
import com.gustavosdaniel.stock_flow_api.repository.UserRepository;
import com.gustavosdaniel.stock_flow_api.util.PageUtils;
import com.gustavosdaniel.stock_flow_api.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Service for user identity management: JWT-based user resolution/sync, role promotion,
 * activation/deactivation, deletion, and search. Delegates Keycloak role updates on promotion.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final SecurityUtils securityUtils;
    private final KeycloakService keycloakService;

    public UserService(UserRepository userRepository, UserMapper userMapper, SecurityUtils securityUtils, KeycloakService keycloakService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.securityUtils = securityUtils;
        this.keycloakService = keycloakService;
    }

    /**
     * Resolves the current user from a JWT. If the user does not exist locally, a new record
     * is created. If the role differs from the token, the local role is synchronized.
     *
     * @param jwt the JWT from the request context
     * @return a Mono emitting the resolved local user entity
     * @throws UnauthorizedException if the JWT is null
     */
    @Transactional
    public Mono<User> getCurrentUser(Jwt jwt) {

        validateJwt(jwt);

        String keycloakId = jwt.getSubject();

        UserRole roleFromToken = extractHighestRoleFromJwt(jwt);

        return userRepository
                .findByKeycloakId(keycloakId)
                .switchIfEmpty(Mono.defer(() -> createUSer(jwt, roleFromToken)))
                .doOnNext(user -> log.info("User {} encontrado com sucesso", user.getUserName()))
                .flatMap(existingUser -> {
                    if (!existingUser.getRole().equals(roleFromToken)) {
                        existingUser.setRole(roleFromToken);

                        return userRepository.save(existingUser)
                                .doOnSuccess(saveUser -> {
                                    log.info("Sincronização: Role do usuário {} atualizada para {}",
                                            saveUser.getUserName(), roleFromToken);
                                });

                    }
                    return Mono.just(existingUser);
                });

    }

    /**
     * Retrieves a paginated list of all users.
     *
     * @param pageable pagination information
     * @return a Mono emitting a page of user responses
     */
    @Transactional(readOnly = true)
    public Mono<Page<UserResponse>> findAllUsers(Pageable pageable) {

        return PageUtils.toPage(

                        userRepository.findAllBy(pageable),
                        userRepository.count(),
                        userMapper::toUserResponse,
                        pageable
        )
                .doFirst(() -> log.info("Buscando todos os usuários do sistema."))
                .doOnSuccess(page -> {
                    log.info("O total de usuários encontrados foram de {}, usuários.",
                            page.getTotalElements());
                });
    }

    /**
     * Searches users by username (case-insensitive partial match).
     *
     * @param userName the search term
     * @param pageable pagination information
     * @return a Mono emitting a page of matching user responses
     */
    @Transactional(readOnly = true)
    public Mono<Page<UserResponse>> searchUsersByName(String userName, Pageable pageable) {

        return PageUtils.toPage(

                        userRepository.searchByName(userName, pageable),
                        userRepository.countByName(userName),
                        userMapper::toUserResponse,
                        pageable
        )
                .doFirst(() -> log.info("Iniciando busca por usuários com o nome: {}", userName))
                .doOnSuccess(page -> {
                    log.info(
                            "Busca concluída, {} usuários encontrados para o nome {}, na pagina {}",
                            page.getNumberOfElements(), userName, pageable.getPageNumber());
                });
    }

    /**
     * Promotes a target user to a new role. The current user must have sufficient privileges
     * to manage the target's role. Also updates the role in Keycloak; reverts on failure.
     *
     * @param targetUserId the ID of the user to promote
     * @param newRole      the new role to assign
     * @return a Mono that completes when the promotion is persisted locally and in Keycloak
     * @throws UserNotFoundException if the target user does not exist
     * @throws BusinessRuleException if the current user tries to modify their own account
     * @throws AccessDeniedException if the current user's role level is insufficient
     */
    @Transactional
    public Mono<Void> promoteUser(UUID targetUserId, UserRole newRole) {
        return Mono.zip(
                securityUtils.getCurrentUserRole(),
                securityUtils.getCurrentKeycloakId(),
                userRepository.findById(targetUserId)
                        .switchIfEmpty(Mono.error(new UserNotFoundException()))
        ).flatMap(tuple -> {
            UserRole currentRole = tuple.getT1();
            String currentKcId = tuple.getT2();
            User targetUser = tuple.getT3();

            validateKeycloakId(targetUser.getKeycloakId(), currentKcId);
            validateRole(targetUser.getRole(), currentRole);
            validateRole(newRole, currentRole);

            UserRole originalRole = targetUser.getRole();

            targetUser.setRole(newRole);

            return userRepository.save(targetUser)
                    .flatMap(savedUser ->
                        keycloakService.updateUserRoleInKeycloak(
                                targetUser.getKeycloakId(), newRole)
                            .onErrorResume(e -> {
                                log.error("Falha ao atualizar Keycloak. Revertendo role no DB...", e);
                                savedUser.setRole(originalRole);
                                return userRepository.save(savedUser)
                                    .then(Mono.error(new RuntimeException(
                                        "Falha ao sincronizar com Keycloak. " +
                                        "Alteração de cargo revertida.", e)));
                            })
                    )
                    .doFirst(() -> log.info("Iniciando o processo para promover o funcionário: {}",
                            targetUser.getUserName()))
                    .doOnSuccess(v -> log.info("Funcionário: {}, promovido com sucesso para o cargo de {}",
                            targetUser.getUserName(), newRole));
        }).then();
    }

    /**
     * Activates a previously deactivated user. The current user must have sufficient privileges
     * and cannot activate their own account.
     *
     * @param targetUserId the ID of the user to activate
     * @return a Mono that completes when the activation is persisted
     * @throws UserNotFoundException if the target user does not exist
     * @throws BusinessRuleException if the user is already active or if the current user
     *                               tries to activate their own account
     * @throws AccessDeniedException if the current user's role level is insufficient
     */
    @Transactional
    public Mono<Void> activeUser(UUID targetUserId){

        return Mono.zip(
                securityUtils.getCurrentUserRole(),
                securityUtils.getCurrentKeycloakId(),
                userRepository.findById(targetUserId)
                        .switchIfEmpty(Mono.error(UserNotFoundException::new))

        ).flatMap( tuple -> {
                    UserRole currentRole = tuple.getT1();
                    String currentKeycloakId = tuple.getT2();
                    User targetUser = tuple.getT3();

                    validateKeycloakId(targetUser.getKeycloakId(), currentKeycloakId);
                    validateRole(targetUser.getRole(), currentRole);

                    if (targetUser.isActive()) return Mono.error(
                            new BusinessRuleException("O usuário já se encontra ativo"));
                    targetUser.setActive(true);
                    return userRepository.save(targetUser)
                            .doFirst(() -> log.info("Ativando usuário {}", targetUserId))
                    .doOnSuccess(saved -> log.info(
                            "Usuário {} ativado com sucesso", saved.getUserName()))
                    .then();
                });
    }


    /**
     * Deactivates a user. The current user must have sufficient privileges
     * and cannot deactivate their own account.
     *
     * @param targetUserId the ID of the user to deactivate
     * @return a Mono that completes when the deactivation is persisted
     * @throws UserNotFoundException if the target user does not exist
     * @throws BusinessRuleException if the user is already inactive or if the current user
     *                               tries to deactivate their own account
     * @throws AccessDeniedException if the current user's role level is insufficient
     */
    @Transactional
    public Mono<Void> disabledUser(UUID targetUserId) {

        return Mono.zip(
                securityUtils.getCurrentUserRole(),
                securityUtils.getCurrentKeycloakId(),
                userRepository.findById(targetUserId)
                        .switchIfEmpty(Mono.error(UserNotFoundException::new))

        ).flatMap(tuple -> {

            UserRole currentRole = tuple.getT1();
            String currentKeycloakId = tuple.getT2();
            User targetUser = tuple.getT3();

            validateKeycloakId(targetUser.getKeycloakId(), currentKeycloakId);
            validateRole(targetUser.getRole(), currentRole);
            if (!targetUser.isActive()) return Mono.error(
                    new BusinessRuleException("O usuário já se encontra desativado"));

            targetUser.setActive(false);

            return userRepository.save(targetUser)
                    .doFirst(() -> log.info("Desativando usuário {}", targetUserId))
                    .doOnSuccess(saved -> log.info(
                            "Usuário {} desativado com sucesso", saved.getUserName()))
                    .then();
        });
    }

    /**
     * Permanently deletes a user. The current user must have sufficient privileges
     * and cannot delete their own account.
     *
     * @param targetUserId the ID of the user to delete
     * @return a Mono that completes when the deletion is persisted
     * @throws UserNotFoundException if the target user does not exist
     * @throws BusinessRuleException if the current user tries to delete their own account
     * @throws AccessDeniedException if the current user's role level is insufficient
     */
    @Transactional
    public Mono<Void> deleteUser(UUID targetUserId) {

        log.warn("Iniciando processo para deletar o usuário do ID: {}", targetUserId);

        return Mono.zip(
                securityUtils.getCurrentUserRole(),
                securityUtils.getCurrentKeycloakId(),
                userRepository.findById(targetUserId)
                        .switchIfEmpty(Mono.error(UserNotFoundException::new))

        ).flatMap(tuple -> {

            UserRole currentRole = tuple.getT1();
            String currentKeycloakId = tuple.getT2();
            User targetUser = tuple.getT3();

            validateKeycloakId(targetUser.getKeycloakId(), currentKeycloakId);
            validateRole(targetUser.getRole(), currentRole);

            return userRepository.delete(targetUser)
                    .doOnSuccess(v -> log.info(
                            "Usuário {} deletado com sucesso", targetUser.getUserName()));
        });
    }


    private Mono<User> createUSer(Jwt jwt, UserRole role) {

        String keycloakId = jwt.getSubject();
        String userName = jwt.getClaimAsString("preferred_username");

        User newUser = userMapper.toUser(keycloakId, userName);

        newUser.setRole(role);

        return userRepository.save(newUser)
                .doOnSuccess(saved -> log.info(
                        "Usuário {} criado com sucesso", saved.getUserName()));
    }

    private UserRole extractHighestRoleFromJwt(Jwt jwt) {

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

        if (realmAccess == null || !realmAccess.containsKey("roles"))
            return UserRole.EMPLOYEE;

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");

        return roles.stream()
                .map(r -> {
                    try {
                        return UserRole.valueOf(r.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(UserRole::getLevel))
                .orElse(UserRole.EMPLOYEE);
    }

    private void validateJwt(Jwt jwt) {
        if (jwt == null) throw new UnauthorizedException("Token JWT ausente.");

    }

    private void validateKeycloakId(String targetKeycloakId, String currentKeycloakId) {

        if (targetKeycloakId.equals(currentKeycloakId))
            throw new BusinessRuleException("Você não pode realizar essa ação a sua própria conta.");


    }

    private void validateRole(UserRole targetRole, UserRole currentRole) {
        if (!currentRole.canManage(targetRole)) {
            throw new AccessDeniedException(
                    "Acesso negado: Seu nível não permite realizar essa ação " +
                            "a um usuário de nível " + targetRole.name()
            );
        }
    }
}
