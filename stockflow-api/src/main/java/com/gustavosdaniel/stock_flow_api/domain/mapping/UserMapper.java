package com.gustavosdaniel.stock_flow_api.domain.mapping;

import com.gustavosdaniel.stock_flow_api.domain.po.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(String keycloak, String userName){

        if (keycloak == null || userName == null) {

            throw new IllegalArgumentException("O nome e o ID do keycloak são obrigatórios");
        }

        return new User(keycloak, userName);
    }
}
