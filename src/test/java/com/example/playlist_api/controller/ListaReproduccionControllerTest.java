package com.example.playlist_api.controller;


import com.example.playlist_api.dto.ListaReproduccionRequestDto;
import com.example.playlist_api.dto.ListaReproduccionResponseDto;
import com.example.playlist_api.dto.CancionDto;
import com.example.playlist_api.exception.BadRequestException;
import com.example.playlist_api.exception.ResourceNotFoundException;
import com.example.playlist_api.service.ListaReproduccionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.example.playlist_api.security.JwtAuthenticationEntryPoint;
import com.example.playlist_api.security.JwtRequestFilter;
import com.example.playlist_api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = ListaReproduccionController.class)
class ListaReproduccionControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;


    @MockBean
    private ListaReproduccionService listaReproduccionService;


    @MockBean
    private UserDetailsService userDetailsService;


    @MockBean
    private JwtUtil jwtUtil;


    @MockBean
    private JwtRequestFilter jwtRequestFilter;


    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    @Test
    @WithMockUser(roles = "ADMIN")
    void crearListaReproduccion_success() throws Exception {
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Mi Lista", "Descripción", Collections.emptySet());
        ListaReproduccionResponseDto responseDto = new ListaReproduccionResponseDto(1L, "Mi Lista", "Descripción", Collections.emptySet());

        when(listaReproduccionService.crearListaReproduccion(any(ListaReproduccionRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/listas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/listas/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Mi Lista"));
        
        verify(listaReproduccionService, times(1)).crearListaReproduccion(any(ListaReproduccionRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearListaReproduccion_nameAlreadyExists_returnsBadRequest() throws Exception {
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Existente", "Desc", Collections.emptySet());

        when(listaReproduccionService.crearListaReproduccion(any(ListaReproduccionRequestDto.class)))
                .thenThrow(new BadRequestException("Ya existe una lista de reproducción con el nombre: Existente"));

        mockMvc.perform(post("/api/listas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
        
        verify(listaReproduccionService, times(1)).crearListaReproduccion(any(ListaReproduccionRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void crearListaReproduccion_asUser_returnsForbidden() throws Exception {
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Mi Lista", "Descripción", Collections.emptySet());

        mockMvc.perform(post("/api/listas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
        
        verify(listaReproduccionService, never()).crearListaReproduccion(any(ListaReproduccionRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearListaReproduccionConCancionInicial_success() throws Exception {
        Long songId = 1L;
        ListaReproduccionController.NombreDescripcionListaDto requestDetails = new ListaReproduccionController.NombreDescripcionListaDto();
        requestDetails.setNombre("Lista con Cancion");
        requestDetails.setDescripcion("Desc");

        ListaReproduccionResponseDto responseDto = new ListaReproduccionResponseDto(1L, "Lista con Cancion", "Desc", new HashSet<>(Collections.singletonList(new CancionDto(songId, "Titulo", "Artista", "Album", "2020", "Pop"))));

        when(listaReproduccionService.crearListaReproduccionConCancion(anyString(), anyString(), eq(songId))).thenReturn(responseDto);

        mockMvc.perform(post("/api/listas/con-cancion-inicial/{idCancion}", songId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDetails)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/listas/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Lista con Cancion"));
        
        verify(listaReproduccionService, times(1)).crearListaReproduccionConCancion(anyString(), anyString(), eq(songId));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void obtenerTodasLasListasReproduccion_success() throws Exception {
        List<ListaReproduccionResponseDto> listas = Arrays.asList(
            new ListaReproduccionResponseDto(1L, "Lista1", "Desc1", Collections.emptySet()),
            new ListaReproduccionResponseDto(2L, "Lista2", "Desc2", Collections.emptySet())
        );

        when(listaReproduccionService.obtenerTodasLasListasReproduccion()).thenReturn(listas);

        mockMvc.perform(get("/api/listas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Lista1"))
                .andExpect(jsonPath("$[1].nombre").value("Lista2"));
        
        verify(listaReproduccionService, times(1)).obtenerTodasLasListasReproduccion();
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void obtenerListaReproduccionPorNombre_found() throws Exception {
        String nombre = "Mi Lista";
        ListaReproduccionResponseDto responseDto = new ListaReproduccionResponseDto(1L, nombre, "Desc", Collections.emptySet());

        when(listaReproduccionService.obtenerListaReproduccionPorNombre(nombre)).thenReturn(responseDto);

        mockMvc.perform(get("/api/listas/porNombre/{nombreLista}", nombre)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nombre));
        
        verify(listaReproduccionService, times(1)).obtenerListaReproduccionPorNombre(nombre);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void obtenerListaReproduccionPorNombre_notFound_returnsNotFound() throws Exception {
        String nombre = "NoExiste";
        when(listaReproduccionService.obtenerListaReproduccionPorNombre(nombre)).thenThrow(new ResourceNotFoundException("ListaReproduccion", "nombre", nombre));

        mockMvc.perform(get("/api/listas/porNombre/{nombreLista}", nombre)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
        
        verify(listaReproduccionService, times(1)).obtenerListaReproduccionPorNombre(nombre);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void obtenerListaReproduccionPorId_found() throws Exception {
        Long id = 1L;
        ListaReproduccionResponseDto responseDto = new ListaReproduccionResponseDto(id, "Mi Lista", "Desc", Collections.emptySet());

        when(listaReproduccionService.obtenerListaReproduccionPorId(id)).thenReturn(responseDto);

        mockMvc.perform(get("/api/listas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        
        verify(listaReproduccionService, times(1)).obtenerListaReproduccionPorId(id);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void obtenerListaReproduccionPorId_notFound_returnsNotFound() throws Exception {
        Long id = 99L;
        when(listaReproduccionService.obtenerListaReproduccionPorId(id)).thenThrow(new ResourceNotFoundException("ListaReproduccion", "id", id.toString()));

        mockMvc.perform(get("/api/listas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
        
        verify(listaReproduccionService, times(1)).obtenerListaReproduccionPorId(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizarListaReproduccion_success() throws Exception {
        Long id = 1L;
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Lista Actualizada", "Nueva Desc", Collections.emptySet());
        ListaReproduccionResponseDto responseDto = new ListaReproduccionResponseDto(id, "Lista Actualizada", "Nueva Desc", Collections.emptySet());

        when(listaReproduccionService.actualizarListaReproduccion(eq(id), any(ListaReproduccionRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/listas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Lista Actualizada"));
        
        verify(listaReproduccionService, times(1)).actualizarListaReproduccion(eq(id), any(ListaReproduccionRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizarListaReproduccion_notFound_returnsNotFound() throws Exception {
        Long id = 99L;
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Lista Actualizada", "Nueva Desc", Collections.emptySet());

        when(listaReproduccionService.actualizarListaReproduccion(eq(id), any(ListaReproduccionRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("ListaReproduccion", "id", id.toString()));

        mockMvc.perform(put("/api/listas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
        
        verify(listaReproduccionService, times(1)).actualizarListaReproduccion(eq(id), any(ListaReproduccionRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void actualizarListaReproduccion_asUser_returnsForbidden() throws Exception {
        Long id = 1L;
        ListaReproduccionRequestDto requestDto = new ListaReproduccionRequestDto("Lista Actualizada", "Nueva Desc", Collections.emptySet());

        mockMvc.perform(put("/api/listas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
        
        verify(listaReproduccionService, never()).actualizarListaReproduccion(anyLong(), any(ListaReproduccionRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarListaReproduccionPorNombre_success() throws Exception {
        String nombre = "Lista a Eliminar";
        doNothing().when(listaReproduccionService).eliminarListaReproduccionPorNombre(nombre);

        mockMvc.perform(delete("/api/listas/porNombre/{nombreLista}", nombre))
                .andExpect(status().isNoContent());
        
        verify(listaReproduccionService, times(1)).eliminarListaReproduccionPorNombre(nombre);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarListaReproduccionPorNombre_notFound_returnsNotFound() throws Exception {
        String nombre = "NoExiste";
        doThrow(new ResourceNotFoundException("ListaReproduccion", "nombre", nombre)).when(listaReproduccionService).eliminarListaReproduccionPorNombre(nombre);

        mockMvc.perform(delete("/api/listas/porNombre/{nombreLista}", nombre))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
        
        verify(listaReproduccionService, times(1)).eliminarListaReproduccionPorNombre(nombre);
    }

    @Test
    @WithMockUser(roles = "USER")
    void eliminarListaReproduccionPorNombre_asUser_returnsForbidden() throws Exception {
        String nombre = "Lista a Eliminar";
        mockMvc.perform(delete("/api/listas/porNombre/{nombreLista}", nombre))
                .andExpect(status().isForbidden());
        
        verify(listaReproduccionService, never()).eliminarListaReproduccionPorNombre(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarListaReproduccionPorId_success() throws Exception {
        Long id = 1L;
        doNothing().when(listaReproduccionService).eliminarListaReproduccionPorId(id);

        mockMvc.perform(delete("/api/listas/{id}", id))
                .andExpect(status().isNoContent());
        
        verify(listaReproduccionService, times(1)).eliminarListaReproduccionPorId(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarListaReproduccionPorId_notFound_returnsNotFound() throws Exception {
        Long id = 99L;
        doThrow(new ResourceNotFoundException("ListaReproduccion", "id", id.toString())).when(listaReproduccionService).eliminarListaReproduccionPorId(id);

        mockMvc.perform(delete("/api/listas/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
        
        verify(listaReproduccionService, times(1)).eliminarListaReproduccionPorId(id);
    }

    @Test
    @WithMockUser(roles = "USER")
    void eliminarListaReproduccionPorId_asUser_returnsForbidden() throws Exception {
        Long id = 1L;
        mockMvc.perform(delete("/api/listas/{id}", id))
                .andExpect(status().isForbidden());
        
        verify(listaReproduccionService, never()).eliminarListaReproduccionPorId(anyLong());
    }
}
