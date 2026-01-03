package com.abhinav.moviebooking.security;

import org.springframework.security.core.GrantedAuthority;

public class JwtGrantedAuthority implements GrantedAuthority {

    private final String authority;

    public JwtGrantedAuthority(String authority) {
        this.authority = authority;
    }


    @Override
    public String getAuthority() {
        return authority;
    }
}
