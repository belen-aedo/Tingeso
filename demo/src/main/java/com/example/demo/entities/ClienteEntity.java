package com.example.demo.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEntity {

    @Id
    @Column(unique = true, nullable = false)
    private String rut;

    private String nombre;
    private String email;
    private int visitasMes;
    private LocalDate fechaCumple;
    private int descuentoAplicable;


}