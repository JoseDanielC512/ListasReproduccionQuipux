package com.example.playlist_api.security;

import com.example.playlist_api.dto.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

/**
 * Implementación de {@link AuthenticationEntryPoint} para manejar errores de autenticación en APIs REST.
 * Esta clase se encarga de enviar una respuesta de error 401 Unauthorized cuando un usuario no autenticado
 * intenta acceder a un recurso protegido. La respuesta se formatea como JSON utilizando {@link ErrorResponseDto}.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Se invoca cuando un usuario no autenticado intenta acceder a un recurso protegido.
     * Construye y envía una respuesta JSON con el estado HTTP 401 (Unauthorized) y detalles del error.
     *
     * @param request La petición HTTP que causó la excepción de autenticación.
     * @param response La respuesta HTTP a la que se enviará la respuesta de error.
     * @param authException La excepción de autenticación que fue lanzada.
     * @throws IOException Si ocurre un error de entrada/salida al escribir la respuesta.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        // Establece el estado de la respuesta HTTP a 401 Unauthorized.
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        // Establece el tipo de contenido de la respuesta a JSON.
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Construye el objeto ErrorResponseDto con los detalles del error.
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                LocalDateTime.now(), // Marca de tiempo del error.
                HttpStatus.UNAUTHORIZED.value(), // Código de estado HTTP.
                "Unauthorized", // Mensaje de error general.
                "Se requiere autenticación para acceder a este recurso. " + authException.getMessage(), // Mensaje detallado.
                request.getRequestURI(), // URI de la petición.
                null // No hay errores de validación específicos en este caso.
        );

        // Escribe el objeto ErrorResponseDto como JSON en el cuerpo de la respuesta.
        OutputStream out = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        // Registra el módulo JavaTimeModule para serializar objetos LocalDateTime correctamente.
        mapper.registerModule(new JavaTimeModule());
        mapper.writeValue(out, errorResponse);
        out.flush(); // Asegura que todos los datos se escriban en la respuesta.
    }
}
