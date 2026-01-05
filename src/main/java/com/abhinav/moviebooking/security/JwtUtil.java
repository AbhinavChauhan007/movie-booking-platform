package com.abhinav.moviebooking.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final long EXPIRATION_TIME = 10 * 60; // 5 minutes
    private static final String SECRET_KEY = "mySecretKeyForJsonWebTokenThatIsAtLeast256BitsLong!";

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Generate JWT Token via authentication object
    public String generateToken(Authentication authentication) {
        Set<String> roles = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return buildToken(authentication.getName(), roles);
    }

    // Generate JWT Token via username and roles -> Refresh token usage
    public String generateToken(String username, Set<String> roles) {
        return buildToken(username, roles);
    }

    // Extract Username
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }


    // Extract roles
    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        List<String> roles = extractAllClaims(token).get("roles", List.class);
        return roles == null ? Set.of() : new HashSet<>(roles);
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public Long extractExpiration(String token) {
        return extractAllClaims(token).getExpiration().getTime();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String buildToken(String username, Set<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
