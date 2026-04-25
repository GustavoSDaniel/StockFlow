package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("users")
public class Users extends BaseEntity {

    public Users(){}

    public Users(String keycloakId, String userName, UserRole role) {

        this.id = UUID.randomUUID();
        this.keycloakId = keycloakId;
        this.userName = userName;
        this.role = role;
        this.isNew = true;
    }

    @Id
    private UUID id;

    private String keycloakId;

    private String userName;

    private UserRole role = UserRole.EMPLOYEE;

    @Transient
    private boolean isNew;


    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
