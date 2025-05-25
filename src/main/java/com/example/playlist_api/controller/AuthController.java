package com.example.playlist_api.controller;

import com.example.playlist_api.dto.AuthRequestDto;
import com.example.playlist_api.dto.AuthResponseDto;
import com.example.playlist_api.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la autenticación de usuarios.
 * Proporciona un endpoint para que los usuarios inicien sesión y obtengan un token JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Autentica a un usuario y genera un token JWT si las credenciales son válidas.
     *
     * @param authenticationRequest DTO que contiene el nombre de usuario y la contraseña.
     * @return ResponseEntity con el token JWT si la autenticación es exitosa.
     * @throws Exception Si el usuario está deshabilitado o las credenciales son inválidas.
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody AuthRequestDto authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponseDto(token));
    }

    /**
     * Método auxiliar para autenticar al usuario utilizando el AuthenticationManager.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @throws Exception Si la autenticación falla (usuario deshabilitado o credenciales inválidas).
     */
    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }
}
