package com.abhinav.moviebooking.security;

import com.abhinav.moviebooking.security.token.TokenBlackListService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlackListService blackListService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlackListService blackListService) {
        this.jwtUtil = jwtUtil;
        this.blackListService = blackListService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals("/auth/login")
                || request.getServletPath().equals("/auth/logout")
                || request.getServletPath().equals("/auth/refresh")
                || request.getServletPath().startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No token present");
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring(7);

        // 1. Check blacklist
        if (blackListService.isBlackListed(jwtToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String username = jwtUtil.extractUsername(jwtToken);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                Set<String> roles = jwtUtil.extractRoles(jwtToken);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username, null, authorities);

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("alpha -> " + authentication.toString());
        filterChain.doFilter(request, response);
    }


}