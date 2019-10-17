package com.albertsons.app.ps01.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class Ps01CustomAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -2559357568133352085L;
    private User authenticatedUser;

    @Override
    public Object getCredentials() {
        return authenticatedUser.getUsername();
    }

    /**
     * @return {@link User}
     */
    @Override
    public Object getPrincipal() {
        return authenticatedUser;
    }

    public Ps01CustomAuthenticationToken(Collection<? extends GrantedAuthority> authorities, User authenticatedUser) {
        super(authorities);
        this.authenticatedUser = authenticatedUser;
        setAuthenticated(authenticatedUser.isAuthenticated());
    }

}
