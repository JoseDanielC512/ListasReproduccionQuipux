package com.example.playlist_api.controller;

import com.example.playlist_api.dto.CancionDto;
import com.example.playlist_api.exception.ResourceNotFoundException;
import com.example.playlist_api.service.CancionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CancionController.class)
class CancionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CancionService cancionService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearCancion_success() throws Exception {
        CancionDto requestDto = new CancionDto(null, "Titulo Test", "Artista Test", "Album Test", "2023", "Pop");
        CancionDto responseDto = new CancionDto(1L, "Titulo Test", "Artista Test", "Album Test", "2023", "Pop");

        when(cancionService.crearCancion(any(CancionDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/canciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/canciones/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Titulo Test"));
        
        verify(cancionService, times(1)).crearCancion(any(CancionDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void crearCancion_asUser_returnsForbidden() throws Exception {
        CancionDto requestDto = new CancionDto(null, "Titulo Test", "Artista Test", "Album Test", "2023", "Pop");

        mockMvc.perform(post("/api/canciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
        
        verify(cancionService, never()).crearCancion(any(CancionDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void obtenerTodasLasCanciones_success() throws Exception {
        List<CancionDto> canciones = Arrays.asList(
            new CancionDto(1L, "Titulo1", "Artista1", "Album1", "2000", "Pop"),
            new CancionDto(2L, "Titulo2", "Artista2", "Album2", "2001", "Rock")
        );

        when(cancionService.obtenerTodasLasCanciones()).thenReturn(canciones);

        mockMvc.perform(get("/api/canciones")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Titulo1"))
                .andExpect(jsonPath("$[1].titulo").value("Titulo2"));
        
        verify(cancionService, times(1)).obtenerTodasLasCanciones();
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void obtenerCancionPorId_found() throws Exception {
        Long id = 1L;
        CancionDto responseDto = new CancionDto(id, "Titulo Test", "Artista Test", "Album Test", "2023", "Pop");

        when(cancionService.obtenerCancionPorId(id)).thenReturn(responseDto);

        mockMvc.perform(get("/api/canciones/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titulo").value("Titulo Test"));
        
        verify(cancionService, times(1)).obtenerCancionPorId(id);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void obtenerCancionPorId_notFound_returnsNotFound() throws Exception {
        Long id = 99L;
        when(cancionService.obtenerCancionPorId(id)).thenThrow(new ResourceNotFoundException("Cancion", "id", id.toString()));

        mockMvc.perform(get("/api/canciones/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
        
        verify(cancionService, times(1)).obtenerCancionPorId(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizarCancion_success() throws Exception {
        Long id = 1L;
        CancionDto requestDto = new CancionDto(id, "Titulo Actualizado", "Artista Actualizado", "Album Actualizado", "2024", "Jazz");
        CancionDto responseDto = new CancionDto(id, "Titulo Actualizado", "Artista Actualizado", "Album Actualizado", "2024", "Jazz");

        when(cancionService.actualizarCancion(eq(id), any(CancionDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/canciones/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titulo").value("Titulo Actualizado"));
        
        verify(cancionService, times(1)).actualizarCancion(eq(id), any(CancionDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizarCancion_notFound_returnsNotFound() throws Exception {
        Long id = 99L;
        CancionDto requestDto = new CancionDto(id, "Titulo Actualizado", "Artista Actualizado", "Album Actualizado", "2024", "Jazz");

        when(cancionService.actualizarCancion(eq(id), any(CancionDto.class))).thenThrow(new ResourceNotFoundException("Cancion", "id", id.toString()));

        mockMvc.perform(put("/api/canciones/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
        
        verify(cancionService, times(1)).actualizarCancion(eq(id), any(CancionDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void actualizarCancion_asUser_returnsForbidden() throws Exception {
        Long id = 1L;
        CancionDto requestDto = new CancionDto(id, "Titulo Actualizado", "Artista Actualizado", "Album Actualizado", "2024", "Jazz");

        mockMvc.perform(put("/api/canciones/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
        
        verify(cancionService, never()).actualizarCancion(anyLong(), any(CancionDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarCancion_success() throws Exception {
        Long id = 1L;
        doNothing().when(cancionService).eliminarCancion(id);

        mockMvc.perform(delete("/api/canciones/{id}", id))
                .andExpect(status().isNoContent());
        
        verify(cancionService, times(1)).eliminarCancion(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarCancion_notFound_returnsNotFound() throws Exception {
        Long id = 99L;
        doThrow(new ResourceNotFoundException("Cancion", "id", id.toString())).when(cancionService).eliminarCancion(id);

        mockMvc.perform(delete("/api/canciones/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
        
        verify(cancionService, times(1)).eliminarCancion(id);
    }

    @Test
    @WithMockUser(roles = "USER")
    void eliminarCancion_asUser_returnsForbidden() throws Exception {
        Long id = 1L;
        mockMvc.perform(delete("/api/canciones/{id}", id))
                .andExpect(status().isForbidden());
        
        verify(cancionService, never()).eliminarCancion(anyLong());
    }
}
