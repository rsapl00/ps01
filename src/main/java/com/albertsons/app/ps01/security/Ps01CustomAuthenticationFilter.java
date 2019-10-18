package com.albertsons.app.ps01.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.albertsons.app.ps01.security.userdetails.RoleType;
import com.albertsons.app.ps01.security.userdetails.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class Ps01CustomAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String xUsername = request.getHeader("SWY_API_USER");
        String xGroup = request.getHeader("SWY_GROUP");
        String xDivision = request.getHeader("SWY_DIV");

        User user = new User(xUsername, xDivision, xGroup);

        Authentication auth = new Ps01CustomAuthenticationToken(RoleType.getGrantedAuthorities(xGroup), user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

}