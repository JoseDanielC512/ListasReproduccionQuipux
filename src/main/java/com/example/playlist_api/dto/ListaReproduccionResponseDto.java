package com.example.playlist_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaReproduccionResponseDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private Set<CancionDto> canciones;
}
