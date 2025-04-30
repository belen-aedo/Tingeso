package com.example.demo.repositories;

import com.example.demo.entities.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, String> {

    Optional<ClienteEntity> findByEmail(String email);

    // Método renombrado para usar la nomenclatura estándar de Spring Data JPA
    List<ClienteEntity> findByVisitasMesBetween(int minVisitas, int maxVisitas);
}