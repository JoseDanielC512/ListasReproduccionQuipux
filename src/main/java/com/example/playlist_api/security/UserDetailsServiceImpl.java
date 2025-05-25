package com.example.playlist_api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Value("${app.security.users.admin.username}")
    private String adminUsername;
    @Value("${app.security.users.admin.password}")
    private String adminPassword;
    @Value("${app.security.users.admin.roles}")
    private String adminRoles;

    @Value("${app.security.users.user.username}")
    private String regularUsername;
    @Value("${app.security.users.user.password}")
    private String regularPassword;
    @Value("${app.security.users.user.roles}")
    private String regularRoles;

    private final PasswordEncoder passwordEncoder;

    public UserDetailsServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (adminUsername.equals(username)) {
            return new User(adminUsername,
                            passwordEncoder.encode(adminPassword),
                            getAuthorities(adminRoles));
        } else if (regularUsername.equals(username)) {
            return new User(regularUsername,
                            passwordEncoder.encode(regularPassword),
                            getAuthorities(regularRoles));
        } else {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }
    }

    private List<GrantedAuthority> getAuthorities(String rolesString) {
        return Arrays.stream(rolesString.split(","))
                .map(role -> "ROLE_" + role.trim())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
