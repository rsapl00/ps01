package com.albertsons.app.ps01;

import com.albertsons.app.ps01.security.CustomAccessDeniedHandler;
import com.albertsons.app.ps01.security.MySavedRequestAwareAuthenticationSuccessHandler;
import com.albertsons.app.ps01.security.Ps01CustomAuthenticationFilter;
// import com.albertsons.app.ps01.security.MySavedRequestAwareAuthenticationSuccessHandler;
import com.albertsons.app.ps01.security.Ps01CustomAuthenticationProvider;
import com.albertsons.app.ps01.security.RestAuthenticationEntryPoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@ComponentScan("com.albertsons.app.ps01.security")
public class Ps01SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private Ps01CustomAuthenticationProvider authenticationProvider;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private MySavedRequestAwareAuthenticationSuccessHandler mySuccessHandler;
    
    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf().disable()
            .headers().frameOptions().disable()
            .and()
            .authorizeRequests()
            .requestMatchers(EndpointRequest.to("info")).permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                .antMatchers("/ps01/rest/cyclechanges/approve").hasRole("ADMIN")
                .antMatchers("/ps01/rest/cyclechanges/reject").hasRole("ADMIN")
                .antMatchers("/ps01/actuator/").hasRole("ADMIN")
                // .antMatchers("/ps01/actuator/").permitAll()
                .antMatchers("/ps01/**").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/ps01/h2-console/**").permitAll()
                .anyRequest()
                .authenticated()
            .and()
            .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
            .addFilterBefore(new Ps01CustomAuthenticationFilter(), BasicAuthenticationFilter.class);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }
}