package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;

import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("users")
public class Users extends BaseEntity {

    private String keycloakId;

    private String userName;

    private UserRole role = UserRole.EMPLOYEE;


}
