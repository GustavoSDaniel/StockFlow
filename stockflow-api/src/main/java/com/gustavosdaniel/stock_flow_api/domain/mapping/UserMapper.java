package com.gustavosdaniel.stock_flow_api.domain.mapping;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.UserResponse;
import com.gustavosdaniel.stock_flow_api.domain.po.User;
import org.springframework.stereotype.Component;

/**
 * Handles manual mapping between {@link User} persistence objects
 * and their corresponding response DTOs.
 * <p>
 * Provides methods to create a {@link User} from Keycloak identifiers
 * and to convert a persisted user into a {@link UserResponse} DTO.
 * </p>
 */
@Component
public class UserMapper {

    public User toUser(String keycloak, String userName){

        if (keycloak == null || userName == null) {

            throw new IllegalArgumentException("O nome e o ID do keycloak são obrigatórios");
        }

        return new User(keycloak, userName);
    }

    public UserResponse toUserResponse(User user){

        if (user == null) return null;

        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
