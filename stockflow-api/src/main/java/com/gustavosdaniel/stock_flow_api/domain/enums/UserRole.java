package com.gustavosdaniel.stock_flow_api.domain.enums;

public enum UserRole {

    EMPLOYEE(1),
    MANAGER(2),
    ADMIN(3);

    private final int level;

    UserRole(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean canManager(UserRole target){

        if (target == null) return false;

        return this.level > target.level;
    }
}
