package com.safeway.app.ps01.model;

public enum RoleType {
    
    USER_UNAUTHORIZED("Unauthorized user."),
    USER_RIM ("ps01.user.rim"),
    USER_ADMIN("ps01.user.admin");

    private String memberOf;

    private RoleType(String memberOf) {
        this.memberOf = memberOf;
    }

    public String getMemberOf() {
        return this.memberOf;
    }

    public void setMemberOf(String memberOf) {
        this.memberOf = memberOf;
    }

    public static RoleType getDefaultRoleType() {
        return RoleType.USER_UNAUTHORIZED;
    }

    public static RoleType getRoleType (String memberOf) {
        
        RoleType roleType = getDefaultRoleType();

        for (RoleType role : RoleType.values()) {
            if (role.getMemberOf().equals(memberOf)) {
                roleType = role;
            }
        }

        return roleType;
    }
    
}