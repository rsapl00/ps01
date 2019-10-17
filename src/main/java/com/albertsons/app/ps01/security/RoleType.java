package com.albertsons.app.ps01.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum RoleType {

    USER_UNAUTHORIZED("Unauthorized user.", "UNAUTHORIZED"), USER_RIM("ps01.user.rim", "ROLE_RIM"),
    USER_ADMIN("ps01.user.admin", "ROLE_ADMIN");

    private String memberOf;
    private String role;

    private RoleType(String memberOf, String role) {
        this.memberOf = memberOf;
    }

    public String getMemberOf() {
        return this.memberOf;
    }

    public void setMemberOf(String memberOf) {
        this.memberOf = memberOf;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static RoleType getDefaultRoleType() {
        return RoleType.USER_UNAUTHORIZED;
    }

    public static RoleType getRoleType(String memberOf) {

        RoleType roleType = getDefaultRoleType();

        for (RoleType role : RoleType.values()) {
            if (role.getMemberOf().equals(memberOf)) {
                roleType = role;
            }
        }

        return roleType;
    }

    public static List<SimpleGrantedAuthority> getGrantedAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority(RoleType.USER_ADMIN.getRole()),
                new SimpleGrantedAuthority(RoleType.USER_RIM.getRole()));
    }
}