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

@RestController
@RequestMapping("/api/canciones")
public class CancionController {

    private final CancionService cancionService;

    @Autowired
    public CancionController(CancionService cancionService) {
        this.cancionService = cancionService;
    }

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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<CancionDto>> obtenerTodasLasCanciones() {
        return ResponseEntity.ok(cancionService.obtenerTodasLasCanciones());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<CancionDto> obtenerCancionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cancionService.obtenerCancionPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CancionDto> actualizarCancion(@PathVariable Long id, @Valid @RequestBody CancionDto cancionDto) {
        return ResponseEntity.ok(cancionService.actualizarCancion(id, cancionDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCancion(@PathVariable Long id) {
        cancionService.eliminarCancion(id);
        return ResponseEntity.noContent().build();
    }
}
