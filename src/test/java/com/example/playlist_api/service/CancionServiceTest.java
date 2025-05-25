package com.example.playlist_api.service;

import com.example.playlist_api.dto.CancionDto;
import com.example.playlist_api.entity.Cancion;
import com.example.playlist_api.exception.ResourceNotFoundException;
import com.example.playlist_api.repository.CancionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancionServiceTest {

    @Mock
    private CancionRepository cancionRepository;

    @InjectMocks
    private CancionService cancionService;

    @Test
    void crearCancion_success() {
        CancionDto cancionDto = new CancionDto(null, "Titulo Test", "Artista Test", "Album Test", "2023", "Pop");
        Cancion cancion = new Cancion(1L, "Titulo Test", "Artista Test", "Album Test", "2023", "Pop", new HashSet<>());

        when(cancionRepository.save(any(Cancion.class))).thenReturn(cancion);

        CancionDto result = cancionService.crearCancion(cancionDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Titulo Test", result.getTitulo());
        verify(cancionRepository, times(1)).save(any(Cancion.class));
    }

    @Test
    void obtenerTodasLasCanciones_returnsListOfSongs() {
        Cancion cancion1 = new Cancion(1L, "Titulo1", "Artista1", "Album1", "2000", "Pop", new HashSet<>());
        Cancion cancion2 = new Cancion(2L, "Titulo2", "Artista2", "Album2", "2001", "Rock", new HashSet<>());
        List<Cancion> canciones = Arrays.asList(cancion1, cancion2);

        when(cancionRepository.findAll()).thenReturn(canciones);

        List<CancionDto> result = cancionService.obtenerTodasLasCanciones();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Titulo1", result.get(0).getTitulo());
        assertEquals("Titulo2", result.get(1).getTitulo());
        verify(cancionRepository, times(1)).findAll();
    }

    @Test
    void obtenerCancionPorId_found() {
        Long id = 1L;
        Cancion cancion = new Cancion(id, "Titulo Test", "Artista Test", "Album Test", "2023", "Pop", new HashSet<>());

        when(cancionRepository.findById(id)).thenReturn(Optional.of(cancion));

        CancionDto result = cancionService.obtenerCancionPorId(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Titulo Test", result.getTitulo());
        verify(cancionRepository, times(1)).findById(id);
    }

    @Test
    void obtenerCancionPorId_notFound_throwsResourceNotFoundException() {
        Long id = 99L;
        when(cancionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cancionService.obtenerCancionPorId(id));
        verify(cancionRepository, times(1)).findById(id);
    }

    @Test
    void actualizarCancion_success() {
        Long id = 1L;
        CancionDto updatedCancionDto = new CancionDto(id, "Titulo Actualizado", "Artista Actualizado", "Album Actualizado", "2024", "Jazz");
        Cancion existingCancion = new Cancion(id, "Titulo Original", "Artista Original", "Album Original", "2023", "Pop", new HashSet<>());
        Cancion updatedCancion = new Cancion(id, "Titulo Actualizado", "Artista Actualizado", "Album Actualizado", "2024", "Jazz", new HashSet<>());

        when(cancionRepository.findById(id)).thenReturn(Optional.of(existingCancion));
        when(cancionRepository.save(any(Cancion.class))).thenReturn(updatedCancion);

        CancionDto result = cancionService.actualizarCancion(id, updatedCancionDto);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Titulo Actualizado", result.getTitulo());
        assertEquals("Artista Actualizado", result.getArtista());
        verify(cancionRepository, times(1)).findById(id);
        verify(cancionRepository, times(1)).save(any(Cancion.class));
    }

    @Test
    void actualizarCancion_notFound_throwsResourceNotFoundException() {
        Long id = 99L;
        CancionDto cancionDto = new CancionDto(id, "Titulo", "Artista", "Album", "2023", "Pop");

        when(cancionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cancionService.actualizarCancion(id, cancionDto));
        verify(cancionRepository, times(1)).findById(id);
        verify(cancionRepository, never()).save(any(Cancion.class));
    }

    @Test
    void eliminarCancion_success() {
        Long id = 1L;
        when(cancionRepository.existsById(id)).thenReturn(true);
        doNothing().when(cancionRepository).deleteById(id);

        cancionService.eliminarCancion(id);

        verify(cancionRepository, times(1)).existsById(id);
        verify(cancionRepository, times(1)).deleteById(id);
    }

    @Test
    void eliminarCancion_notFound_throwsResourceNotFoundException() {
        Long id = 99L;
        when(cancionRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> cancionService.eliminarCancion(id));
        verify(cancionRepository, times(1)).existsById(id);
        verify(cancionRepository, never()).deleteById(anyLong());
    }
}
