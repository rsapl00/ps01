package com.albertsons.app.ps01.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class HttpHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String xUsername = request.getHeader("username");
        String xGroup = request.getHeader("");
        String xDivision = request.getHeader("");

        if (isValid()) {

        }

        
    }

    private Boolean isValid() {
        return false;
    }

}