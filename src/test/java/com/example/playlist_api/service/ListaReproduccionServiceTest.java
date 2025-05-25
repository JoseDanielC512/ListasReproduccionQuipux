package com.example.playlist_api.service;

import com.example.playlist_api.dto.ListaReproduccionRequestDto;
import com.example.playlist_api.dto.ListaReproduccionResponseDto;
import com.example.playlist_api.dto.CancionDto;
import com.example.playlist_api.entity.ListaReproduccion;
import com.example.playlist_api.entity.Cancion;
import com.example.playlist_api.exception.BadRequestException;
import com.example.playlist_api.exception.ResourceNotFoundException;
import com.example.playlist_api.repository.ListaReproduccionRepository;
import com.example.playlist_api.repository.CancionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListaReproduccionServiceTest {

    @Mock
    private ListaReproduccionRepository listaReproduccionRepository;

    @Mock
    private CancionRepository cancionRepository;

    @InjectMocks
    private ListaReproduccionService listaReproduccionService;

    @Test
    void crearListaReproduccion_success() {
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Mi Lista", "Descripción", Collections.emptySet());
        ListaReproduccion listaReproduccion = new ListaReproduccion(1L, "Mi Lista", "Descripción", new HashSet<>());
        
        when(listaReproduccionRepository.existsByNombre("Mi Lista")).thenReturn(false);
        when(listaReproduccionRepository.save(any(ListaReproduccion.class))).thenReturn(listaReproduccion);

        ListaReproduccionResponseDto responseDto = listaReproduccionService.crearListaReproduccion(requestDto);

        assertNotNull(responseDto);
        assertEquals("Mi Lista", responseDto.getNombre());
        verify(listaReproduccionRepository, times(1)).save(any(ListaReproduccion.class));
    }
    
    @Test
    void crearListaReproduccion_nameAlreadyExists_throwsBadRequestException() {
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Existente", "Desc", Collections.emptySet());
        when(listaReproduccionRepository.existsByNombre("Existente")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> listaReproduccionService.crearListaReproduccion(requestDto));
        verify(listaReproduccionRepository, never()).save(any(ListaReproduccion.class));
    }

    @Test
    void crearListaReproduccion_withExistingSongs_success() {
        Set<Long> cancionIds = new HashSet<>(Arrays.asList(1L, 2L));
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Lista con Canciones", "Descripción", cancionIds);
        
        Cancion cancion1 = new Cancion(1L, "Titulo1", "Artista1", "Album1", "2000", "Pop", new HashSet<>());
        Cancion cancion2 = new Cancion(2L, "Titulo2", "Artista2", "Album2", "2001", "Rock", new HashSet<>());
        
        when(listaReproduccionRepository.existsByNombre(anyString())).thenReturn(false);
        when(cancionRepository.findById(1L)).thenReturn(Optional.of(cancion1));
        when(cancionRepository.findById(2L)).thenReturn(Optional.of(cancion2));
        
        ListaReproduccion savedPlaylist = new ListaReproduccion(1L, "Lista con Canciones", "Descripción", new HashSet<>(Arrays.asList(cancion1, cancion2)));
        when(listaReproduccionRepository.save(any(ListaReproduccion.class))).thenReturn(savedPlaylist);

        ListaReproduccionResponseDto responseDto = listaReproduccionService.crearListaReproduccion(requestDto);

        assertNotNull(responseDto);
        assertEquals("Lista con Canciones", responseDto.getNombre());
        assertEquals(2, responseDto.getCanciones().size());
        verify(listaReproduccionRepository, times(1)).save(any(ListaReproduccion.class));
        verify(cancionRepository, times(1)).findById(1L);
        verify(cancionRepository, times(1)).findById(2L);
    }

    @Test
    void crearListaReproduccion_withNonExistingSong_throwsResourceNotFoundException() {
        Set<Long> cancionIds = new HashSet<>(Arrays.asList(1L, 6L)); // 6L no existe
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Lista con Canciones Invalidas", "Descripción", cancionIds);
        
        Cancion cancion1 = new Cancion(1L, "Titulo1", "Artista1", "Album1", "2000", "Pop", new HashSet<>());
        
        when(listaReproduccionRepository.existsByNombre(anyString())).thenReturn(false);
        when(cancionRepository.findById(1L)).thenReturn(Optional.of(cancion1));
        when(cancionRepository.findById(6L)).thenReturn(Optional.empty()); // Simula que la canción 6 no existe

        assertThrows(ResourceNotFoundException.class, () -> listaReproduccionService.crearListaReproduccion(requestDto));
        verify(listaReproduccionRepository, never()).save(any(ListaReproduccion.class));
        verify(cancionRepository, times(1)).findById(1L);
        verify(cancionRepository, times(1)).findById(6L);
    }

    @Test
    void crearListaReproduccionConCancion_success() {
        Long songId = 1L;
        Cancion cancion = new Cancion(songId, "Titulo", "Artista", "Album", "2020", "Pop", new HashSet<>());
        
        when(listaReproduccionRepository.existsByNombre(anyString())).thenReturn(false);
        when(cancionRepository.findById(songId)).thenReturn(Optional.of(cancion));
        
        ListaReproduccion savedPlaylist = new ListaReproduccion(1L, "Nueva Lista", "Desc", new HashSet<>(Collections.singletonList(cancion)));
        when(listaReproduccionRepository.save(any(ListaReproduccion.class))).thenReturn(savedPlaylist);

        ListaReproduccionResponseDto responseDto = listaReproduccionService.crearListaReproduccionConCancion("Nueva Lista", "Desc", songId);

        assertNotNull(responseDto);
        assertEquals("Nueva Lista", responseDto.getNombre());
        assertEquals(1, responseDto.getCanciones().size());
        verify(listaReproduccionRepository, times(1)).save(any(ListaReproduccion.class));
        verify(cancionRepository, times(1)).findById(songId);
    }

    @Test
    void crearListaReproduccionConCancion_playlistNameAlreadyExists_throwsBadRequestException() {
        Long songId = 1L;
        when(listaReproduccionRepository.existsByNombre("Existente")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> listaReproduccionService.crearListaReproduccionConCancion("Existente", "Desc", songId));
        verify(listaReproduccionRepository, never()).save(any(ListaReproduccion.class));
        verify(cancionRepository, never()).findById(anyLong());
    }

    @Test
    void crearListaReproduccionConCancion_songNotFound_throwsResourceNotFoundException() {
        Long songId = 99L;
        when(listaReproduccionRepository.existsByNombre(anyString())).thenReturn(false);
        when(cancionRepository.findById(songId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listaReproduccionService.crearListaReproduccionConCancion("Nueva Lista", "Desc", songId));
        verify(listaReproduccionRepository, never()).save(any(ListaReproduccion.class));
        verify(cancionRepository, times(1)).findById(songId);
    }

    @Test
    void obtenerTodasLasListasReproduccion_returnsListOfPlaylists() {
        ListaReproduccion playlist1 = new ListaReproduccion(1L, "Lista1", "Desc1", new HashSet<>());
        ListaReproduccion playlist2 = new ListaReproduccion(2L, "Lista2", "Desc2", new HashSet<>());
        List<ListaReproduccion> playlists = Arrays.asList(playlist1, playlist2);

        when(listaReproduccionRepository.findAll()).thenReturn(playlists);

        List<ListaReproduccionResponseDto> result = listaReproduccionService.obtenerTodasLasListasReproduccion();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Lista1", result.get(0).getNombre());
        assertEquals("Lista2", result.get(1).getNombre());
        verify(listaReproduccionRepository, times(1)).findAll();
    }

    @Test
    void obtenerListaReproduccionPorNombre_found() {
        ListaReproduccion playlist = new ListaReproduccion(1L, "Buscada", "Desc", new HashSet<>());
        when(listaReproduccionRepository.findByNombre("Buscada")).thenReturn(Optional.of(playlist));

        ListaReproduccionResponseDto responseDto = listaReproduccionService.obtenerListaReproduccionPorNombre("Buscada");

        assertNotNull(responseDto);
        assertEquals("Buscada", responseDto.getNombre());
        verify(listaReproduccionRepository, times(1)).findByNombre("Buscada");
    }

    @Test
    void obtenerListaReproduccionPorNombre_notFound_throwsResourceNotFoundException() {
        when(listaReproduccionRepository.findByNombre("NoExiste")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> listaReproduccionService.obtenerListaReproduccionPorNombre("NoExiste"));
        verify(listaReproduccionRepository, times(1)).findByNombre("NoExiste");
    }
    
    @Test
    void obtenerListaReproduccionPorId_found() {
        ListaReproduccion playlist = new ListaReproduccion(1L, "BuscadaPorId", "Desc", new HashSet<>());
        when(listaReproduccionRepository.findById(1L)).thenReturn(Optional.of(playlist));

        ListaReproduccionResponseDto responseDto = listaReproduccionService.obtenerListaReproduccionPorId(1L);

        assertNotNull(responseDto);
        assertEquals("BuscadaPorId", responseDto.getNombre());
        verify(listaReproduccionRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerListaReproduccionPorId_notFound_throwsResourceNotFoundException() {
        when(listaReproduccionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> listaReproduccionService.obtenerListaReproduccionPorId(99L));
        verify(listaReproduccionRepository, times(1)).findById(99L);
    }

    @Test
    void actualizarListaReproduccion_success() {
        Long playlistId = 1L;
        ListaReproduccion existingPlaylist = new ListaReproduccion(playlistId, "Original", "Desc Original", new HashSet<>());
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Actualizada", "Nueva Desc", Collections.emptySet());

        when(listaReproduccionRepository.findById(playlistId)).thenReturn(Optional.of(existingPlaylist));
        when(listaReproduccionRepository.existsByNombre("Actualizada")).thenReturn(false);
        when(listaReproduccionRepository.save(any(ListaReproduccion.class))).thenReturn(new ListaReproduccion(playlistId, "Actualizada", "Nueva Desc", new HashSet<>()));

        ListaReproduccionResponseDto responseDto = listaReproduccionService.actualizarListaReproduccion(playlistId, requestDto);

        assertNotNull(responseDto);
        assertEquals("Actualizada", responseDto.getNombre());
        assertEquals("Nueva Desc", responseDto.getDescripcion());
        verify(listaReproduccionRepository, times(1)).findById(playlistId);
        verify(listaReproduccionRepository, times(1)).existsByNombre("Actualizada");
        verify(listaReproduccionRepository, times(1)).save(any(ListaReproduccion.class));
    }

    @Test
    void actualizarListaReproduccion_notFound_throwsResourceNotFoundException() {
        Long playlistId = 99L;
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Actualizada", "Nueva Desc", Collections.emptySet());

        when(listaReproduccionRepository.findById(playlistId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listaReproduccionService.actualizarListaReproduccion(playlistId, requestDto));
        verify(listaReproduccionRepository, times(1)).findById(playlistId);
        verify(listaReproduccionRepository, never()).existsByNombre(anyString());
        verify(listaReproduccionRepository, never()).save(any(ListaReproduccion.class));
    }

    @Test
    void actualizarListaReproduccion_nameAlreadyExistsInOtherPlaylist_throwsBadRequestException() {
        Long playlistId = 1L;
        ListaReproduccion existingPlaylist = new ListaReproduccion(playlistId, "Original", "Desc Original", new HashSet<>());
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("NombreExistente", "Nueva Desc", Collections.emptySet());

        when(listaReproduccionRepository.findById(playlistId)).thenReturn(Optional.of(existingPlaylist));
        when(listaReproduccionRepository.existsByNombre("NombreExistente")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> listaReproduccionService.actualizarListaReproduccion(playlistId, requestDto));
        verify(listaReproduccionRepository, times(1)).findById(playlistId);
        verify(listaReproduccionRepository, times(1)).existsByNombre("NombreExistente");
        verify(listaReproduccionRepository, never()).save(any(ListaReproduccion.class));
    }

    @Test
    void actualizarListaReproduccion_withExistingAndNonExistingSongs_throwsResourceNotFoundException() {
        Long playlistId = 1L;
        ListaReproduccion existingPlaylist = new ListaReproduccion(playlistId, "Original", "Desc Original", new HashSet<>());
        Set<Long> cancionIds = new HashSet<>(Arrays.asList(1L, 99L)); // 99L no existe
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Actualizada", "Nueva Desc", cancionIds);

        Cancion cancion1 = new Cancion(1L, "Titulo1", "Artista1", "Album1", "2000", "Pop", new HashSet<>());

        when(listaReproduccionRepository.findById(playlistId)).thenReturn(Optional.of(existingPlaylist));
        when(listaReproduccionRepository.existsByNombre(anyString())).thenReturn(false);
        when(cancionRepository.findById(1L)).thenReturn(Optional.of(cancion1));
        when(cancionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listaReproduccionService.actualizarListaReproduccion(playlistId, requestDto));
        verify(listaReproduccionRepository, never()).save(any(ListaReproduccion.class));
        verify(cancionRepository, times(1)).findById(1L);
        verify(cancionRepository, times(1)).findById(99L);
    }

    @Test
    void eliminarListaReproduccionPorNombre_success() {
        ListaReproduccion playlist = new ListaReproduccion(1L, "A Borrar", "Desc", new HashSet<>());
        when(listaReproduccionRepository.findByNombre("A Borrar")).thenReturn(Optional.of(playlist));
        doNothing().when(listaReproduccionRepository).delete(playlist);

        listaReproduccionService.eliminarListaReproduccionPorNombre("A Borrar");

        verify(listaReproduccionRepository, times(1)).findByNombre("A Borrar");
        verify(listaReproduccionRepository, times(1)).delete(playlist);
    }

    @Test
    void eliminarListaReproduccionPorNombre_notFound_throwsResourceNotFoundException() {
        when(listaReproduccionRepository.findByNombre("NoExiste")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listaReproduccionService.eliminarListaReproduccionPorNombre("NoExiste"));
        verify(listaReproduccionRepository, times(1)).findByNombre("NoExiste");
        verify(listaReproduccionRepository, never()).delete(any(ListaReproduccion.class));
    }

    @Test
    void eliminarListaReproduccionPorId_success() {
        Long playlistId = 1L;
        ListaReproduccion playlist = new ListaReproduccion(playlistId, "A Borrar Por Id", "Desc", new HashSet<>());
        when(listaReproduccionRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        doNothing().when(listaReproduccionRepository).delete(playlist);

        listaReproduccionService.eliminarListaReproduccionPorId(playlistId);

        verify(listaReproduccionRepository, times(1)).findById(playlistId);
        verify(listaReproduccionRepository, times(1)).delete(playlist);
    }

    @Test
    void eliminarListaReproduccionPorId_notFound_throwsResourceNotFoundException() {
        Long playlistId = 99L;
        when(listaReproduccionRepository.findById(playlistId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> listaReproduccionService.eliminarListaReproduccionPorId(playlistId));
        verify(listaReproduccionRepository, times(1)).findById(playlistId);
        verify(listaReproduccionRepository, never()).delete(any(ListaReproduccion.class));
    }
}
