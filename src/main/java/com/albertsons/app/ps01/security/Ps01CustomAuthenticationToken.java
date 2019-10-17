package com.albertsons.app.ps01.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class Ps01CustomAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -2559357568133352085L;
    private User authenticatedUser;

    public Ps01CustomAuthenticationToken(User authenticatedUser) {
        super(RoleType.getGrantedAuthorities());
        this.authenticatedUser = authenticatedUser;
    }

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

}
