package com.example.playlist_api.controller;

import com.example.playlist_api.dto.CancionDto;
import com.example.playlist_api.service.CancionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para la gestión de canciones.
 * Proporciona endpoints para operaciones CRUD sobre la entidad Cancion.
 */
@RestController
@RequestMapping("/api/canciones")
public class CancionController {

    private final CancionService cancionService;

    @Autowired
    public CancionController(CancionService cancionService) {
        this.cancionService = cancionService;
    }

    /**
     * Crea una nueva canción. Solo accesible por usuarios con rol ADMIN.
     *
     * @param cancionDto DTO con los datos de la canción a crear.
     * @return ResponseEntity con la canción creada y el estado HTTP 201 (Created).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CancionDto> crearCancion(@Valid @RequestBody CancionDto cancionDto) {
        CancionDto createdCancion = cancionService.crearCancion(cancionDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCancion.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdCancion);
    }

    /**
     * Obtiene todas las canciones existentes. Accesible por usuarios con rol ADMIN o USER.
     *
     * @return ResponseEntity con una lista de CancionDto y el estado HTTP 200 (OK).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<CancionDto>> obtenerTodasLasCanciones() {
        return ResponseEntity.ok(cancionService.obtenerTodasLasCanciones());
    }

    /**
     * Obtiene una canción por su ID. Accesible por usuarios con rol ADMIN o USER.
     *
     * @param id ID de la canción a buscar.
     * @return ResponseEntity con la CancionDto encontrada y el estado HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<CancionDto> obtenerCancionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cancionService.obtenerCancionPorId(id));
    }

    /**
     * Actualiza una canción existente por su ID. Solo accesible por usuarios con rol ADMIN.
     *
     * @param id ID de la canción a actualizar.
     * @param cancionDto DTO con los datos actualizados de la canción.
     * @return ResponseEntity con la CancionDto actualizada y el estado HTTP 200 (OK).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CancionDto> actualizarCancion(@PathVariable Long id, @Valid @RequestBody CancionDto cancionDto) {
        return ResponseEntity.ok(cancionService.actualizarCancion(id, cancionDto));
    }

    /**
     * Elimina una canción por su ID. Solo accesible por usuarios con rol ADMIN.
     *
     * @param id ID de la canción a eliminar.
     * @return ResponseEntity con estado HTTP 204 (No Content) si la eliminación es exitosa.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCancion(@PathVariable Long id) {
        cancionService.eliminarCancion(id);
        return ResponseEntity.noContent().build();
    }
}
