package com.example.playlist_api.config;

import com.example.playlist_api.security.JwtAuthenticationEntryPoint;
import com.example.playlist_api.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP para la aplicación.
     * Define las reglas de autorización, el manejo de sesiones y la integración de JWT.
     *
     * @param httpSecurity El objeto HttpSecurity proporcionado por Spring Security para configurar la seguridad web.
     * @return Una instancia de SecurityFilterChain que representa la cadena de filtros de seguridad configurada.
     * @throws Exception Si ocurre un error durante la configuración de seguridad.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // Habilitar CORS usando la configuración definida en el bean corsConfigurationSource
            .cors(Customizer.withDefaults())
            // Deshabilita la protección CSRF (Cross-Site Request Forgery) ya que la API es stateless y usa JWT.
            .csrf(csrf -> csrf.disable())
            // Configura las reglas de autorización para las peticiones HTTP.
            .authorizeHttpRequests(auth -> auth
                // Permite el acceso público al endpoint de login.
                .requestMatchers("/api/auth/login").permitAll()
                // Permite el acceso público a la consola H2 (útil para desarrollo).
                .requestMatchers("/h2-console/**").permitAll()
                // Permite a usuarios con roles 'USER' o 'ADMIN' acceder a los métodos GET de canciones y listas.
                .requestMatchers(HttpMethod.GET, "/api/canciones/**", "/api/listas/**").hasAnyRole("USER", "ADMIN")
                // Restringe los métodos POST de canciones y listas solo a usuarios con rol 'ADMIN'.
                .requestMatchers(HttpMethod.POST, "/api/canciones/**", "/api/listas/**").hasRole("ADMIN")
                // Restringe los métodos PUT de canciones y listas solo a usuarios con rol 'ADMIN'.
                .requestMatchers(HttpMethod.PUT, "/api/canciones/**", "/api/listas/**").hasRole("ADMIN")
                // Restringe los métodos DELETE de canciones y listas solo a usuarios con rol 'ADMIN'.
                .requestMatchers(HttpMethod.DELETE, "/api/canciones/**", "/api/listas/**").hasRole("ADMIN")
                // Cualquier otra petición debe estar autenticada.
                .anyRequest().authenticated()
            )
            // Configura el manejador de excepciones para la autenticación fallida.
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            // Configura la gestión de sesiones como STATELESS, lo que significa que no se crean sesiones HTTP.
            // Esto es crucial para una API REST que usa JWT, ya que cada petición lleva su propio token.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Añade el filtro JWT personalizado antes del filtro de autenticación de usuario y contraseña de Spring Security.
        // Esto asegura que el token JWT sea validado antes de que la autenticación estándar ocurra.
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Configura las opciones de encabezado para permitir que la consola H2 se muestre en un iframe.
        httpSecurity.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Especificar el origen del frontend Angular
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        // Especificar los métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Especificar las cabeceras permitidas
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", // Para el token JWT
            "Content-Type",  // Para el tipo de contenido de la solicitud (ej. application/json)
            "Cache-Control",
            "X-Requested-With",
            "Accept"
        ));
        // Permitir credenciales (si se usan cookies o autenticación HTTP básica, aunque para JWT stateless puede no ser estrictamente necesario)
        configuration.setAllowCredentials(true); 
        // Exponer cabeceras (si el frontend necesita leer alguna cabecera específica de la respuesta)
        // configuration.setExposedHeaders(Arrays.asList("Authorization")); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicar esta configuración CORS a todas las rutas de la API
        source.registerCorsConfiguration("/**", configuration); 
        return source;
    }
}
