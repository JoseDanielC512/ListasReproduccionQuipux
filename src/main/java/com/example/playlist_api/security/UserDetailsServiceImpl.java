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

/**
 * Implementación personalizada de {@link UserDetailsService} para cargar los detalles del usuario.
 * Esta clase carga usuarios desde las propiedades de la aplicación (en memoria) y codifica sus contraseñas.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    // Inyección de valores de configuración para el usuario administrador desde application.properties.
    @Value("${app.security.users.admin.username}")
    private String adminUsername;
    @Value("${app.security.users.admin.password}")
    private String adminPassword;
    @Value("${app.security.users.admin.roles}")
    private String adminRoles;

    // Inyección de valores de configuración para el usuario regular desde application.properties.
    @Value("${app.security.users.user.username}")
    private String regularUsername;
    @Value("${app.security.users.user.password}")
    private String regularPassword;
    @Value("${app.security.users.user.roles}")
    private String regularRoles;

    // Inyector de contraseñas para codificar las contraseñas de los usuarios.
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor para inyectar el codificador de contraseñas.
     *
     * @param passwordEncoder El codificador de contraseñas (ej. BCryptPasswordEncoder) utilizado para codificar las contraseñas.
     */
    public UserDetailsServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Carga los detalles del usuario por su nombre de usuario.
     * Este método es invocado por Spring Security durante el proceso de autenticación.
     *
     * @param username El nombre de usuario del usuario a cargar.
     * @return Un objeto {@link UserDetails} que contiene la información del usuario (nombre de usuario, contraseña codificada, roles).
     * @throws UsernameNotFoundException Si el usuario con el nombre de usuario especificado no es encontrado.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Verifica si el nombre de usuario coincide con el del administrador.
        if (adminUsername.equals(username)) {
            return new User(adminUsername,
                            passwordEncoder.encode(adminPassword), // Codifica la contraseña antes de devolverla.
                            getAuthorities(adminRoles)); // Obtiene las autoridades (roles) del administrador.
        } 
        // Verifica si el nombre de usuario coincide con el del usuario regular.
        else if (regularUsername.equals(username)) {
            return new User(regularUsername,
                            passwordEncoder.encode(regularPassword), // Codifica la contraseña.
                            getAuthorities(regularRoles)); // Obtiene las autoridades (roles) del usuario regular.
        } 
        // Si el usuario no es encontrado en las configuraciones en memoria.
        else {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }
    }

    /**
     * Convierte una cadena de roles (separados por comas) en una lista de objetos {@link GrantedAuthority}.
     * Asegura que cada rol tenga el prefijo "ROLE_" requerido por Spring Security para el uso con {@code hasRole()}.
     *
     * @param rolesString Una cadena que contiene los roles del usuario, separados por comas (ej. "ADMIN,USER").
     * @return Una lista de {@link GrantedAuthority} que representa los roles del usuario.
     */
    private List<GrantedAuthority> getAuthorities(String rolesString) {
        return Arrays.stream(rolesString.split(",")) // Divide la cadena de roles por comas.
                .map(role -> "ROLE_" + role.trim()) // Añade el prefijo "ROLE_" a cada rol y elimina espacios.
                .map(SimpleGrantedAuthority::new) // Convierte cada cadena de rol en un objeto SimpleGrantedAuthority.
                .collect(Collectors.toList()); // Recopila los objetos en una lista.
    }
}
