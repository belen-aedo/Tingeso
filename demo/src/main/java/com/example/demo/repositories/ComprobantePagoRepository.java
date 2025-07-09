package com.example.demo.repositories;

import com.example.demo.entities.ClienteEntity;
import com.example.demo.entities.ComprobantePagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ComprobantePagoRepository extends JpaRepository<ComprobantePagoEntity, Long> {
    Optional<ComprobantePagoEntity> findByReservaIdReserva(Long reservaId);
    List<ComprobantePagoEntity> findByCliente(ClienteEntity cliente);
    List<ComprobantePagoEntity> findAll();
}