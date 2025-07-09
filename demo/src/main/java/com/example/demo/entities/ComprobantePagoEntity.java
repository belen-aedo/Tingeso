package com.example.demo.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "comprobantes_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobantePagoEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "clientes_id", nullable = false)
        //del cliente obtendremos el RUT que conectaremos con la reserva
        private ClienteEntity cliente;

        // Relación con la reserva asociada
        @ManyToOne
        @JoinColumn(name = "reserva_id", nullable = false)
        //al obtener la reserva con el RUT del cliente obtendremos todos los datos de esta
        private ReservaEntity reserva;

        // Relación con la tarifa aplicada
        @ManyToOne
        @JoinColumn(name = "tarifa_id", nullable = false)
        //de la reserva obtendremos el tiempo de reserva; el cual tendremos que buscar
        //en la tarifa para obtener el precio base por el tiempo de reserva
        // tiempo maxim y los datos del número de vueltas
        private TarifaEntity tarifa;

        //el descuento por grupo se calculará sumando los acompañantes más 1
        //depende de cuantas personas sean es el descuento aplicable
        private BigDecimal descuentoGrupo;

        //el descuento por cliente frecuente se calculará dependiendo de la cantidad de visitas
        //por cada uno de los integrantes del grupo, que acompañaran y el que reservo
        //si alguno tiene descuento se calculará cuanto es el monto a descontar y se sumara con los descuento de los demas
        private BigDecimal descuentoClienteFrecuente;

        //El cumpleaños se calculará dependiendo de la fecha de nacimiento del cliente y el dia de reserva
        //tengan un 50% de descuento especial. Este descuento funciona de la siguiente manera, en un grupo de 3 a 5
        //personas se aplica a una persona que cumple años, en un grupo de 6 a 10 personas se aplica
        //hasta 2 personas que cumplen años.
        private BigDecimal descuentoCumpleanos;
        //monto total numero de personas * precio base
        private BigDecimal montoBase;
        //monto total con descuento
        private BigDecimal montoFinal;
        // 19% * monto final
        private BigDecimal iva;
        //suma del iva más el monto final
        private BigDecimal montoTotalConIva;
}