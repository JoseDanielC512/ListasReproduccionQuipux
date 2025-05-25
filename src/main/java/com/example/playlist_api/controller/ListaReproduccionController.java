package com.example.playlist_api.controller;

import com.example.playlist_api.dto.ListaReproduccionRequestDto;
import com.example.playlist_api.dto.ListaReproduccionResponseDto;
import com.example.playlist_api.service.ListaReproduccionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/listas")
public class ListaReproduccionController {

    private final ListaReproduccionService listaReproduccionService;

    @Autowired
    public ListaReproduccionController(ListaReproduccionService listaReproduccionService) {
        this.listaReproduccionService = listaReproduccionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListaReproduccionResponseDto> crearListaReproduccion(@Valid @RequestBody ListaReproduccionRequestDto listaReproduccionRequestDto) {
        ListaReproduccionResponseDto createdListaReproduccion = listaReproduccionService.crearListaReproduccion(listaReproduccionRequestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdListaReproduccion.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdListaReproduccion);
    }
    
    static class CrearListaReproduccionConCancionRequest {
        @Valid
        private NombreDescripcionListaDto details;
        public NombreDescripcionListaDto getDetails() { return details; }
        public void setDetails(NombreDescripcionListaDto details) { this.details = details; }
    }
    static class NombreDescripcionListaDto {
        @jakarta.validation.constraints.NotBlank
        private String nombre;
        private String descripcion;
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    @PostMapping("/con-cancion-inicial/{idCancion}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListaReproduccionResponseDto> crearListaReproduccionConCancionInicial(
            @PathVariable Long idCancion,
            @Valid @RequestBody NombreDescripcionListaDto listaDetails) {
        ListaReproduccionResponseDto createdListaReproduccion = listaReproduccionService.crearListaReproduccionConCancion(
                listaDetails.getNombre(),
                listaDetails.getDescripcion(),
                idCancion
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/../{id}")
                .buildAndExpand(createdListaReproduccion.getId())
                .normalize()
                .toUri();
        return ResponseEntity.created(location).body(createdListaReproduccion);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<ListaReproduccionResponseDto>> obtenerTodasLasListasReproduccion() {
        return ResponseEntity.ok(listaReproduccionService.obtenerTodasLasListasReproduccion());
    }

    @GetMapping("/porNombre/{nombreLista}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ListaReproduccionResponseDto> obtenerListaReproduccionPorNombre(@PathVariable String nombreLista) {
        return ResponseEntity.ok(listaReproduccionService.obtenerListaReproduccionPorNombre(nombreLista));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ListaReproduccionResponseDto> obtenerListaReproduccionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(listaReproduccionService.obtenerListaReproduccionPorId(id));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListaReproduccionResponseDto> actualizarListaReproduccion(@PathVariable Long id, @Valid @RequestBody ListaReproduccionRequestDto listaReproduccionRequestDto) {
        ListaReproduccionResponseDto updatedListaReproduccion = listaReproduccionService.actualizarListaReproduccion(id, listaReproduccionRequestDto);
        return ResponseEntity.ok(updatedListaReproduccion);
    }

    @DeleteMapping("/porNombre/{nombreLista}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarListaReproduccionPorNombre(@PathVariable String nombreLista) {
        listaReproduccionService.eliminarListaReproduccionPorNombre(nombreLista);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarListaReproduccionPorId(@PathVariable Long id) {
        listaReproduccionService.eliminarListaReproduccionPorId(id);
        return ResponseEntity.noContent().build();
    }
}
