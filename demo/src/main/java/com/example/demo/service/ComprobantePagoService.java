package com.example.demo.service;

import com.example.demo.entities.*;
import com.example.demo.repositories.ComprobantePagoRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.transaction.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ComprobantePagoService {

    private final ComprobantePagoRepository comprobantePagoRepository;
    private final ClienteService clienteService;
    private final ReservaService reservaService;
    private final TarifaService tarifaService;
    private final JavaMailSender mailSender;

    private static final BigDecimal IVA_RATE = new BigDecimal("0.19");

    @Autowired
    public ComprobantePagoService(
            ComprobantePagoRepository comprobantePagoRepository,
            ClienteService clienteService,
            ReservaService reservaService,
            TarifaService tarifaService,
            JavaMailSender mailSender) {
        this.comprobantePagoRepository = comprobantePagoRepository;
        this.clienteService = clienteService;
        this.reservaService = reservaService;
        this.tarifaService = tarifaService;
        this.mailSender = mailSender;
    }
// Método para obtener todos los comprobantes de pago
    public List<ComprobantePagoEntity> getAllComprobantes() {
        return comprobantePagoRepository.findAll();
    }

    public Optional<ComprobantePagoEntity> getComprobanteById(Long id) {
        return comprobantePagoRepository.findById(id);
    }

    public Optional<ComprobantePagoEntity> getComprobanteByReservaId(Long reservaId) {
        return comprobantePagoRepository.findByReservaIdReserva(reservaId);
    }

    public List<ComprobantePagoEntity> getComprobantesByCliente(ClienteEntity cliente) {
        return comprobantePagoRepository.findByCliente(cliente);
    }

    @Transactional
    public ComprobantePagoEntity generarComprobantePago(Long reservaId) {
        // Obtener la reserva
        Optional<ReservaEntity> optReserva = reservaService.obtenerReservaPorId(reservaId);
        if (optReserva.isEmpty()) {
            throw new RuntimeException("No se encontró la reserva con ID: " + reservaId);
        }

        ReservaEntity reserva = optReserva.get();
        ClienteEntity cliente = reserva.getCliente();

        // Buscar la tarifa según el tiempo de reserva
        Optional<TarifaEntity> optTarifa =
                tarifaService.getTarifaById(obtenerTarifaIdPorTiempoReserva(reserva.getTiempoReserva()));
        if (optTarifa.isEmpty()) {
            throw new RuntimeException("No se encontró una tarifa para el tiempo de reserva: " +
                    reserva.getTiempoReserva() + " minutos");
        }

        TarifaEntity tarifa = optTarifa.get();

        // Calcular número total de personas (incluido el cliente que hizo la reserva)
        int totalPersonas = 1 + (reserva.getAcompanantes() != null ? reserva.getAcompanantes().size() : 0);

        // Calcular el descuento de grupo
        BigDecimal descuentoGrupo = calcularDescuentoGrupo(totalPersonas);

        // Calcular el descuento por cliente frecuente
        BigDecimal descuentoClienteFrecuente = calcularDescuentoClienteFrecuente(cliente,
                reserva.getAcompanantes());

        // Calcular el descuento por cumpleaños
        BigDecimal descuentoCumpleanos = calcularDescuentoCumpleanos(cliente,
                reserva.getAcompanantes(), totalPersonas, reserva.getDiaReserva());

        // Calcular el monto base (precio por persona * número de personas)
        BigDecimal precioBase = new BigDecimal(tarifa.getPrecioBase());
        BigDecimal montoBase = precioBase.multiply(new BigDecimal(totalPersonas));

        // Calcular descuentos totales
        BigDecimal descuentoTotal =
                descuentoGrupo.add(descuentoClienteFrecuente).add(descuentoCumpleanos);

        // Calcular monto final después de descuentos
        BigDecimal montoFinal = montoBase.subtract(descuentoTotal);
        if (montoFinal.compareTo(BigDecimal.ZERO) < 0) {
            montoFinal = BigDecimal.ZERO;
        }

        // Calcular IVA
        BigDecimal iva = montoFinal.multiply(IVA_RATE).setScale(0, RoundingMode.HALF_UP);

        // Calcular monto total con IVA
        BigDecimal montoTotalConIva = montoFinal.add(iva);

        // Crear el comprobante de pago
        ComprobantePagoEntity comprobante = new ComprobantePagoEntity();
        comprobante.setCliente(cliente);
        comprobante.setReserva(reserva);
        comprobante.setTarifa(tarifa);
        comprobante.setDescuentoGrupo(descuentoGrupo);
        comprobante.setDescuentoClienteFrecuente(descuentoClienteFrecuente);
        comprobante.setDescuentoCumpleanos(descuentoCumpleanos);
        comprobante.setMontoBase(montoBase);
        comprobante.setMontoFinal(montoFinal);
        comprobante.setIva(iva);
        comprobante.setMontoTotalConIva(montoTotalConIva);

        // Guardar el comprobante
        return comprobantePagoRepository.save(comprobante);
    }

    private Long obtenerTarifaIdPorTiempoReserva(int tiempoReserva) {
        if (tiempoReserva <= 30) {
            return 1L;
        } else if (tiempoReserva <= 35) {
            return 2L;
        } else {
            return 3L;
        }
    }

    private BigDecimal calcularDescuentoGrupo(int totalPersonas) {
        BigDecimal porcentajeDescuento;
        if (totalPersonas <= 2) {
            porcentajeDescuento = BigDecimal.ZERO;
        } else if (totalPersonas <= 5) {
            porcentajeDescuento = new BigDecimal("0.10");
        } else if (totalPersonas <= 10) {
            porcentajeDescuento = new BigDecimal("0.20");
        } else {
            porcentajeDescuento = new BigDecimal("0.30");
        }
        return porcentajeDescuento;
    }

    private BigDecimal calcularDescuentoClienteFrecuente(ClienteEntity cliente, List<String> acompanantesRut) {
        BigDecimal descuentoTotal = BigDecimal.ZERO;

        // Añadir descuento del cliente principal
        BigDecimal descuentoCliente = new BigDecimal(cliente.getDescuentoAplicable()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        descuentoTotal = descuentoTotal.add(descuentoCliente);

        // Añadir descuentos de los acompañantes
        if (acompanantesRut != null && !acompanantesRut.isEmpty()) {
            for (String rutAcompanante : acompanantesRut) {
                Optional<ClienteEntity> optAcompanante = clienteService.getClienteById(rutAcompanante);
                if (optAcompanante.isPresent()) {
                    BigDecimal descuentoAcompanante = new BigDecimal(optAcompanante.get().getDescuentoAplicable())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    descuentoTotal = descuentoTotal.add(descuentoAcompanante);
                }
            }
        }

        return descuentoTotal;
    }

    private BigDecimal calcularDescuentoCumpleanos(ClienteEntity cliente, List<String> acompanantesRut,
                                                   int totalPersonas, LocalDate diaReserva) {
        BigDecimal descuentoTotal = BigDecimal.ZERO;
        int maxCumpleanerosBeneficiados = 0;

        // Determinar cuántas personas pueden beneficiarse del descuento por cumpleaños
        if (totalPersonas >= 3 && totalPersonas <= 5) {
            maxCumpleanerosBeneficiados = 1;
        } else if (totalPersonas >= 6 && totalPersonas <= 10) {
            maxCumpleanerosBeneficiados = 2;
        }

        if (maxCumpleanerosBeneficiados <= 0) {
            return BigDecimal.ZERO;
        }

        // Lista para almacenar a los cumpleañeros
        List<ClienteEntity> cumpleaneros = new ArrayList<>();

        // Verificar si el cliente principal cumple años
        if (esCumpleanos(cliente, diaReserva)) {
            cumpleaneros.add(cliente);
        }

        // Verificar si algún acompañante cumple años
        if (acompanantesRut != null && !acompanantesRut.isEmpty()) {
            for (String rutAcompanante : acompanantesRut) {
                if (cumpleaneros.size() >= maxCumpleanerosBeneficiados) {
                    break;
                }

                Optional<ClienteEntity> optAcompanante = clienteService.getClienteById(rutAcompanante);
                if (optAcompanante.isPresent() && esCumpleanos(optAcompanante.get(), diaReserva)) {
                    cumpleaneros.add(optAcompanante.get());
                }
            }
        }

        // Calcular el descuento total (50% para cada cumpleañero)
        for (ClienteEntity cumpleanero : cumpleaneros) {
            descuentoTotal = descuentoTotal.add(new BigDecimal("0.50"));
        }

        return descuentoTotal;
    }

    private boolean esCumpleanos(ClienteEntity cliente, LocalDate diaReserva) {
        if (cliente.getFechaCumple() == null) {
            return false;
        }

        return cliente.getFechaCumple().getDayOfMonth() == diaReserva.getDayOfMonth() &&
                cliente.getFechaCumple().getMonthValue() == diaReserva.getMonthValue();
    }

    @Transactional
    public void enviarComprobantePorEmail(Long comprobanteId) {
        Optional<ComprobantePagoEntity> optComprobante = comprobantePagoRepository.findById(comprobanteId);
        if (optComprobante.isEmpty()) {
            throw new RuntimeException("No se encontró el comprobante con ID: " + comprobanteId);
        }

        ComprobantePagoEntity comprobante = optComprobante.get();
        ReservaEntity reserva = comprobante.getReserva();
        ClienteEntity cliente = comprobante.getCliente();

        try {
            // Generar el PDF
            byte[] pdfBytes = generarPDF(comprobante);

            // Enviar el email al cliente principal
            enviarEmail(cliente.getEmail(), "Comprobante de Pago - KartingRM",
                    "Adjunto encontrará el comprobante de pago para su reserva en KartingRM.", pdfBytes);

            // Enviar emails a los acompañantes
            if (reserva.getAcompanantes() != null && !reserva.getAcompanantes().isEmpty()) {
                for (String rutAcompanante : reserva.getAcompanantes()) {
                    Optional<ClienteEntity> optAcompanante = clienteService.getClienteById(rutAcompanante);
                    if (optAcompanante.isPresent()) {
                        ClienteEntity acompanante = optAcompanante.get();
                        enviarEmail(acompanante.getEmail(), "Comprobante de Pago - KartingRM",
                                "Adjunto encontrará el comprobante de pago para su reserva en KartingRM.", pdfBytes);
                    }
                }
            }
        } catch (IOException | MessagingException e) {
            throw new RuntimeException("Error al enviar el comprobante por email: " + e.getMessage(), e);
        }
    }

    private byte[] generarPDF(ComprobantePagoEntity comprobante) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Configurar la fuente
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("COMPROBANTE DE PAGO - KARTINGRM");
                contentStream.endText();

                // Información de la reserva
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("INFORMACIÓN DE LA RESERVA:");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 12);

                // Código de reserva
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Código de reserva: " + comprobante.getReserva().getIdReserva());
                contentStream.endText();

                // Fecha y hora
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 680);
                contentStream.showText("Fecha: " +
                        comprobante.getReserva().getDiaReserva().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 660);
                contentStream.showText("Hora: " + comprobante.getReserva().getHoraInicio() + " - " +
                        comprobante.getReserva().getHoraFin());
                contentStream.endText();

                // Número de vueltas/tiempo
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 640);
                contentStream.showText("Número de vueltas: " + comprobante.getTarifa().getNumeroVueltas() +
                        " (máx " + comprobante.getTarifa().getTiempoMaximo() + " min)");
                contentStream.endText();

                // Cantidad de personas
                int totalPersonas = 1 + (comprobante.getReserva().getAcompanantes() != null ?
                        comprobante.getReserva().getAcompanantes().size() : 0);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 620);
                contentStream.showText("Cantidad de personas: " + totalPersonas);
                contentStream.endText();

                // Persona que realizó la reserva
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 600);
                contentStream.showText("Reservado por: " + comprobante.getCliente().getNombre() + " (RUT: " +
                        comprobante.getCliente().getRut() + ")");
                contentStream.endText();

                // Totales
                float tableY = 400;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(350, tableY);
                contentStream.showText("Monto Base:");
                contentStream.endText();
                contentStream.beginText();
                contentStream.newLineAtOffset(480, tableY);
                contentStream.showText("$" + comprobante.getMontoBase());
                contentStream.endText();

                tableY -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(350, tableY);
                contentStream.showText("Descuentos Totales:");
                contentStream.endText();
                BigDecimal descuentosTotales = comprobante.getMontoBase().subtract(comprobante.getMontoFinal());
                contentStream.beginText();
                contentStream.newLineAtOffset(480, tableY);
                contentStream.showText("$" + descuentosTotales);
                contentStream.endText();

                tableY -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(350, tableY);
                contentStream.showText("Monto Final:");
                contentStream.endText();
                contentStream.beginText();
                contentStream.newLineAtOffset(480, tableY);
                contentStream.showText("$" + comprobante.getMontoFinal());
                contentStream.endText();

                tableY -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(350, tableY);
                contentStream.showText("IVA (19%):");
                contentStream.endText();
                contentStream.beginText();
                contentStream.newLineAtOffset(480, tableY);
                contentStream.showText("$" + comprobante.getIva());
                contentStream.endText();

                tableY -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(350, tableY);
                contentStream.showText("TOTAL A PAGAR:");
                contentStream.endText();
                contentStream.beginText();
                contentStream.newLineAtOffset(480, tableY);
                contentStream.showText("$" + comprobante.getMontoTotalConIva());
                contentStream.endText();

                // Pie del documento
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 50);
                contentStream.showText("Este comprobante debe ser presentado el día de su visita KartingRM");
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void enviarEmail(String destinatario, String asunto, String mensaje, byte[] pdfAdjunto) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setFrom("aedope.pbpaa@gmail.com");
            helper.setSubject(asunto);
            helper.setText(mensaje);

            // Usar ByteArrayDataSource para el adjunto
            ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfAdjunto, "application/pdf");
            helper.addAttachment("Comprobante_KartingRM.pdf", dataSource);

            mailSender.send(message);

        } catch (MessagingException e) {
            System.err.println("Error al enviar email a: " + destinatario);
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }
}