package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;
import com.gustavosdaniel.stock_flow_api.domain.mapping.UserMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.User;
import com.gustavosdaniel.stock_flow_api.exception.UnauthorizedException;
import com.gustavosdaniel.stock_flow_api.exception.UserNotFoundException;
import com.gustavosdaniel.stock_flow_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
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

    public Mono<Void> deleteUser(UUID id){

        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(UserNotFoundException::new))
                .flatMap(userRepository::delete);
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
