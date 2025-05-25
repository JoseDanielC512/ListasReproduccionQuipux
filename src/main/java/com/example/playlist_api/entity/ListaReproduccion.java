package com.example.playlist_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "listas_reproduccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaReproduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la lista no puede estar vacío ni ser nulo.")
    @Column(nullable = false, unique = true)
    private String nombre;

    @Column
    private String descripcion;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "lista_reproduccion_canciones",
        joinColumns = @JoinColumn(name = "lista_reproduccion_id"),
        inverseJoinColumns = @JoinColumn(name = "cancion_id")
    )
    private Set<Cancion> canciones = new HashSet<>();

    public void addCancion(Cancion cancion) {
        this.canciones.add(cancion);
        cancion.getListasReproduccion().add(this);
    }

    public void removeCancion(Cancion cancion) {
        this.canciones.remove(cancion);
        cancion.getListasReproduccion().remove(this);
    }
    
    @Override
    public String toString() {
        return "ListaReproduccion{" +
               "id=" + id +
               ", nombre='" + nombre + '\'' +
               ", descripcion='" + descripcion + '\'' +
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
        ListaReproduccion listaReproduccion = (ListaReproduccion) o;
        return id != null && id.equals(listaReproduccion.id);
    }
}
