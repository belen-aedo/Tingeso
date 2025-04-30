package com.example.demo.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private String tipoReporte;  // "PorVueltas", "PorPersonas"
     //mes generado
    private LocalDate mesGenerado;
    private double ingresoTotal;

    // Para reportes por vueltas
    private Integer numeroVueltas;
    private Integer tiempoMaximo;

    // Para reportes por personas
    private Integer minPersonas;
    private Integer maxPersonas;

}