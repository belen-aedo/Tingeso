package com.example.demo.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "karts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KartEntity {

    @Id
    private String codigo;  // K001, K002, etc.
    private String estado;  // "Disponible", "En uso", "En mantenimiento"
    private String modelo;  // Sodikart RT8


}