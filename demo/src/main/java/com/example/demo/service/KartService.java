package com.example.demo.service;

import com.example.demo.entities.KartEntity;
import com.example.demo.repositories.KartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KartService {

    private final KartRepository kartRepository;

    @Autowired
    public KartService(KartRepository kartRepository) {
        this.kartRepository = kartRepository;
    }

    // Obtener todos los karts
    public List<KartEntity> getAllKarts() {
        return kartRepository.findAll();
    }

    // Buscar un kart por su código
    public Optional<KartEntity> getKartByCodigo(String codigo) {
        return kartRepository.findByCodigo(codigo);
    }

    // Obtener karts por estado
    public List<KartEntity> getKartsByEstado(String estado) {
        return kartRepository.findByEstado(estado);
    }

    // Obtener solo los karts disponibles
    public List<KartEntity> getKartsDisponibles() {
        return kartRepository.findKartsDisponibles();
    }

    // Contar los karts disponibles
    public Integer contarKartsDisponibles() {
        return kartRepository.contarKartsDisponibles();
    }

    // Guardar un nuevo kart
    public KartEntity saveKart(KartEntity kart) {
        return kartRepository.save(kart);
    }

    // ✅ CORREGIDO: Eliminar un kart usando el código como ID
    public void deleteKart(String codigo) {
        System.out.println("Intentando eliminar kart con código: " + codigo);
        if (!kartRepository.existsById(codigo)) {
            System.out.println("Kart no encontrado: " + codigo);
            throw new RuntimeException("Kart con código " + codigo + " no encontrado");
        }
        System.out.println("Eliminando kart: " + codigo);
        kartRepository.deleteById(codigo);
    }

    /**
     * Cambia el estado de un kart
     * @param codigo Código del kart
     * @param estado Nuevo estado ("Disponible", "En uso", "En mantenimiento")
     * @return El kart actualizado o vacío si no existe
     */
    public Optional<KartEntity> cambiarEstadoKart(String codigo, String estado) {
        System.out.println("Cambiando estado del kart " + codigo + " a: " + estado);
        Optional<KartEntity> kartOpt = kartRepository.findById(codigo);
        if (kartOpt.isPresent()) {
            KartEntity kart = kartOpt.get();
            kart.setEstado(estado);
            KartEntity kartActualizado = kartRepository.save(kart);
            System.out.println("Estado actualizado exitosamente");
            return Optional.of(kartActualizado);
        }
        System.out.println("Kart no encontrado: " + codigo);
        return Optional.empty();
    }

    /**
     * Inicializa los 15 karts del sistema según el enunciado
     */
    public void inicializarKarts() {
        if (kartRepository.count() > 0) {
            return;
        }

        for (int i = 1; i <= 15; i++) {
            KartEntity kart = new KartEntity();
            kart.setCodigo("K" + String.format("%03d", i));
            kart.setEstado("Disponible");
            kart.setModelo("Sodikart RT8");
            kartRepository.save(kart);
        }
    }
}