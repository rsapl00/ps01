package com.albertsons.app.ps01.security.userdetails;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import lombok.Data;
import lombok.NonNull;

@Data
public class User implements Authentication {

    private static final String EMAIL_DOMAIN = "@safeway.com";

    /**
     *
     */
    private static final long serialVersionUID = 7358339418644588868L;

    public User() {
    }

    public User(String username, String division, String group) {
        
        this.username = username;
        this.email = username + EMAIL_DOMAIN;
        this.division = division;
        
        if (username == null || "".equals(username) || division == null || "".equals(division)) {
            this.role = RoleType.USER_ANONYMOUS;
        } else {
            this.role = RoleType.getRoleType(group);
        }

    }

    @NonNull
    private String username;

    @NonNull
    private String email;

    @NonNull
    private String division;

    @NonNull
    private RoleType role;

    @Override
    public String getName() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return RoleType.getGrantedAuthorities(Arrays.asList(role));
    }

    /**
     * @return {@link User}
     */
    @Override
    public Object getCredentials() {
        return this;
    }

    /**
     * @return the user's email address
     */
    @Override
    public Object getDetails() {
        return this.email;
    }

    /**
     * @return {@link User}
     */
    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return isUserValid();
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    public Boolean isUserValid() {
        if (username == null || "".equals(username) || division == null || "".equals(division)
                || RoleType.USER_ANONYMOUS == role) {
            return false;
        }

        return true;
    }
}