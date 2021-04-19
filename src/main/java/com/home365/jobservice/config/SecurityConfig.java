package com.home365.jobservice.config;


import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {


    //     Submits the KeycloakAuthenticationProvider to the AuthenticationManager
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }


    @Bean
    public KeycloakSpringBootConfigResolver KeycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }


    // Specifies the session authentication strategy
    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/v2/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/swagger-config",
                        "/v3/api-docs",
                        "/swagger-ui.html",
                        "/swagger-ui/index.html?configUrl=/finance/v3/api-docs/swagger-config",
                        "/swagger-ui/index.html#/"
                );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers( "/webjars/**").permitAll()
                .antMatchers( "/v2/**").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/error").permitAll()
                .antMatchers("/error/**").permitAll()
                .antMatchers("/camunda/**").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers("/projects/**").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers("/swagger-ui/index.html?configUrl=/finance/v3/api-docs/swagger-config").permitAll()
                .antMatchers("/**")
                .hasRole("user")
                .anyRequest()
                .permitAll();
    }


}
