package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.UserResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;
import com.gustavosdaniel.stock_flow_api.domain.mapping.UserMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.User;
import com.gustavosdaniel.stock_flow_api.repository.UserRepository;
import com.gustavosdaniel.stock_flow_api.util.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    UserService userService;

    @Nested
    class findAllUSer {

        @Test
        @DisplayName("Should all user with sucesso")
        void shouldAllUsers(){

            Pageable pageable = Pageable.unpaged();

            UUID userId = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            String keycloakId = "idDokeycloak";
            String keycloakId2 = "segundo id do keycloak";

            String nome = "Gustavo";
            String nome2 = "Daniel";

            User user = new User(keycloakId, nome);
            User user2 = new User(keycloakId2, nome2);

            UserResponse userResponse = new UserResponse(userId, nome, UserRole.EMPLOYEE);
            UserResponse userResponse2 = new UserResponse(userId2, nome2, UserRole.MANAGER);

            when(userRepository.findAllBy(pageable)).thenReturn(Flux.just(user, user2));
            when(userRepository.count()).thenReturn(Mono.just(2L));

            when(userMapper.toUserResponse(user)).thenReturn((userResponse));
            when(userMapper.toUserResponse(user2)).thenReturn(userResponse2);

            Mono<Page<UserResponse>> output = userService.findAllUsers(pageable);

            StepVerifier.create(output)
                    .assertNext(page -> {

                        assertEquals(2, page.getTotalElements(), "O total de elementos deve ser 2");
                        assertEquals(nome, page.getContent().get(0).userName(), "O primeiro usuário deve ser Gustavo");
                        assertEquals(nome2, page.getContent().get(1).userName(), "O primeiro usuário deve ser Gustavo");

                    })
                    .verifyComplete();

        }
    }

    @Nested
    class searchByName{

        @Test
        @DisplayName("Should by name with sucesso")
        void shouldUserByName(){

            Pageable pageable = PageRequest.of(0,10);

            UUID userId = UUID.randomUUID();

            String keycloakId = "chave keycloak user 1";

            String name = "Gustavo";

            User user = new User(keycloakId, name);

            UserResponse userResponse = new UserResponse(userId, name, UserRole.MANAGER);

            when(userRepository.searchByName(name, pageable)).thenReturn(Flux.just(user));
            when(userRepository.countByName(name)).thenReturn(Mono.just(1L));
            when(userMapper.toUserResponse(user)).thenReturn(userResponse);

            Mono<Page<UserResponse>> output = userService.searchUsersByName(name, pageable);

            StepVerifier.create(output)
                    .assertNext(page -> {

                        assertEquals(1, page.getTotalElements(), "A página deve conter no total 1 elemento");
                        assertEquals(1, page.getContent().size(), "A lista de conteúdo deve ter 1 elemento");

                        assertEquals(name, page.getContent().get(0).userName(), "O nome do usuário deve ser GUstavo");
                    })
                    .verifyComplete();
        }
    }

    @Nested
    class promoterUser{

        @Test
        @DisplayName("Should promoter user with sucesso")
        void shouldPromoterUser(){

            UUID userId = UUID.randomUUID();
            String name = "Daniel";
            String keycloak = "keycloakId";
            UserRole newRole = UserRole.MANAGER;

            String adminKeycloak = "keycloakadmin";

            User user = new User(keycloak, name);
            user.setRole(UserRole.EMPLOYEE);

            when(securityUtils.getCurrentUserRole()).thenReturn(Mono.just(UserRole.ADMIN));
            when(securityUtils.getCurrentKeycloakId()).thenReturn(Mono.just(adminKeycloak));

            when(userRepository.findById(userId)).thenReturn(Mono.just(user));
            when(keycloakService.updateUserRoleInKeycloak(keycloak, newRole)).thenReturn(Mono.empty());
            when(userRepository.save(user)).thenReturn(Mono.just(user));

            Mono<Void> output = userService.promoteUser(userId, newRole);

            StepVerifier.create(output).verifyComplete();
            verify(userRepository, times(1)).save(user);

        }
    }

    @Nested
    class disableUser{

        @Test
        @DisplayName("Disablited user with sucesso")
        void disableUser(){

            UUID userId = UUID.randomUUID();
            String name = "Daniel";
            String keycloakTarget = "chave keycloak target";
            User targetUser = new User(keycloakTarget, name);


            UserRole roleAdmin = UserRole.ADMIN;
            String keycloakAdmin = "chave_admin";

            when(securityUtils.getCurrentUserRole()).thenReturn(Mono.just(roleAdmin));
            when(securityUtils.getCurrentKeycloakId()).thenReturn(Mono.just(keycloakAdmin));
            when(userRepository.findById(userId)).thenReturn(Mono.just(targetUser));

            when(userRepository.save(targetUser)).thenReturn(Mono.empty());

            Mono<Void> output = userService.disabledUser(userId);

            StepVerifier.create(output)
                    .verifyComplete();

            verify(userRepository, times(1)).save(targetUser);

        }
    }

    @Nested
    class deleteUser{

        @Test
        @DisplayName("Should delete user with sucesso")
        void deleteUserWithSucesso(){

            UUID targetUser = UUID.randomUUID();
            String keycloakTarget = "keycloak_target";
            String name = "Daniel";
            User userTarget = new User(keycloakTarget, name);

            UserRole roleAdmin = UserRole.ADMIN;
            String keycloakAdmin = "chaveAdmin";

            when(securityUtils.getCurrentUserRole()).thenReturn(Mono.just(roleAdmin));
            when(securityUtils.getCurrentKeycloakId()).thenReturn(Mono.just(keycloakAdmin));
            when(userRepository.findById(targetUser)).thenReturn(Mono.just(userTarget));

            when(userRepository.delete(userTarget)).thenReturn(Mono.empty());

            Mono<Void> output = userService.deleteUser(targetUser);

            StepVerifier.create(output).verifyComplete();
            verify(userRepository, times(1)).delete(userTarget);

        }
    }

}