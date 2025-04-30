package com.example.demo.repositories;

import com.example.demo.entities.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {
    List<ReservaEntity> findByDiaReserva(LocalDate diaReserva);
    List<ReservaEntity> findByClienteRut(String rutCliente);
}