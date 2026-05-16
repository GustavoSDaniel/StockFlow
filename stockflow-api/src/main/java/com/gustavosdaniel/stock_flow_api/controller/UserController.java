package com.gustavosdaniel.stock_flow_api.controller;

import com.gustavosdaniel.stock_flow_api.controller.OpenApi.UserOpenApi;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.UserResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;
import com.gustavosdaniel.stock_flow_api.domain.mapping.UserMapper;
import com.gustavosdaniel.stock_flow_api.service.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserOpenApi {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserResponse>> getUSer(@AuthenticationPrincipal Jwt jwt){

        return userService.getCurrentUser(jwt)
                .map(userMapper::toUserResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<Page<UserResponse>>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 20, sort = "userName", direction = Sort.Direction.ASC)
            Pageable pageable)
    {

        return userService.findAllUsers(pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<Page<UserResponse>>> searchUsers(
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(sort = "userName", direction = Sort.Direction.ASC)
            Pageable pageable){

        return userService.searchUsersByName(name, pageable).map(ResponseEntity::ok);
    }

    @PatchMapping("/{targetUserId}/promote")
    public Mono<ResponseEntity<Void>> promoteUser(
            @PathVariable UUID targetUserId,
            @RequestParam UserRole newRole){

        return userService.promoteUser(targetUserId, newRole)
                .thenReturn(ResponseEntity. noContent().build());
    }

    @PatchMapping("/{targetUserId}/active")
    public Mono<ResponseEntity<Void>> activeUser(@PathVariable UUID targetUserId){

        return userService.activeUser(targetUserId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PatchMapping("/{targetUserId}/disable")
    public Mono<ResponseEntity<Void>> disableUser(
            @PathVariable UUID targetUserId
    ){
        return userService.disabledUser(targetUserId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/{targetUserId}")
    public Mono<ResponseEntity<Void>> deleteUser(
            @PathVariable UUID targetUserId)
    {
        return userService.deleteUser(targetUserId)
                .thenReturn(ResponseEntity.noContent().build());
    }

}
