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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/auth")
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
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequest.getEmail());

        return ResponseEntity.ok().body(new AuthResponse(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .badRequest()
                    .body("Missing or invalid Authorization Header");
        }


        try{
            // 2. Validate token (this MUST verify signature + expiry)
            String accessToken = authHeader.substring(7);
            long expiry = jwtUtil.extractExpiration(accessToken);

            // 2. Blacklist ACCESS TOKEN
            blackListService.blackList(accessToken, expiry);

            // 3. clear security context
            SecurityContextHolder.clearContext();

            return  ResponseEntity.ok().body("Logged out successfully");
        }
        catch (ExpiredJwtException e){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Expired JWT Token");
        }
        catch (JwtException e){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT Token");
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest, HttpServletRequest request) {

        String refreshTokenValue = refreshTokenRequest.getRefreshToken();

        // 1. Validate refresh token (DB lookup)
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);

        // 2. Rotate refresh token
        refreshTokenService.revokeToken(refreshToken);

        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(refreshToken.getUsername());

        // 3. Issue new ACCESS TOKEN (JWT)
        String newAccessToken = jwtUtil.generateToken(
                refreshToken.getUsername(),
                refreshToken.getRoles() != null ? refreshToken.getRoles() : Set.of("USER")
        );

        return ResponseEntity.ok(
                new AuthResponse(newAccessToken, newRefreshToken.getToken())
        );


    }
}
