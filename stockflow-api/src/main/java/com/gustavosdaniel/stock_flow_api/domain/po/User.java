package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("users")
public class User extends BaseEntity {

    public User(){}

    public User(String keycloakId, String userName) {
        this.keycloakId = keycloakId;
        this.userName = userName;

    }

    @Column("keycloak_id")
    private String keycloakId;

    @Column("user_name")
    private String userName;


    public String getKeycloakId() {
        return keycloakId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


}
