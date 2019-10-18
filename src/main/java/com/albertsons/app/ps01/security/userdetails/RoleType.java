package com.albertsons.app.ps01.security.userdetails;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum RoleType {

    USER_ANONYMOUS("Anonymous user.", "ROLE_ANONYMOUS"), USER_RIM("ps01.user.rim", "ROLE_RIM"),
    USER_ADMIN("ps01.user.admin", "ROLE_ADMIN");

    private String memberOf;
    private String role;

    private RoleType(String memberOf, String role) {
        this.memberOf = memberOf;
        this.role = role;
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
        return RoleType.USER_ANONYMOUS;
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
                // new SimpleGrantedAuthority(RoleType.USER_ANONYMOUS.getRole()));
    }

    public static List<SimpleGrantedAuthority> getGrantedAuthorities(String... groups) {
        return Arrays.asList(groups).stream().map(group -> {
            return new SimpleGrantedAuthority(RoleType.getRoleType(group).getRole());
        }).collect(Collectors.toList());
    }

    public static List<SimpleGrantedAuthority> getGrantedAuthorities(List<RoleType> groups) {
        return groups.stream().map(group -> {
            return new SimpleGrantedAuthority(group.getRole());
        }).collect(Collectors.toList());
    }
}