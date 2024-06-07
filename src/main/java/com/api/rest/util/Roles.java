package com.api.rest.util;

/**
 * Role class, contains the roles associated with the context
 */
public enum Roles {
    USER("user"),
    ADMIN("admin"),
    DEVELOPER("developer"),
    SUPERVISOR("supervisor");

    private final String roleName;

    Roles(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    /**
     * asks if the role matches any of the context roles
     *
     * @param roleName
     * @return true/false if there are match
     */
    public static boolean isAllowed(String roleName) {
        for (Roles role : values()) {
            if (role.getRoleName().equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }
}
