package com.abhinav.moviebooking.security.token.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Instant expiry;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "refresh_token_roles",
            joinColumns = @JoinColumn(name = "refresh_token_id")
    )
    @Column(name = "role")
    private Set<String> roles;

    @Column(nullable = false)
    private boolean revoked = false;

    public RefreshToken() {
    }

    public RefreshToken(String token, String username, Instant expiry, Set<String> roles, boolean revoked) {
        this.token = token;
        this.username = username;
        this.expiry = expiry;
        this.roles = roles;
        this.revoked = revoked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public void setExpiry(Instant expiry) {
        this.expiry = expiry;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
