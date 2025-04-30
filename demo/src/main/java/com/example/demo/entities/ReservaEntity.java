package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReserva;

    // Relación con Cliente (rut)
    @ManyToOne
    private ClienteEntity cliente;

    private LocalDate diaReserva;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int tiempoReserva;

    // Lista de acompañantes (solo los ruts, no entidades completas)
    @ElementCollection
    @Column(name = "rut_acompanante")
    private List<String> acompanantes;
}
