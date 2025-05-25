package com.example.playlist_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancionDto {
    private Long id;

    @NotBlank(message = "El título no puede estar vacío ni ser nulo.")
    private String titulo;

    @NotBlank(message = "El artista no puede estar vacío ni ser nulo.")
    private String artista;

    @NotBlank(message = "El álbum no puede estar vacío ni ser nulo.")
    private String album;

    @NotBlank(message = "El año no puede estar vacío ni ser nulo.")
    private String anno;

    @NotBlank(message = "El género no puede estar vacío ni ser nulo.")
    private String genero;
}
