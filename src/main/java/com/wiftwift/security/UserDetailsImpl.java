package com.wiftwift.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.wiftwift.entity.User;

import java.util.Collection;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        
        return user.getRoles().stream()
            .map(role -> (GrantedAuthority) () -> "ROLE_" + role.getName())  
            .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();  
    }

    @Override
    public String getUsername() {
        return user.getUsername();  
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  
    }

    @Override
    public boolean isEnabled() {
        return true;  
    }
}
