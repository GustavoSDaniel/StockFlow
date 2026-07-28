package com.gustavosdaniel.stock_flow_api.domain.enums;

/**
 * Defines access roles for system users with hierarchical permission levels.
 * <p>
 * Each role has an integer {@code level} that establishes a hierarchy.
 * The {@link #canManage(UserRole)} method returns {@code true} only when
 * the current role has a strictly higher level than the target role.
 * </p>
 */
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

    public boolean canManage(UserRole target){

        if (target == null) return false;

        return this.level > target.level;
    }
}
