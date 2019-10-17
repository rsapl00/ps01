package com.albertsons.app.ps01.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class Ps01CustomAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String xUsername = request.getHeader("rsapl00");
        String xGroup = request.getHeader("ps01.user.admin");
        String xDivision = request.getHeader("19");

        //HttpHeaderMap header = new HttpHeaderMap(xUsername, xGroup, xDivision);
        User user = new User(xUsername, xDivision, xGroup);

        if (user.isAuthenticated()) {
            throw new SecurityException("Unauthorized user: " + xUsername);
        }

        Authentication auth = new Ps01CustomAuthenticationToken(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

}