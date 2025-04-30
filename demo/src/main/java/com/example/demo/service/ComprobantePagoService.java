package com.example.demo.service;

import com.example.demo.entities.*;
import com.example.demo.repositories.ComprobantePagoRepository;
import com.example.demo.repositories.TarifaRepository;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.Getter;
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
import jakarta.transaction.Transactional;

import java.io.ByteArrayInputStream;
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

    private static final BigDecimal IVA_RATE = BigDecimal.valueOf(0);
    @Getter
    private final ComprobantePagoRepository comprobantePagoRepository;
    private final ClienteService clienteService;
    private final ReservaService reservaService;
    private final TarifaService tarifaService;
    private final TarifaRepository tarifaRepository;
    private final JavaMailSender mailSender;

    @Autowired
    private ReporteService reporteService; // ✅ CAMPO CORRECTAMENTE INYECTADO

    @Autowired
    public ComprobantePagoService(
            ComprobantePagoRepository comprobantePagoRepository,
            ClienteService clienteService,
            ReservaService reservaService,
            TarifaService tarifaService,
            TarifaRepository tarifaRepository,
            JavaMailSender mailSender) {
        this.comprobantePagoRepository = comprobantePagoRepository;
        this.clienteService = clienteService;
        this.reservaService = reservaService;
        this.tarifaService = tarifaService;
        this.tarifaRepository = tarifaRepository;
        this.mailSender = mailSender;
    }

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
        Optional<TarifaEntity> optTarifa = tarifaService.getTarifaById(obtenerTarifaIdPorTiempoReserva(reserva.getTiempoReserva()));
        if (optTarifa.isEmpty()) {
            throw new RuntimeException("No se encontró una tarifa para el tiempo de reserva: " + reserva.getTiempoReserva() + " minutos");
        }

        TarifaEntity tarifa = optTarifa.get();

        // Calcular número total de personas (incluido el cliente que hizo la reserva)
        int totalPersonas = 1 + (reserva.getAcompanantes() != null ? reserva.getAcompanantes().size() : 0);

        // Calcular el precio base por persona
        BigDecimal precioPorPersona = new BigDecimal(tarifa.getPrecioBase());

        // Calcular el monto base (precio por persona * número de personas)
        BigDecimal montoBase = precioPorPersona.multiply(new BigDecimal(totalPersonas));

        // Calcular el porcentaje de descuento de grupo
        BigDecimal porcentajeDescuentoGrupo = calcularDescuentoGrupo(totalPersonas);

        // Aplicar el descuento de grupo al monto base
        BigDecimal descuentoGrupo = montoBase.multiply(porcentajeDescuentoGrupo).setScale(0, RoundingMode.HALF_UP);

        // Calcular el descuento por cliente frecuente (aplicado a cada persona individualmente)
        BigDecimal descuentoClienteFrecuente = calcularDescuentoClienteFrecuente(cliente, reserva.getAcompanantes(), precioPorPersona);

        // Calcular el descuento por cumpleaños (aplicado a cada cumpleañero individualmente)
        BigDecimal descuentoCumpleanos = calcularDescuentoCumpleanos(cliente, reserva.getAcompanantes(), totalPersonas, reserva.getDiaReserva(), precioPorPersona);

        // Calcular descuentos totales
        BigDecimal descuentoTotal = descuentoGrupo.add(descuentoClienteFrecuente).add(descuentoCumpleanos);

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
        comprobante = comprobantePagoRepository.save(comprobante);

        // ACTUALIZAR REPORTE AUTOMÁTICAMENTE
        actualizarReporteMensual(comprobante);

        // Guardar el comprobante
        return comprobantePagoRepository.save(comprobante);
    }

    private Long obtenerTarifaIdPorTiempoReserva(int tiempoReserva) {
        // Buscar directamente la tarifa adecuada
        TarifaEntity tarifa = tarifaRepository.findFirstByTiempoMaximoGreaterThanEqualOrderByTiempoMaximoAsc(tiempoReserva);

        if (tarifa != null) {
            return tarifa.getId();
        }

        // Si no hay tarifas con tiempo suficiente, buscar la de mayor tiempo
        TarifaEntity tarifaMaxima = tarifaRepository.findFirstByOrderByTiempoMaximoDesc();
        return tarifaMaxima != null ? tarifaMaxima.getId() : null;
    }

    private BigDecimal calcularDescuentoGrupo(int totalPersonas) {
        BigDecimal porcentajeDescuento;

        if (totalPersonas <= 2) {
            porcentajeDescuento = BigDecimal.ZERO; // 0%
        } else if (totalPersonas <= 5) {
            porcentajeDescuento = new BigDecimal("0.10"); // 10%
        } else if (totalPersonas <= 10) {
            porcentajeDescuento = new BigDecimal("0.20"); // 20%
        } else {
            porcentajeDescuento = new BigDecimal("0.30"); // 30%
        }

        return porcentajeDescuento;
    }

    private BigDecimal calcularDescuentoClienteFrecuente(ClienteEntity cliente, List<String> acompanantesRut, BigDecimal precioPorPersona) {
        BigDecimal descuentoTotal = BigDecimal.ZERO;

        // Calcular descuento del cliente principal
        BigDecimal porcentajeDescuentoCliente = new BigDecimal(cliente.getDescuentoAplicable()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal descuentoCliente = precioPorPersona.multiply(porcentajeDescuentoCliente).setScale(0, RoundingMode.HALF_UP);
        descuentoTotal = descuentoTotal.add(descuentoCliente);

        // Calcular descuentos de los acompañantes
        if (acompanantesRut != null && !acompanantesRut.isEmpty()) {
            for (String rutAcompanante : acompanantesRut) {
                Optional<ClienteEntity> optAcompanante = clienteService.getClienteById(rutAcompanante);
                if (optAcompanante.isPresent()) {
                    BigDecimal porcentajeDescuentoAcompanante = new BigDecimal(optAcompanante.get().getDescuentoAplicable())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    BigDecimal descuentoAcompanante = precioPorPersona.multiply(porcentajeDescuentoAcompanante).setScale(0, RoundingMode.HALF_UP);
                    descuentoTotal = descuentoTotal.add(descuentoAcompanante);
                }
            }
        }

        return descuentoTotal;
    }

    private BigDecimal calcularDescuentoCumpleanos(ClienteEntity cliente, List<String> acompanantesRut,
                                                   int totalPersonas, LocalDate diaReserva, BigDecimal precioPorPersona) {
        BigDecimal descuentoTotal = BigDecimal.ZERO;
        int maxCumpleanerosBeneficiados = 0;

        // Determinar cuántas personas pueden beneficiarse del descuento por cumpleaños
        if (totalPersonas >= 3 && totalPersonas <= 5) {
            maxCumpleanerosBeneficiados = 1;
        } else if (totalPersonas >= 6 && totalPersonas <= 10) {
            maxCumpleanerosBeneficiados = 2;
        } else if (totalPersonas > 10) {
            maxCumpleanerosBeneficiados = 3; // Para grupos de más de 10 personas, aplica a 3 cumpleañeros
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
                    break; // Ya alcanzamos el máximo de beneficiados
                }

                Optional<ClienteEntity> optAcompanante = clienteService.getClienteById(rutAcompanante);
                if (optAcompanante.isPresent() && esCumpleanos(optAcompanante.get(), diaReserva)) {
                    cumpleaneros.add(optAcompanante.get());
                }
            }
        }

        // Calcular el descuento total (50% del precio para cada cumpleañero)
        for (ClienteEntity cumpleanero : cumpleaneros) {
            BigDecimal descuentoCumpleanero = precioPorPersona.multiply(new BigDecimal("0.50")).setScale(0, RoundingMode.HALF_UP);
            descuentoTotal = descuentoTotal.add(descuentoCumpleanero);
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
    private void actualizarReporteMensual(ComprobantePagoEntity comprobante) {
        LocalDate mesActual = comprobante.getReserva().getDiaReserva().withDayOfMonth(1);

        // === REPORTE POR VUELTAS ===
        String tipoPorVueltas = "PorVueltas";
        Integer vueltas = comprobante.getTarifa().getNumeroVueltas();
        Integer tiempo = comprobante.getTarifa().getTiempoMaximo();




        Optional<ReporteEntity> optReporteVueltas = reporteService.obtenerPorMes(mesActual).stream()
                .filter(r -> tipoPorVueltas.equals(r.getTipoReporte()) &&
                        vueltas.equals(r.getNumeroVueltas()) &&
                        tiempo.equals(r.getTiempoMaximo()))
                .findFirst();

        ReporteEntity reporteVueltas;
        if (optReporteVueltas.isPresent()) {
            reporteVueltas = optReporteVueltas.get();
            reporteVueltas.setIngresoTotal(reporteVueltas.getIngresoTotal() + comprobante.getMontoTotalConIva().doubleValue());
        } else {
            reporteVueltas = new ReporteEntity();
            reporteVueltas.setMesGenerado(mesActual);
            reporteVueltas.setTipoReporte(tipoPorVueltas);
            reporteVueltas.setNumeroVueltas(vueltas);
            reporteVueltas.setTiempoMaximo(tiempo);
            reporteVueltas.setIngresoTotal(comprobante.getMontoTotalConIva().doubleValue());
        }
        reporteService.guardarReporte(reporteVueltas);

        // === REPORTE POR PERSONAS ===
        String tipoPorPersonas = "PorPersonas";
        int totalPersonas = 1 + (comprobante.getReserva().getAcompanantes() != null ? comprobante.getReserva().getAcompanantes().size() : 0);

        // Definir rango
        int min = totalPersonas <= 2 ? 1 : totalPersonas <= 5 ? 3 : totalPersonas <= 10 ? 6 : 11;
        int max = totalPersonas <= 2 ? 2 : totalPersonas <= 5 ? 5 : totalPersonas <= 10 ? 10 : 20;

        Optional<ReporteEntity> optReportePersonas = reporteService.obtenerPorMes(mesActual).stream()
                .filter(r -> tipoPorPersonas.equals(r.getTipoReporte()) &&
                        r.getMinPersonas() != null && r.getMaxPersonas() != null &&
                        r.getMinPersonas().equals(min) &&
                        r.getMaxPersonas().equals(max))
                .findFirst();

        ReporteEntity reportePersonas;
        if (optReportePersonas.isPresent()) {
            reportePersonas = optReportePersonas.get();
            reportePersonas.setIngresoTotal(reportePersonas.getIngresoTotal() + comprobante.getMontoTotalConIva().doubleValue());
        } else {
            reportePersonas = new ReporteEntity();
            reportePersonas.setMesGenerado(mesActual);
            reportePersonas.setTipoReporte(tipoPorPersonas);
            reportePersonas.setMinPersonas(min);
            reportePersonas.setMaxPersonas(max);
            reportePersonas.setIngresoTotal(comprobante.getMontoTotalConIva().doubleValue());
        }
        reporteService.guardarReporte(reportePersonas);
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
                contentStream.showText("Fecha: " + comprobante.getReserva().getDiaReserva().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 660);
                contentStream.showText("Hora: " + comprobante.getReserva().getHoraInicio() + " - " + comprobante.getReserva().getHoraFin());
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
                contentStream.showText("Reservado por: " + comprobante.getCliente().getNombre() + " (RUT: " + comprobante.getCliente().getRut() + ")");
                contentStream.endText();

                // Tabla de detalles de pago
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 570);
                contentStream.showText("DETALLE DE PAGO:");
                contentStream.endText();

                // Encabezados de la tabla
                float tableY = 550;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);

                contentStream.beginText();
                contentStream.newLineAtOffset(50, tableY);
                contentStream.showText("Cliente");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(150, tableY);
                contentStream.showText("Tarifa Base");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(220, tableY);
                contentStream.showText("Desc. Grupo");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(290, tableY);
                contentStream.showText("Desc. Frecuente");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(380, tableY);
                contentStream.showText("Desc. Cumpleaños");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(480, tableY);
                contentStream.showText("Monto Final");
                contentStream.endText();

                // Datos del cliente principal
                tableY -= 20;
                contentStream.setFont(PDType1Font.HELVETICA, 10);

                contentStream.beginText();
                contentStream.newLineAtOffset(50, tableY);
                contentStream.showText(comprobante.getCliente().getNombre());
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(150, tableY);
                contentStream.showText("$" + comprobante.getTarifa().getPrecioBase());
                contentStream.endText();

                // Calcular porcentaje de descuento de grupo para mostrar
                int porcentajeDescuentoGrupo = calcularDescuentoGrupo(totalPersonas).multiply(new BigDecimal("100")).intValue();
                contentStream.beginText();
                contentStream.newLineAtOffset(220, tableY);
                contentStream.showText(porcentajeDescuentoGrupo + "%");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(290, tableY);
                contentStream.showText(comprobante.getCliente().getDescuentoAplicable() + "%");
                contentStream.endText();

                if (esCumpleanos(comprobante.getCliente(), comprobante.getReserva().getDiaReserva())) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(380, tableY);
                    contentStream.showText("50%");
                    contentStream.endText();
                } else {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(380, tableY);
                    contentStream.showText("0%");
                    contentStream.endText();
                }

                // El monto final individual sería una parte del total
                BigDecimal montoIndividual = comprobante.getMontoFinal().divide(new BigDecimal(totalPersonas), 0, RoundingMode.HALF_UP);
                contentStream.beginText();
                contentStream.newLineAtOffset(480, tableY);
                contentStream.showText("$" + montoIndividual);
                contentStream.endText();

                // Datos de los acompañantes
                if (comprobante.getReserva().getAcompanantes() != null && !comprobante.getReserva().getAcompanantes().isEmpty()) {
                    for (String rutAcompanante : comprobante.getReserva().getAcompanantes()) {
                        Optional<ClienteEntity> optAcompanante = clienteService.getClienteById(rutAcompanante);
                        if (optAcompanante.isPresent()) {
                            ClienteEntity acompanante = optAcompanante.get();
                            tableY -= 20;

                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, tableY);
                            contentStream.showText(acompanante.getNombre());
                            contentStream.endText();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(150, tableY);
                            contentStream.showText("$" + comprobante.getTarifa().getPrecioBase());
                            contentStream.endText();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(220, tableY);
                            contentStream.showText(porcentajeDescuentoGrupo + "%");
                            contentStream.endText();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(290, tableY);
                            contentStream.showText(acompanante.getDescuentoAplicable() + "%");
                            contentStream.endText();

                            if (esCumpleanos(acompanante, comprobante.getReserva().getDiaReserva())) {
                                contentStream.beginText();
                                contentStream.newLineAtOffset(380, tableY);
                                contentStream.showText("50%");
                                contentStream.endText();
                            } else {
                                contentStream.beginText();
                                contentStream.newLineAtOffset(380, tableY);
                                contentStream.showText("0%");
                                contentStream.endText();
                            }

                            contentStream.beginText();
                            contentStream.newLineAtOffset(480, tableY);
                            contentStream.showText("$" + montoIndividual);
                            contentStream.endText();
                        }
                    }
                }

                // Totales
                tableY -= 40;
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

                BigDecimal descuentosTotales = comprobante.getDescuentoGrupo()
                        .add(comprobante.getDescuentoClienteFrecuente())
                        .add(comprobante.getDescuentoCumpleanos());
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
                contentStream.showText("Este comprobante debe ser presentado el día de su visita a KartingRM");
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void enviarEmail(String destinatario, String asunto, String mensaje, byte[] pdfAdjunto) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(mensaje);
        ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfAdjunto, "application/pdf");
        helper.addAttachment("Comprobante_KartingRM.pdf", dataSource);

        mailSender.send(message);
    }
}