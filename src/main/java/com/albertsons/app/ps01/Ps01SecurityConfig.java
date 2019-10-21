package com.albertsons.app.ps01;

import com.albertsons.app.ps01.exception.CustomAccessDeniedHandler;
import com.albertsons.app.ps01.security.Ps01CustomAuthenticationFilter;
import com.albertsons.app.ps01.security.Ps01CustomAuthenticationProvider;
import com.albertsons.app.ps01.security.RestAuthenticationEntryPoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
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

    // @Autowired
    // private MySavedRequestAwareAuthenticationSuccessHandler mySuccessHandler;
    
    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        
        http
            .csrf().disable()
            .headers().frameOptions().disable()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(restAuthenticationEntryPoint)
            .and()
            // .authenticationProvider(authenticationProvider)
            .authorizeRequests()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAnyRole("ADMIN")
                .antMatchers("/rest/cyclechanges/approve").hasRole("ADMIN")
                .antMatchers("/rest/cyclechanges/reject").hasRole("ADMIN")
                .antMatchers("/rest/**").hasAnyRole("ADMIN", "RIM")
                .antMatchers("/home").hasAnyRole("ADMIN", "RIM")
                .anyRequest().authenticated()
            .and()
                .addFilterBefore(new Ps01CustomAuthenticationFilter(), BasicAuthenticationFilter.class)
            .formLogin().disable()
            .httpBasic().disable()
            .logout()
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID");
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
            .requestMatchers(EndpointRequest.to("info"))
            .antMatchers("/h2-console/**")
            .antMatchers("/version.jsp");
	}
}