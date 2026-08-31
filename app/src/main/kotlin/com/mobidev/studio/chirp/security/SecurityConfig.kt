package com.mobidev.studio.chirp.security

import com.mobidev.studio.chirp.api.config.JwtAuthFilter
import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

// define project wide endpoints security configuration
@Configuration
class SecurityConfig {

    // function call with every single request to check if that
    // particular request has to be authenticated (attached token, key etc.)
    @Bean
    fun filterChain(
        httpSecurity: HttpSecurity,
        jwtAuthFilter: JwtAuthFilter
    ): SecurityFilterChain {
        return httpSecurity
            // Cross-site request forgery (CSRF), as long as server side session is not a requirement it can be disabled,
            // jwt tokens are mostly proofed to this kind of attack
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    .dispatcherTypeMatchers(
                        DispatcherType.ERROR,
                        DispatcherType.FORWARD
                    )
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }
            // jwtAuthFilter -> works as a bean but right here we put more effort in what order, which filter
            // has to be executed
            .addFilterBefore(
                // call JwtAuthFilter before predefined UsernamePasswordAuthenticationFilter
                jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java
            )
            .exceptionHandling { configurer ->
                configurer.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .build()
    }
}