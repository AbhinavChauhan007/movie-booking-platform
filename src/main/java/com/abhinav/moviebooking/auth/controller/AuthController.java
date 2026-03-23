package com.abhinav.moviebooking.auth.controller;

import com.abhinav.moviebooking.auth.dto.request.AuthRequest;
import com.abhinav.moviebooking.auth.dto.request.RefreshTokenRequest;
import com.abhinav.moviebooking.auth.dto.response.AuthResponse;
import com.abhinav.moviebooking.security.JwtUtil;
import com.abhinav.moviebooking.security.token.TokenBlackListService;
import com.abhinav.moviebooking.security.token.entity.RefreshToken;
import com.abhinav.moviebooking.security.token.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and authorization operations")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenBlackListService blackListService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, TokenBlackListService blackListService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.blackListService = blackListService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user with email and password, returns JWT access token and refresh token"
    )
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest) {

        // 1. Authenticate (DB hit happens INSIDE this call)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );

        // set authentication in the context

        SecurityContextHolder.getContext().setAuthentication(authentication);
        // generate accessToken (JWT)
        String accessToken = jwtUtil.generateToken(authentication);

        // generate refresh token
        Set<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(authRequest.getEmail(), roles);

        return ResponseEntity.ok().body(new AuthResponse(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "Blacklist the current JWT token to prevent further use"
    )
    public ResponseEntity<String> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .badRequest()
                    .body("Missing or invalid Authorization Header");
        }


        try {
            // 2. Validate token (this MUST verify signature + expiry)
            String accessToken = authHeader.substring(7);
            long expiry = jwtUtil.extractExpiration(accessToken);

            // 2. Blacklist ACCESS TOKEN
            blackListService.blackList(accessToken, expiry);

            // 3. clear security context
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok().body("Logged out successfully");
        } catch (ExpiredJwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Expired JWT Token");
        } catch (JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT Token");
        }
    }


    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generate new access token and refresh token using existing refresh token"
    )
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {

        String refreshTokenValue = refreshTokenRequest.getRefreshToken();

        // 1. Validate refresh token (DB lookup + expiry + revoked check)
        RefreshToken refreshToken =
                refreshTokenService.validateRefreshToken(refreshTokenValue);

        // 2. Rotate (revoke old + issue new refresh token WITH SAME ROLES)
        refreshTokenService.revokeToken(refreshToken);

        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(
                        refreshToken.getUsername(),
                        refreshToken.getRoles()
                );

        // 3. Issue new ACCESS TOKEN using ROLES FROM REFRESH TOKEN
        String newAccessToken = jwtUtil.generateToken(
                refreshToken.getUsername(),
                refreshToken.getRoles()
        );

        return ResponseEntity.ok(
                new AuthResponse(newAccessToken, newRefreshToken.getToken())
        );
    }

}
