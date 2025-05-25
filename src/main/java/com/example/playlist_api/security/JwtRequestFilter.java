package com.example.playlist_api.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de petición JWT que intercepta todas las peticiones HTTP para validar y procesar tokens JWT.
 * Extiende {@link OncePerRequestFilter} para asegurar que el filtro se ejecute solo una vez por petición.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.header}")
    private String authorizationHeaderName;

    @Value("${jwt.prefix}")
    private String jwtPrefix;

    /**
     * Constructor para inyectar las dependencias necesarias.
     * La inyección de {@code UserDetailsService} se marca con {@code @Lazy} para romper un posible ciclo de dependencias.
     *
     * @param userDetailsService Servicio para cargar los detalles del usuario.
     * @param jwtUtil Utilidad para operaciones relacionadas con JWT (generación, validación, extracción).
     */
    public JwtRequestFilter(@Lazy UserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Realiza el filtrado interno de la petición.
     * Extrae el token JWT del encabezado de autorización, lo valida y configura la autenticación en el contexto de seguridad de Spring.
     *
     * @param request La petición HTTP entrante.
     * @param response La respuesta HTTP saliente.
     * @param chain La cadena de filtros para continuar con la ejecución.
     * @throws ServletException Si ocurre un error específico de servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Obtiene el encabezado de autorización de la petición.
        final String authorizationHeader = request.getHeader(authorizationHeaderName);

        String username = null;
        String jwt = null;

        // Verifica si el encabezado de autorización existe y comienza con el prefijo JWT (ej. "Bearer ").
        if (authorizationHeader != null && authorizationHeader.startsWith(jwtPrefix + " ")) {
            // Extrae el token JWT eliminando el prefijo.
            jwt = authorizationHeader.substring(jwtPrefix.length() + 1);
            try {
                // Intenta extraer el nombre de usuario del token JWT.
                username = jwtUtil.extractUsername(jwt);
            } catch (IllegalArgumentException e) {
                // Registra una advertencia si el token no se pudo obtener.
                logger.warn("No se pudo obtener el token JWT", e);
            } catch (ExpiredJwtException e) {
                // Registra una advertencia si el token ha expirado.
                logger.warn("El token JWT ha expirado", e);
            } catch (SignatureException | MalformedJwtException | UnsupportedJwtException e) {
                // Registra una advertencia si el token JWT es inválido (firma, formato, etc.).
                logger.warn("Token JWT inválido: " + e.getMessage(), e);
            }
        } else {
            // Comentario para depuración: logger.warn("El encabezado de autorización no comienza con Bearer String");
        }

        // Si se extrajo un nombre de usuario y no hay autenticación en el contexto de seguridad actual.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Carga los detalles del usuario usando el UserDetailsService.
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Valida el token JWT con los detalles del usuario.
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // Si el token es válido, crea un objeto de autenticación.
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                // Establece los detalles de la petición en el objeto de autenticación.
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Establece el objeto de autenticación en el contexto de seguridad de Spring.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        // Continúa con la cadena de filtros.
        chain.doFilter(request, response);
    }
}
