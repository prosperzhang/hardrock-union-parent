package com.hardrockunion.framework.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.hardrockunion.framework.security.service.JwtClaimsService;
import com.hardrockunion.framework.security.web.JwtAuthenticationInterceptor;
import com.hardrockunion.framework.security.web.LoginUserArgumentResolver;

@Configuration
public class SecurityWebMvcConfigurer implements WebMvcConfigurer {

    private final JwtClaimsService jwtClaimsService;

    public SecurityWebMvcConfigurer(JwtClaimsService jwtClaimsService) {
        this.jwtClaimsService = jwtClaimsService;
    }

    @Bean
    public JwtAuthenticationInterceptor jwtAuthenticationInterceptor() {
        return new JwtAuthenticationInterceptor(jwtClaimsService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor())
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/health", "/api/iam/auth/login");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns(
                "http://localhost:*",
                "http://127.0.0.1:*"
            )
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Authorization")
            .allowCredentials(false)
            .maxAge(3600);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginUserArgumentResolver());
    }
}
