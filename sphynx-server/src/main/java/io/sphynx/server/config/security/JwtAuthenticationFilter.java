package io.sphynx.server.config.security;

import io.sphynx.server.model.UserModel;
import io.sphynx.server.model.enums.TokenType;
import io.sphynx.server.service.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final JwtService jwtService;
    private final Endpoints endpoints;

    @Autowired
    public JwtAuthenticationFilter(
            JwtService jwtService,
            Endpoints endpoints
    ) {
        this.jwtService = jwtService;
        this.endpoints = endpoints;
    }

    @Override
    protected boolean shouldNotFilter(
            @NonNull HttpServletRequest request
    ) throws ServletException {
        return Arrays.stream(this.endpoints.getCommonEndpoints()).anyMatch(pattern -> pathMatcher.match(pattern, request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            if (!this.isBearerTokenPresent(authHeader)) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header missing or invalid");
                return;
            }

            final String token = this.extractToken(authHeader);
            if (!jwtService.isTokenValid(token, TokenType.AUTH)) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            UserModel user = this.jwtService.extractUserFromToken(token, TokenType.AUTH);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");

        } catch (Exception e) {
            logger.error("An error occurred while authenticating user:", e);
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error");
        }
    }

    private boolean isBearerTokenPresent(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

    private String extractToken(String authHeader) {
        return authHeader.substring(7);
    }
}
