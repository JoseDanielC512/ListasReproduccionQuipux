package com.example.playlist_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "canciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título no puede estar vacío ni ser nulo.")
    @Column(nullable = false)
    private String titulo;

    @NotBlank(message = "El artista no puede estar vacío ni ser nulo.")
    @Column(nullable = false)
    private String artista;

    @NotBlank(message = "El álbum no puede estar vacío ni ser nulo.")
    @Column(nullable = false)
    private String album;

    @NotBlank(message = "El año no puede estar vacío ni ser nulo.")
    @Column(nullable = false)
    private String anno;

    @NotBlank(message = "El género no puede estar vacío ni ser nulo.")
    @Column(nullable = false)
    private String genero;

    @ManyToMany(mappedBy = "canciones")
    private Set<ListaReproduccion> listasReproduccion = new HashSet<>();

    @Override
    public String toString() {
        return "Cancion{" +
               "id=" + id +
               ", titulo='" + titulo + '\'' +
               ", artista='" + artista + '\'' +
               ", album='" + album + '\'' +
               ", anno='" + anno + '\'' +
               ", genero='" + genero + '\'' +
               '}';
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cancion cancion = (Cancion) o;
        return id != null && id.equals(cancion.id);
    }
}
