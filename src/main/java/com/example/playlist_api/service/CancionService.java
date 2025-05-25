package com.example.playlist_api.service;

import com.example.playlist_api.dto.CancionDto;
import com.example.playlist_api.entity.Cancion;
import com.example.playlist_api.exception.ResourceNotFoundException;
import com.example.playlist_api.repository.CancionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CancionService {

    private final CancionRepository cancionRepository;

    @Autowired
    public CancionService(CancionRepository cancionRepository) {
        this.cancionRepository = cancionRepository;
    }

    @Transactional
    public CancionDto crearCancion(CancionDto cancionDto) {
        Cancion cancion = mapToEntity(cancionDto);
        Cancion savedCancion = cancionRepository.save(cancion);
        return mapToDto(savedCancion);
    }

    @Transactional(readOnly = true)
    public List<CancionDto> obtenerTodasLasCanciones() {
        return cancionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CancionDto obtenerCancionPorId(Long id) {
        Cancion cancion = cancionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cancion", "id", id.toString()));
        return mapToDto(cancion);
    }

    @Transactional
    public CancionDto actualizarCancion(Long id, CancionDto cancionDto) {
        Cancion existingCancion = cancionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cancion", "id", id.toString()));

        existingCancion.setTitulo(cancionDto.getTitulo());
        existingCancion.setArtista(cancionDto.getArtista());
        existingCancion.setAlbum(cancionDto.getAlbum());
        existingCancion.setAnno(cancionDto.getAnno());
        existingCancion.setGenero(cancionDto.getGenero());

        Cancion updatedCancion = cancionRepository.save(existingCancion);
        return mapToDto(updatedCancion);
    }

    @Transactional
    public void eliminarCancion(Long id) {
        if (!cancionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cancion", "id", id.toString());
        }
        cancionRepository.deleteById(id);
    }

    private CancionDto mapToDto(Cancion cancion) {
        return new CancionDto(
                cancion.getId(),
                cancion.getTitulo(),
                cancion.getArtista(),
                cancion.getAlbum(),
                cancion.getAnno(),
                cancion.getGenero()
        );
    }

    private Cancion mapToEntity(CancionDto cancionDto) {
        Cancion cancion = new Cancion();
        cancion.setTitulo(cancionDto.getTitulo());
        cancion.setArtista(cancionDto.getArtista());
        cancion.setAlbum(cancionDto.getAlbum());
        cancion.setAnno(cancionDto.getAnno());
        cancion.setGenero(cancionDto.getGenero());
        return cancion;
    }
}
