package io.sphynx.server.config.security;

import io.sphynx.server.dto.GenericResponse;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.model.enums.TokenType;
import io.sphynx.server.service.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @NonNull HttpServletRequest servletRequest,
            @NonNull HttpServletResponse servletResponse,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = null;

            Cookie[] cookies = servletRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("JWT".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token == null || token.isEmpty()) {
                SecurityContextHolder.clearContext();
                servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization token missing or invalid");
                return;
            }

            if (!jwtService.isTokenValid(token, TokenType.AUTH)) {
                SecurityContextHolder.clearContext();
                servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            UserModel user = this.jwtService.extractUserFromToken(token, TokenType.AUTH);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(servletRequest));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(servletRequest, servletResponse);

        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");

        } catch (Exception e) {
            logger.error("An error occurred while authenticating user:", e);
            SecurityContextHolder.clearContext();
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error");
        }
    }
}
