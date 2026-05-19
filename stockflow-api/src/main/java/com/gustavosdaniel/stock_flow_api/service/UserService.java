package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.UserResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;
import com.gustavosdaniel.stock_flow_api.domain.mapping.UserMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.User;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import com.gustavosdaniel.stock_flow_api.exception.UnauthorizedException;
import com.gustavosdaniel.stock_flow_api.exception.UserNotFoundException;
import com.gustavosdaniel.stock_flow_api.repository.UserRepository;
import com.gustavosdaniel.stock_flow_api.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final SecurityUtils securityUtils;
    private final KeycloakService keycloakService;

    public UserService(UserRepository userRepository, UserMapper userMapper, SecurityUtils securityUtils, KeycloakService keycloakService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.securityUtils = securityUtils;
        this.keycloakService = keycloakService;
    }

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
                    if (existingUser.getRole() != roleFromToken) {
                        existingUser.setRole(roleFromToken);

                        return userRepository.save(existingUser)
                                .doOnSuccess(saveUSer -> {
                                    assert saveUSer != null;
                                    log.info("Sincronização: Role do usuário {} atualizada para {}",
                                            saveUSer.getUserName(), roleFromToken);
                                });

                    }
                    return Mono.just(existingUser);
                });

    }

    @Transactional(readOnly = true)
    public Mono<Page<UserResponse>> findAllUsers(Pageable pageable) {

        return userRepository.findAllBy(pageable)
                .map(userMapper::toUserResponse)
                .collectList()
                .zipWith(userRepository.count())
                .map(tuple -> (Page<UserResponse>)
                        new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()))
                .doFirst(() -> log.info("Buscando todos os usuários do sistema."))
                .doOnSuccess(page -> {
                    assert page != null;
                    log.info("O total de usuários encontrados foram de {}, usuários.",
                            page.getTotalElements());
                });
    }

    @Transactional(readOnly = true)
    public Mono<Page<UserResponse>> searchUsersByName(String userName, Pageable pageable) {

        return userRepository.searchByName(userName, pageable)
                .map(userMapper::toUserResponse)
                .collectList()
                .zipWith(userRepository.countByName(userName))
                .map(tuple -> (Page<UserResponse>)
                        new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()))
                .doFirst(() -> log.info("Iniciando busca por usuários com o nome: {}", userName))
                .doOnSuccess(page -> {
                    assert page != null;
                    log.info(
                            "Busca concluída, {} usuários encontrados para o nome {}, na pagina {}",
                            page.getNumberOfElements(), userName, pageable.getPageNumber());
                });
    }

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

            return keycloakService
                    .updateUserRoleInKeycloak(targetUser.getKeycloakId(), newRole)
                    .then(Mono.defer(() -> {
                        targetUser.setRole(newRole);
                        return userRepository.save(targetUser);
                    }))
                    .doFirst(() -> log.info("Iniciando o processo para promover o funcionário: {}", targetUser.getUserName()))
                    .doOnSuccess(savedUser -> log.info("Funcionário: {}, promovido com sucesso para o cargo de {}", targetUser.getUserName(), newRole));
        }).then();
    }

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

                    if (targetUser.isActive()) return Mono.empty();
                    targetUser.setActive(true);
                    return userRepository.save(targetUser)
                            .doFirst(() -> log.info("Ativando usuário {}", targetUserId))
                    .doOnNext(saved -> log.info(
                            "Usuário {} ativado com sucesso", saved.getUserName()))
                    .then();
                });


    }


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
            if (!targetUser.isActive()) return Mono.empty();

            targetUser.setActive(false);

            return userRepository.save(targetUser)
                    .doFirst(() -> log.info("Desativando usuário {}", targetUserId))
                    .doOnNext(saved -> log.info(
                            "Usuário {} desativado com sucesso", saved.getUserName()))
                    .then();
        });
    }

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
