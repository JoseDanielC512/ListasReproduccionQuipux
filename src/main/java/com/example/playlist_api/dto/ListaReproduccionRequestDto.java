package com.example.playlist_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaReproduccionRequestDto {
    @NotBlank(message = "El nombre de la lista no puede estar vacío ni ser nulo.")
    private String nombre;

    private String descripcion;

    private Set<Long> cancionIds;
}
