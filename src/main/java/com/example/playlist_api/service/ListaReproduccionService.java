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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ListaReproduccionService {

    private final ListaReproduccionRepository listaReproduccionRepository;
    private final CancionRepository cancionRepository;

    @Autowired
    public ListaReproduccionService(ListaReproduccionRepository listaReproduccionRepository, CancionRepository cancionRepository) {
        this.listaReproduccionRepository = listaReproduccionRepository;
        this.cancionRepository = cancionRepository;
    }

    @Transactional
    public ListaReproduccionResponseDto crearListaReproduccion(ListaReproduccionRequestDto listaReproduccionRequestDto) {
        if (listaReproduccionRepository.existsByNombre(listaReproduccionRequestDto.getNombre())) {
            throw new BadRequestException("Ya existe una lista de reproducción con el nombre: " + listaReproduccionRequestDto.getNombre());
        }
        ListaReproduccion listaReproduccion = new ListaReproduccion();
        listaReproduccion.setNombre(listaReproduccionRequestDto.getNombre());
        listaReproduccion.setDescripcion(listaReproduccionRequestDto.getDescripcion());

        if (listaReproduccionRequestDto.getCancionIds() != null && !listaReproduccionRequestDto.getCancionIds().isEmpty()) {
            Set<Cancion> cancionesEncontradas = new HashSet<>();
            for (Long cancionId : listaReproduccionRequestDto.getCancionIds()) {
                Cancion cancion = cancionRepository.findById(cancionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cancion", "id", cancionId.toString()));
                cancionesEncontradas.add(cancion);
            }
            listaReproduccion.setCanciones(cancionesEncontradas);
        }
        
        ListaReproduccion savedListaReproduccion = listaReproduccionRepository.save(listaReproduccion);
        return mapToResponseDto(savedListaReproduccion);
    }
    
    @Transactional
    public ListaReproduccionResponseDto crearListaReproduccionConCancion(String nombreLista, String descripcionLista, Long idCancion) {
        if (listaReproduccionRepository.existsByNombre(nombreLista)) {
            throw new BadRequestException("Ya existe una lista de reproducción con el nombre: " + nombreLista);
        }
        Cancion cancion = cancionRepository.findById(idCancion)
                .orElseThrow(() -> new ResourceNotFoundException("Cancion", "id", idCancion.toString()));

        ListaReproduccion listaReproduccion = new ListaReproduccion();
        listaReproduccion.setNombre(nombreLista);
        listaReproduccion.setDescripcion(descripcionLista);
        listaReproduccion.addCancion(cancion);

        ListaReproduccion savedListaReproduccion = listaReproduccionRepository.save(listaReproduccion);
        return mapToResponseDto(savedListaReproduccion);
    }


    @Transactional(readOnly = true)
    public List<ListaReproduccionResponseDto> obtenerTodasLasListasReproduccion() {
        return listaReproduccionRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ListaReproduccionResponseDto obtenerListaReproduccionPorNombre(String nombre) {
        ListaReproduccion listaReproduccion = listaReproduccionRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("ListaReproduccion", "nombre", nombre));
        return mapToResponseDto(listaReproduccion);
    }
    
    @Transactional(readOnly = true)
    public ListaReproduccionResponseDto obtenerListaReproduccionPorId(Long id) {
        ListaReproduccion listaReproduccion = listaReproduccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ListaReproduccion", "id", id.toString()));
        return mapToResponseDto(listaReproduccion);
    }

    @Transactional
    public ListaReproduccionResponseDto actualizarListaReproduccion(Long id, ListaReproduccionRequestDto listaReproduccionRequestDto) {
        ListaReproduccion existingListaReproduccion = listaReproduccionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ListaReproduccion", "id", id.toString()));

        if (!existingListaReproduccion.getNombre().equals(listaReproduccionRequestDto.getNombre()) &&
            listaReproduccionRepository.existsByNombre(listaReproduccionRequestDto.getNombre())) {
            throw new BadRequestException("Ya existe otra lista de reproducción con el nombre: " + listaReproduccionRequestDto.getNombre());
        }

        existingListaReproduccion.setNombre(listaReproduccionRequestDto.getNombre());
        existingListaReproduccion.setDescripcion(listaReproduccionRequestDto.getDescripcion());

        existingListaReproduccion.getCanciones().clear(); // Limpiar canciones existentes
        if (listaReproduccionRequestDto.getCancionIds() != null && !listaReproduccionRequestDto.getCancionIds().isEmpty()) {
            Set<Cancion> cancionesParaActualizar = new HashSet<>();
            for (Long cancionId : listaReproduccionRequestDto.getCancionIds()) {
                Cancion cancion = cancionRepository.findById(cancionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cancion", "id", cancionId.toString()));
                cancionesParaActualizar.add(cancion);
            }
            existingListaReproduccion.setCanciones(cancionesParaActualizar);
        }

        ListaReproduccion updatedListaReproduccion = listaReproduccionRepository.save(existingListaReproduccion);
        return mapToResponseDto(updatedListaReproduccion);
    }


    @Transactional
    public void eliminarListaReproduccionPorNombre(String nombre) {
        ListaReproduccion listaReproduccion = listaReproduccionRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("ListaReproduccion", "nombre", nombre));
        listaReproduccionRepository.delete(listaReproduccion);
    }
    
    @Transactional
    public void eliminarListaReproduccionPorId(Long id) {
        ListaReproduccion listaReproduccion = listaReproduccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ListaReproduccion", "id", id.toString()));
        listaReproduccionRepository.delete(listaReproduccion);
    }

    private ListaReproduccionResponseDto mapToResponseDto(ListaReproduccion listaReproduccion) {
        Set<CancionDto> cancionDtos = listaReproduccion.getCanciones().stream()
                .map(cancion -> new CancionDto(cancion.getId(), cancion.getTitulo(), cancion.getArtista(), cancion.getAlbum(), cancion.getAnno(), cancion.getGenero()))
                .collect(Collectors.toSet());
        return new ListaReproduccionResponseDto(
                listaReproduccion.getId(),
                listaReproduccion.getNombre(),
                listaReproduccion.getDescripcion(),
                cancionDtos
        );
    }
}
