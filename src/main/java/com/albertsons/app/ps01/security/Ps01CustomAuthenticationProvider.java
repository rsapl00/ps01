package com.albertsons.app.ps01.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class Ps01CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        Ps01CustomAuthenticationToken token = (Ps01CustomAuthenticationToken) authentication;
        if (!token.isAuthenticated()) {
            throw new AccessDeniedException("Access Denied.");
        }

        return (User) token.getPrincipal();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return Ps01CustomAuthenticationToken.class.isAssignableFrom(authentication);
    }

}