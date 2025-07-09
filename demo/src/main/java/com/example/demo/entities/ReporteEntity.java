package com.example.demo.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.LocalDate;

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
    private LocalDate fecha;     // Fecha específica del reporte
    private double ingresoTotal;

    // Para reportes por vueltas
    private Integer numeroVueltas;
    private Integer tiempoMaximo;

    // Para reportes por personas
    private Integer minPersonas;
    private Integer maxPersonas;

    // Descripción opcional para identificar mejor el reporte
    private String descripcion;
    @Setter
    private LocalDate mesGenerado;

}