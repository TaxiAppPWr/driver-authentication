package com.taxiapp.driver_auth.configuration

import com.taxiapp.driver_auth.filters.JwtFilter
import jakarta.servlet.Filter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain


@Configuration
open class SecurityConfig {

    @Bean
    open fun jwtAuthFilterRegistration(jwtAuthFilter: JwtFilter): FilterRegistrationBean<Filter> {
        val registration = FilterRegistrationBean<Filter>()
        registration.filter = jwtAuthFilter
        registration.addUrlPatterns("*")
        registration.order = 1
        return registration
    }

    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { authz ->
                authz.anyRequest().permitAll()
            }
        return http.build()
    }
}