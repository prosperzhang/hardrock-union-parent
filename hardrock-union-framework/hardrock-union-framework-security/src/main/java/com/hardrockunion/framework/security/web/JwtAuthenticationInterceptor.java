package com.hardrockunion.framework.security.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.hardrockunion.framework.security.context.LoginUserContext;
import com.hardrockunion.framework.security.model.LoginUser;
import com.hardrockunion.framework.security.service.JwtClaimsService;

import io.jsonwebtoken.Claims;

public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtClaimsService jwtClaimsService;

    public JwtAuthenticationInterceptor(JwtClaimsService jwtClaimsService) {
        this.jwtClaimsService = jwtClaimsService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return true;
        }

        String token = authorization.substring(7);
        Claims claims = jwtClaimsService.parse(token);
        Long userId = Long.valueOf(claims.getSubject());
        String appCode = claims.get("appCode", String.class);
        Long tenantId = claims.get("tenantId", Long.class);
        String username = claims.get("username", String.class);
        LoginUserContext.set(new LoginUser(userId, appCode, tenantId, username));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContext.clear();
    }
}
