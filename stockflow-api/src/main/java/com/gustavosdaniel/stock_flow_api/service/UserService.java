package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.UserResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;
import com.gustavosdaniel.stock_flow_api.domain.mapping.UserMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.User;
import com.gustavosdaniel.stock_flow_api.exception.UnauthorizedException;
import com.gustavosdaniel.stock_flow_api.exception.UserNotFoundException;
import com.gustavosdaniel.stock_flow_api.repository.UserRepository;
import com.gustavosdaniel.stock_flow_api.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final SecurityUtils securityUtils;

    public UserService(UserRepository userRepository, UserMapper userMapper, SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public Mono<User> getCurrentUser(Jwt jwt){

        if (jwt == null){

            throw new UnauthorizedException();
        }

        String keycloakId = jwt.getSubject();

        UserRole roleFromToken = extractHighestRoleFromJwt(jwt);

        return userRepository
                .findByKeycloakId(keycloakId)
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
                })
                .switchIfEmpty(Mono.defer(() ->createUSer(jwt, roleFromToken)));
    }

    public Mono<Page<UserResponse>> findAllUsers(Pageable pageable){

        return userRepository.findAllBy(pageable)
                .map(userMapper::toUserResponse)
                .collectList()
                .zipWith(userRepository.count())
                .map(tuple -> {
                    return new PageImpl<>(tuple.getT1(), pageable, tuple.getT2());
                });
    }

    public Mono<Page<UserResponse>> searchUsersByName(String userName, Pageable pageable){

        return userRepository.searchByName(userName, pageable)
                .map(userMapper::toUserResponse)
                .collectList()
                .zipWith(userRepository.countByName(userName))
                .map(tuple ->
                        new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Transactional
    public Mono<Void> disabledUser (UUID targetUserId){

        return Mono.zip(
                securityUtils.getCurrentUserRole(),
                securityUtils.getCurrentKeycloakId(),
                userRepository.findById(targetUserId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Usuário alvo não encontrado.")))

        ).flatMap(tuple -> {

            UserRole currentRole = tuple.getT1();
            String currentKeycloakId = tuple.getT2();
            User targetUser = tuple.getT3();

            if (targetUser.getKeycloakId().equals(currentKeycloakId)){
                return Mono.error(new IllegalArgumentException("Você não pode deletar sua própria conta."));

            }

            if (!currentRole.canManager(targetUser.getRole())){
                return Mono.error(new AccessDeniedException(
                        String.format("Acesso negado: Seu nível (%s) não permite deletar um usuário de nível (%s)",
                                currentRole.name(), targetUser.getRole().name())
                ));
            }
            log.info("Usuário {} desativado pelo usuário {}", currentKeycloakId, targetUser.getId());
            targetUser.setActive(false);
            return userRepository.save(targetUser).then();
        });
    }

    @Transactional
    public Mono<Void> deleteUser(UUID targetUserId){

        return Mono.zip(
                securityUtils.getCurrentUserRole(),
                securityUtils.getCurrentKeycloakId(),
                userRepository.findById(targetUserId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Usuário alvo não encontrado.")))

        ).flatMap(tuple -> {

            UserRole currentRole = tuple.getT1();
            String currentKeycloakId = tuple.getT2();
            User targetUser = tuple.getT3();

            if (targetUser.getKeycloakId().equals(currentKeycloakId)){
                return Mono.error(new IllegalArgumentException("Você não pode deletar sua própria conta."));

            }

            if (!currentRole.canManager(targetUser.getRole())){
                return Mono.error(new AccessDeniedException(
                        String.format("Acesso negado: Seu nível (%s) não permite deletar um usuário de nível (%s)",
                                currentRole.name(), targetUser.getRole().name())
                ));
            }
            log.info("Usuário {} deletando o usuário {}", currentKeycloakId, targetUser.getId());

            return userRepository.delete(targetUser);
        });
    }


    private Mono<User> createUSer(Jwt jwt, UserRole role){

        String keycloakId = jwt.getSubject();
        String userName = jwt.getClaimAsString("name");

        User newUser = userMapper.toUser(keycloakId, userName);

        newUser.setRole(role);

        return userRepository.save(newUser)
                .doOnSuccess(saveUSer -> {
                    assert saveUSer != null;
                    log.info("Usuário {}, criado com sucesso",
                            saveUSer.getKeycloakId());
                });
    }

    private UserRole extractHighestRoleFromJwt(Jwt jwt){

        List<String> roles = jwt.getClaimAsStringList("roles");

        if (roles == null || roles.isEmpty()){
            return UserRole.EMPLOYEE;
        }

        return roles.stream()
                .filter(r -> r.startsWith("ROLE_"))
                .map(roleStr -> {
                    try {
                        return UserRole.valueOf(roleStr);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .max(java.util.Comparator.comparingInt(UserRole::getLevel))
                .orElse(UserRole.EMPLOYEE);
    }
}
