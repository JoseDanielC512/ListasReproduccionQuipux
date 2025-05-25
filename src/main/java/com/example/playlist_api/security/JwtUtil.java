package com.example.playlist_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Clase de utilidad para la generación, validación y extracción de información de tokens JWT (JSON Web Tokens).
 * Utiliza la librería jjwt para realizar las operaciones criptográficas y de parsing.
 */
@Component
public class JwtUtil {

    // Clave secreta para firmar y verificar los tokens JWT, inyectada desde las propiedades de la aplicación.
    @Value("${jwt.secret}")
    private String secretString;

    // Tiempo de expiración del token JWT en milisegundos, inyectado desde las propiedades de la aplicación.
    @Value("${jwt.expiration.ms}")
    private long jwtExpirationInMs;

    /**
     * Genera la clave secreta de firma a partir de la cadena de texto configurada.
     * La clave se codifica en UTF-8 y se utiliza para algoritmos HMAC SHA.
     *
     * @return Una instancia de {@link SecretKey} para la firma JWT.
     */
    private SecretKey getSigningKey() {
        // Convierte la cadena secreta a bytes usando UTF-8.
        byte[] keyBytes = this.secretString.getBytes(StandardCharsets.UTF_8);
        // Genera una clave HMAC SHA a partir de los bytes de la clave.
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el nombre de usuario (subject) del token JWT.
     *
     * @param token El token JWT del cual extraer el nombre de usuario.
     * @return El nombre de usuario (String) contenido en el token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     *
     * @param token El token JWT del cual extraer la fecha de expiración.
     * @return La fecha de expiración (Date) del token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un "claim" específico del token JWT utilizando una función resolutora.
     *
     * @param token El token JWT.
     * @param claimsResolver Una función que toma un objeto {@link Claims} y devuelve el valor del claim deseado.
     * @param <T> El tipo del valor del claim.
     * @return El valor del claim extraído.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        // Extrae todos los claims del token.
        final Claims claims = extractAllClaims(token);
        // Aplica la función resolutora para obtener el claim específico.
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims (cuerpo) del token JWT.
     * Realiza el parsing del token y verifica su firma utilizando la clave secreta.
     *
     * @param token El token JWT a parsear.
     * @return Un objeto {@link Claims} que contiene todos los claims del token.
     */
    private Claims extractAllClaims(String token) {
        // Construye un parser JWT, establece la clave de firma y parsea el token.
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    /**
     * Verifica si el token JWT ha expirado.
     *
     * @param token El token JWT a verificar.
     * @return {@code true} si el token ha expirado, {@code false} en caso contrario.
     */
    private Boolean isTokenExpired(String token) {
        // Compara la fecha de expiración del token con la fecha actual.
        return extractExpiration(token).before(new Date());
    }

    /**
     * Genera un token JWT para un usuario dado.
     * Incluye el nombre de usuario como "subject" y los roles del usuario como un "claim" personalizado.
     *
     * @param userDetails Los detalles del usuario para quien se generará el token.
     * @return El token JWT generado como una cadena de texto.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Convierte los roles del usuario en una cadena separada por comas y los añade como un claim.
        String roles = userDetails.getAuthorities().stream()
                         .map(GrantedAuthority::getAuthority)
                         .collect(Collectors.joining(","));
        claims.put("roles", roles);
        // Crea el token JWT con los claims, el subject (nombre de usuario) y la información de expiración/firma.
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Crea el token JWT final con los claims, el subject, la fecha de emisión, la fecha de expiración y la firma.
     *
     * @param claims Los claims personalizados a incluir en el token.
     * @param subject El "subject" del token (generalmente el nombre de usuario).
     * @return El token JWT compactado y firmado.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims) // Establece los claims.
                .setSubject(subject) // Establece el subject.
                .setIssuedAt(new Date(System.currentTimeMillis())) // Establece la fecha de emisión.
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs)) // Establece la fecha de expiración.
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Firma el token con la clave secreta y el algoritmo HS512.
                .compact(); // Compacta el token en su representación final de cadena.
    }

    /**
     * Valida un token JWT dado contra los detalles de un usuario.
     * Verifica que el nombre de usuario en el token coincida con el del usuario y que el token no haya expirado.
     *
     * @param token El token JWT a validar.
     * @param userDetails Los detalles del usuario con los que se comparará el token.
     * @return {@code true} si el token es válido para el usuario, {@code false} en caso contrario.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // El token es válido si el nombre de usuario coincide y el token no ha expirado.
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
