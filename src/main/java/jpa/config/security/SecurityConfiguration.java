package jpa.config.security;

import jpa.config.security.exception.CustomAccessDeniedHandler;
import jpa.config.security.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtTokenProvider jwtTokenProvider;

    private static final String[] PERMIT_URI_ARRAY = {
            "/sign-api/sign-in",
            "/sign-api/sign-up",
            "/sign-api/refresh",
            "/sign-api/exception",
            "/v3/api-docs/**"
    };

    private static final String[] SWAGGER_URL_ARRAY = {
            "/v3/api-docs",
            "/swagger-resources/**",
            "/webjars/**",
            "/swagger/**",
            "/sign-api/exception",
            "/swagger-ui/**",
            "/apple-touch-icon-precomposed.png",
            "/favicon.ico",
            "/apple-touch-icon.png"
    };

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .headers().frameOptions().disable()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers(PERMIT_URI_ARRAY).permitAll()
                //.antMatchers("/admin/**").hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/product/**").permitAll()
                .antMatchers("**exception**").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler())
                .and()
                .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .csrf().disable();
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain exceptionSecurityFilter(HttpSecurity http) throws Exception {
        return http
                .requestMatchers()
                .antMatchers(SWAGGER_URL_ARRAY)
                .and()
                .authorizeRequests()
                .antMatchers(SWAGGER_URL_ARRAY).permitAll()
                .and()
                .build();
    }
}
