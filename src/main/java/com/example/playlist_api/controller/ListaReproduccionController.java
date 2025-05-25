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

/**
 * Controlador REST para la gestión de listas de reproducción.
 * Proporciona endpoints para operaciones CRUD sobre la entidad ListaReproduccion.
 */
@RestController
@RequestMapping("/api/listas")
public class ListaReproduccionController {

    private final ListaReproduccionService listaReproduccionService;

    @Autowired
    public ListaReproduccionController(ListaReproduccionService listaReproduccionService) {
        this.listaReproduccionService = listaReproduccionService;
    }

    /**
     * Crea una nueva lista de reproducción. Solo accesible por usuarios con rol ADMIN.
     *
     * @param listaReproduccionRequestDto DTO con los datos de la lista de reproducción a crear.
     * @return ResponseEntity con la lista de reproducción creada y el estado HTTP 201 (Created).
     */
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
    
    /**
     * Clase interna para el DTO de la petición de creación de lista con canción inicial.
     */
    static class CrearListaReproduccionConCancionRequest {
        @Valid
        private NombreDescripcionListaDto details;
        public NombreDescripcionListaDto getDetails() { return details; }
        public void setDetails(NombreDescripcionListaDto details) { this.details = details; }
    }

    /**
     * Clase interna para el DTO que contiene el nombre y la descripción de la lista.
     */
    static class NombreDescripcionListaDto {
        @jakarta.validation.constraints.NotBlank
        private String nombre;
        private String descripcion;
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    /**
     * Crea una nueva lista de reproducción y le asocia una canción inicial. Solo accesible por usuarios con rol ADMIN.
     *
     * @param idCancion ID de la canción a asociar inicialmente.
     * @param listaDetails DTO con el nombre y la descripción de la lista.
     * @return ResponseEntity con la lista de reproducción creada y el estado HTTP 201 (Created).
     */
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


    /**
     * Obtiene todas las listas de reproducción existentes. Accesible por usuarios con rol ADMIN o USER.
     *
     * @return ResponseEntity con una lista de ListaReproduccionResponseDto y el estado HTTP 200 (OK).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<ListaReproduccionResponseDto>> obtenerTodasLasListasReproduccion() {
        return ResponseEntity.ok(listaReproduccionService.obtenerTodasLasListasReproduccion());
    }

    /**
     * Obtiene una lista de reproducción por su nombre. Accesible por usuarios con rol ADMIN o USER.
     *
     * @param nombreLista Nombre de la lista de reproducción a buscar.
     * @return ResponseEntity con la ListaReproduccionResponseDto encontrada y el estado HTTP 200 (OK).
     */
    @GetMapping("/porNombre/{nombreLista}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ListaReproduccionResponseDto> obtenerListaReproduccionPorNombre(@PathVariable String nombreLista) {
        return ResponseEntity.ok(listaReproduccionService.obtenerListaReproduccionPorNombre(nombreLista));
    }

    /**
     * Obtiene una lista de reproducción por su ID. Accesible por usuarios con rol ADMIN o USER.
     *
     * @param id ID de la lista de reproducción a buscar.
     * @return ResponseEntity con la ListaReproduccionResponseDto encontrada y el estado HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ListaReproduccionResponseDto> obtenerListaReproduccionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(listaReproduccionService.obtenerListaReproduccionPorId(id));
    }
    
    /**
     * Actualiza una lista de reproducción existente por su ID. Solo accesible por usuarios con rol ADMIN.
     *
     * @param id ID de la lista de reproducción a actualizar.
     * @param listaReproduccionRequestDto DTO con los datos actualizados de la lista de reproducción.
     * @return ResponseEntity con la ListaReproduccionResponseDto actualizada y el estado HTTP 200 (OK).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListaReproduccionResponseDto> actualizarListaReproduccion(@PathVariable Long id, @Valid @RequestBody ListaReproduccionRequestDto listaReproduccionRequestDto) {
        ListaReproduccionResponseDto updatedListaReproduccion = listaReproduccionService.actualizarListaReproduccion(id, listaReproduccionRequestDto);
        return ResponseEntity.ok(updatedListaReproduccion);
    }

    /**
     * Elimina una lista de reproducción por su nombre. Solo accesible por usuarios con rol ADMIN.
     *
     * @param nombreLista Nombre de la lista de reproducción a eliminar.
     * @return ResponseEntity con estado HTTP 204 (No Content) si la eliminación es exitosa.
     */
    @DeleteMapping("/porNombre/{nombreLista}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarListaReproduccionPorNombre(@PathVariable String nombreLista) {
        listaReproduccionService.eliminarListaReproduccionPorNombre(nombreLista);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Elimina una lista de reproducción por su ID. Solo accesible por usuarios con rol ADMIN.
     *
     * @param id ID de la lista de reproducción a eliminar.
     * @return ResponseEntity con estado HTTP 204 (No Content) si la eliminación es exitosa.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarListaReproduccionPorId(@PathVariable Long id) {
        listaReproduccionService.eliminarListaReproduccionPorId(id);
        return ResponseEntity.noContent().build();
    }
}
