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
    public List<KartEntity> getAllKarts() {
        return kartRepository.findAll();
    }
    public Optional<KartEntity> getKartByCodigo(String codigo) {
        return kartRepository.findByCodigo(codigo);
    }
    public List<KartEntity> getKartsByEstado(String estado) {
        return kartRepository.findByEstado(estado);
    }
    public List<KartEntity> getKartsDisponibles() {
        return kartRepository.findKartsDisponibles();
    }
    public Integer contarKartsDisponibles() {
        return kartRepository.contarKartsDisponibles();
    }
    public KartEntity saveKart(KartEntity kart) {
        return kartRepository.save(kart);
    }
    public void deleteKart(String id) {
        kartRepository.deleteById(id);
    }
    /**
     * Cambia el estado de un kart
     * @param kartId ID del kart
     * @param Nuevo Estado Nuevo estado ("Disponible", "En uso", "En
    mantenimiento")
     * @return El kart actualizado o empty si no existe
     */
    public Optional<KartEntity> cambiarEstadoKart(String kartId, String estado) { // Cambiado a String
        Optional<KartEntity> kartOpt = kartRepository.findById(kartId);
        if (kartOpt.isPresent()) {
            KartEntity kart = kartOpt.get();
            kart.setEstado(estado);
            return Optional.of(kartRepository.save(kart));
        }
        return Optional.empty();
    }
    /**
     * Inicializa los 15 karts del sistema según el enunciado
     */
    public void inicializarKarts() {
        // Verificar si ya existen karts
        if (kartRepository.count() > 0) {
            return;
        }
        // Crear los 15 karts según lo especificado
        for (int i = 1; i <= 15; i++) {
            KartEntity kart = new KartEntity();
            kart.setCodigo("K" + String.format("%03d", i));
            kart.setEstado("Disponible");
            kart.setModelo("Sodikart RT8");
            kartRepository.save(kart);
        }
    }
}