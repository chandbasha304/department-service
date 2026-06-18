package com.example.emsdept.security;

import com.example.emsdept.entity.UserSession;

import com.example.emsdept.repository.UserSessionRepository;

import com.example.emsdept.service.CustomUserDetailsService;
import com.example.emsdept.service.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.
        UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.
        SecurityContextHolder;

import org.springframework.security.core.userdetails.
        UserDetails;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.
        WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.
        HandlerExceptionResolver;

import java.io.IOException;
import java.security.SignatureException;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final CustomUserDetailsService
            userDetailsService;

    private final UserSessionRepository
            sessionRepository;


    private final HandlerExceptionResolver handlerExceptionResolver;



    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path =
                request.getServletPath();

        return path.equals(
                "/api/auth/login"
        ) ||

                path.equals(
                        "/api/auth/sign-up"
                );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = null;

        try {

            String authHeader =
                    request.getHeader("Authorization");

            if (authHeader == null
                    || !authHeader.startsWith("Bearer ")) {

                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7);

            // 1. Validate JWT first
            String username = jwtService.extractUsername(jwt);

            // 2. Validate active session
            log.info("[FILTER] JWT received from request: {}", jwt);

            Optional<UserSession> sessionOpt =
                    sessionRepository.findLatestSessionByToken(jwt);

            log.info("[FILTER] Session present in DB: {}", sessionOpt.isPresent());

            if (sessionOpt.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Session expired. Please login again."
                );
            }

            UserSession session = sessionOpt.get();

            log.info("[FILTER] Session ID: {}", session.getId());
            log.info("[FILTER] Session Active: {}", session.getIsActive());

            if (!Boolean.TRUE.equals(session.getIsActive())) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Session expired. Please login again."
                );
            }
            // 3. Authenticate user
            if (username != null
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        }
        catch (ExpiredJwtException ex) {

            deactivateSession(jwt);

            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Session expired. Please login again."
                    )
            );
        }
        catch (MalformedJwtException |
               UnsupportedJwtException |
               IllegalArgumentException ex) {

            deactivateSession(jwt);

            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid authentication token."
                    )
            );
        }
        catch (UsernameNotFoundException ex) {

            deactivateSession(jwt);

            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "User account not found."
                    )
            );
        }
        catch (Exception ex) {

            deactivateSession(jwt);

            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    ex
            );
        }
    }

    private void deactivateSession(String jwt) {

        if (jwt == null) {
            return;
        }

        sessionRepository
                .findLatestSessionByToken(jwt)
                .ifPresent(session -> {

                    session.setIsActive(false);

                    sessionRepository.save(session);
                });
    }
}