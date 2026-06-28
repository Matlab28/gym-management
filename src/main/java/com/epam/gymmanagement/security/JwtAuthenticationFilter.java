package com.epam.gymmanagement.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final UserSessionService userSessionService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(authorizationHeader);

        try {
            authenticate(token, request);
        } catch (JwtException | AuthenticationException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(String authorizationHeader) {
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();

        if (token.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return token.substring(BEARER_PREFIX.length()).trim();
        }

        return token;
    }

//    private void authenticate(String token, HttpServletRequest request) {
//        String username = jwtService.extractUsername(token);
//
//        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
//            return;
//        }
//
//        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//
//        if (!jwtService.isTokenValid(token, userDetails)) {
//            return;
//        }
//
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken(
//                        userDetails,
//                        null,
//                        userDetails.getAuthorities()
//                );
//
//        authenticationToken.setDetails(
//                new WebAuthenticationDetailsSource().buildDetails(request)
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//    }

    private void authenticate(String token, HttpServletRequest request) {
        if (!userSessionService.isSessionActive(token)) {
            return;
        }

        if (userSessionService.isInactive(token)) {

            userSessionService.signOut(token);

            return;
        }

        authenticateUser(token, request);

        userSessionService.updateLastActivity(token);
    }

    private void authenticateUser(
            String token,
            HttpServletRequest request
    ) {

        String username = jwtService.extractUsername(token);

        if (username == null
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(token, userDetails)) {
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}
