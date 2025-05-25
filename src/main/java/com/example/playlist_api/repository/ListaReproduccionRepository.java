package com.example.playlist_api.repository;

import com.example.playlist_api.entity.ListaReproduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ListaReproduccionRepository extends JpaRepository<ListaReproduccion, Long> {
    Optional<ListaReproduccion> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
