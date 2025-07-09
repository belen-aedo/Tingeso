package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.math.BigDecimal;



@Entity
@Table(name = "comprobantes_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ComprobantePagoEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "clientes_id", nullable = false)
        @JsonIgnoreProperties({"comprobantes", "reservas", "hibernateLazyInitializer", "handler"})
        private ClienteEntity cliente;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "reserva_id", nullable = true)
        @JsonIgnoreProperties({"comprobante", "cliente", "hibernateLazyInitializer", "handler"})
        private ReservaEntity reserva;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "tarifa_id", nullable = false)
        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
        private TarifaEntity tarifa;
        //de la reserva obtendremos el tiempo de reserva; el cual tendremos que buscar
        //en la tarifa para obtener el precio base por el tiempo de reserva
        // tiempo maxim y los datos del número de vueltas

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