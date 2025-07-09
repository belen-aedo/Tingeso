package com.example.demo.service;

import com.example.demo.entities.*;
import com.example.demo.repositories.ComprobantePagoRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
            @Lazy ReservaService reservaService,  // Agregar @Lazy aquí
            TarifaService tarifaService,
            JavaMailSender mailSender) {
        this.comprobantePagoRepository = comprobantePagoRepository;
        this.clienteService = clienteService;
        this.reservaService = reservaService;
        this.tarifaService = tarifaService;
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
        try {
            // Verificar si ya existe un comprobante para esta reserva
            Optional<ComprobantePagoEntity> comprobanteExistente = getComprobanteByReservaId(reservaId);
            if (comprobanteExistente.isPresent()) {
                System.out.println("Ya existe un comprobante para la reserva ID: " + reservaId);
                return comprobanteExistente.get();
            }

            // Obtener la reserva
            Optional<ReservaEntity> optReserva = reservaService.obtenerReservaPorId(reservaId);
            if (optReserva.isEmpty()) {
                throw new RuntimeException("No se encontró la reserva con ID: " + reservaId);
            }

            ReservaEntity reserva = optReserva.get();
            ClienteEntity cliente = reserva.getCliente();

            if (cliente == null) {
                throw new RuntimeException("La reserva no tiene cliente asociado");
            }

            // Buscar la tarifa según el tiempo de reserva
            Long tarifaId = obtenerTarifaIdPorTiempoReserva(reserva.getTiempoReserva());
            Optional<TarifaEntity> optTarifa = tarifaService.getTarifaById(tarifaId);
            if (optTarifa.isEmpty()) {
                throw new RuntimeException("No se encontró una tarifa para el tiempo de reserva: " +
                        reserva.getTiempoReserva() + " minutos");
            }

            TarifaEntity tarifa = optTarifa.get();

            // Calcular número total de personas
            int totalPersonas = calcularTotalPersonas(reserva);
            System.out.println("Total personas: " + totalPersonas);

            // Calcular el monto base
            BigDecimal precioBase = new BigDecimal(tarifa.getPrecioBase());
            BigDecimal montoBase = precioBase.multiply(new BigDecimal(totalPersonas));
            System.out.println("Monto base: " + montoBase);

            // Calcular descuentos
            BigDecimal descuentoGrupo = calcularDescuentoGrupo(totalPersonas, montoBase);
            BigDecimal descuentoClienteFrecuente = calcularDescuentoClienteFrecuente(cliente,
                    reserva.getAcompanantes(), montoBase);
            BigDecimal descuentoCumpleanos = calcularDescuentoCumpleanos(cliente,
                    reserva.getAcompanantes(), totalPersonas, reserva.getDiaReserva(), montoBase);

            System.out.println("Descuento grupo: " + descuentoGrupo);
            System.out.println("Descuento cliente frecuente: " + descuentoClienteFrecuente);
            System.out.println("Descuento cumpleaños: " + descuentoCumpleanos);

            // Calcular totales
            BigDecimal descuentoTotal = descuentoGrupo.add(descuentoClienteFrecuente).add(descuentoCumpleanos);
            BigDecimal montoFinal = montoBase.subtract(descuentoTotal);
            if (montoFinal.compareTo(BigDecimal.ZERO) < 0) {
                montoFinal = BigDecimal.ZERO;
            }

            BigDecimal iva = montoFinal.multiply(IVA_RATE).setScale(0, RoundingMode.HALF_UP);
            BigDecimal montoTotalConIva = montoFinal.add(iva);

            // Crear y guardar el comprobante
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

            ComprobantePagoEntity comprobanteGuardado = comprobantePagoRepository.save(comprobante);
            System.out.println("Comprobante guardado exitosamente con ID: " + comprobanteGuardado.getId());

            return comprobanteGuardado;

        } catch (Exception e) {
            System.err.println("Error al generar comprobante: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al generar comprobante: " + e.getMessage(), e);
        }
    }

    private int calcularTotalPersonas(ReservaEntity reserva) {
        int totalPersonas = 1; // Cliente que hizo la reserva
        if (reserva.getAcompanantes() != null) {
            totalPersonas += reserva.getAcompanantes().size();
        }
        return totalPersonas;
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

    private BigDecimal calcularDescuentoGrupo(int totalPersonas, BigDecimal montoBase) {
        BigDecimal porcentajeDescuento = BigDecimal.ZERO;

        if (totalPersonas >= 3 && totalPersonas <= 5) {
            porcentajeDescuento = new BigDecimal("0.10");
        } else if (totalPersonas >= 6 && totalPersonas <= 10) {
            porcentajeDescuento = new BigDecimal("0.20");
        } else if (totalPersonas > 10) {
            porcentajeDescuento = new BigDecimal("0.30");
        }

        return montoBase.multiply(porcentajeDescuento).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularDescuentoClienteFrecuente(ClienteEntity cliente, List<String> acompanantesRut,
                                                         BigDecimal montoBase) {
        BigDecimal porcentajeDescuentoTotal = BigDecimal.ZERO;

        try {
            // Descuento del cliente principal

            // Descuentos de los acompañantes
            if (acompanantesRut != null && !acompanantesRut.isEmpty()) {
                for (String rutAcompanante : acompanantesRut) {
                    try {
                        Optional<ClienteEntity> optAcompanante = clienteService.getClienteById(rutAcompanante);
                        if (optAcompanante.isPresent()) {
                            ClienteEntity acompanante = optAcompanante.get();
                        }
                    } catch (Exception e) {
                        System.err.println("Error obteniendo acompañante " + rutAcompanante + ": " + e.getMessage());
                    }
                }
            }

            return montoBase.multiply(porcentajeDescuentoTotal).setScale(0, RoundingMode.HALF_UP);
        } catch (Exception e) {
            System.err.println("Error calculando descuento cliente frecuente: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calcularDescuentoCumpleanos(ClienteEntity cliente, List<String> acompanantesRut,
                                                   int totalPersonas, LocalDate diaReserva, BigDecimal montoBase) {
        try {
            int maxCumpleanerosBeneficiados = 0;

            if (totalPersonas >= 3 && totalPersonas <= 5) {
                maxCumpleanerosBeneficiados = 1;
            } else if (totalPersonas >= 6 && totalPersonas <= 10) {
                maxCumpleanerosBeneficiados = 2;
            }

            if (maxCumpleanerosBeneficiados <= 0) {
                return BigDecimal.ZERO;
            }

            List<ClienteEntity> cumpleaneros = new ArrayList<>();

            // Verificar cliente principal
            if (esCumpleanos(cliente, diaReserva)) {
                cumpleaneros.add(cliente);
            }

            // Verificar acompañantes
            if (acompanantesRut != null && !acompanantesRut.isEmpty()) {
                for (String rutAcompanante : acompanantesRut) {
                    if (cumpleaneros.size() >= maxCumpleanerosBeneficiados) {
                        break;
                    }
                    try {
                        Optional<ClienteEntity> optAcompanante = clienteService.getClienteById(rutAcompanante);
                        if (optAcompanante.isPresent() && esCumpleanos(optAcompanante.get(), diaReserva)) {
                            cumpleaneros.add(optAcompanante.get());
                        }
                    } catch (Exception e) {
                        System.err.println("Error verificando cumpleaños de acompañante " + rutAcompanante + ": " + e.getMessage());
                    }
                }
            }

            // Calcular descuento (50% por cada cumpleañero)
            BigDecimal descuentoPorCumpleanero = montoBase.multiply(new BigDecimal("0.50"))
                    .setScale(0, RoundingMode.HALF_UP);

            return descuentoPorCumpleanero.multiply(new BigDecimal(cumpleaneros.size()));

        } catch (Exception e) {
            System.err.println("Error calculando descuento cumpleaños: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private boolean esCumpleanos(ClienteEntity cliente, LocalDate diaReserva) {
        try {
            if (cliente.getFechaCumple() == null) {
                return false;
            }

            return cliente.getFechaCumple().getDayOfMonth() == diaReserva.getDayOfMonth() &&
                    cliente.getFechaCumple().getMonthValue() == diaReserva.getMonthValue();
        } catch (Exception e) {
            System.err.println("Error verificando cumpleaños: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public void enviarComprobantePorEmail(Long comprobanteId) {
        try {
            Optional<ComprobantePagoEntity> optComprobante = comprobantePagoRepository.findById(comprobanteId);
            if (optComprobante.isEmpty()) {
                throw new RuntimeException("No se encontró el comprobante con ID: " + comprobanteId);
            }

            ComprobantePagoEntity comprobante = optComprobante.get();
            ReservaEntity reserva = comprobante.getReserva();
            ClienteEntity cliente = comprobante.getCliente();

            if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
                throw new RuntimeException("El cliente no tiene email registrado");
            }

            // Generar el PDF
            byte[] pdfBytes = generarPDF(comprobante);

            // Enviar email al cliente principal
            String mensajeCliente = String.format(
                    "Estimado/a %s,\n\n" +
                            "Adjunto encontrará el comprobante de pago para su reserva en KartingRM.\n\n" +
                            "Código de reserva: %s\n" +
                            "Fecha: %s\n" +
                            "Hora: %s - %s\n\n" +
                            "Recuerde presentar este comprobante el día de su visita.\n\n" +
                            "Saludos cordiales,\n" +
                            "Equipo KartingRM",
                    cliente.getNombre(),
                    reserva.getIdReserva(),
                    reserva.getDiaReserva().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    reserva.getHoraInicio(),
                    reserva.getHoraFin()
            );

            enviarEmail(cliente.getEmail(), "Comprobante de Pago - KartingRM", mensajeCliente, pdfBytes);

            // Enviar emails a los acompañantes
            if (reserva.getAcompanantes() != null && !reserva.getAcompanantes().isEmpty()) {
                for (String rutAcompanante : reserva.getAcompanantes()) {
                    try {
                        Optional<ClienteEntity> optAcompanante = clienteService.getClienteById(rutAcompanante);
                        if (optAcompanante.isPresent()) {
                            ClienteEntity acompanante = optAcompanante.get();
                            if (acompanante.getEmail() != null && !acompanante.getEmail().trim().isEmpty()) {
                                String mensajeAcompanante = String.format(
                                        "Estimado/a %s,\n\n" +
                                                "Adjunto encontrará el comprobante de pago para la reserva en KartingRM donde usted es acompañante.\n\n" +
                                                "Reserva realizada por: %s\n" +
                                                "Código de reserva: %s\n" +
                                                "Fecha: %s\n" +
                                                "Hora: %s - %s\n\n" +
                                                "Recuerde presentar este comprobante el día de su visita.\n\n" +
                                                "Saludos cordiales,\n" +
                                                "Equipo KartingRM",
                                        acompanante.getNombre(),
                                        cliente.getNombre(),
                                        reserva.getIdReserva(),
                                        reserva.getDiaReserva().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                        reserva.getHoraInicio(),
                                        reserva.getHoraFin()
                                );

                                enviarEmail(acompanante.getEmail(), "Comprobante de Pago - KartingRM", mensajeAcompanante, pdfBytes);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error enviando email a acompañante " + rutAcompanante + ": " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error al enviar el comprobante por email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al enviar el comprobante por email: " + e.getMessage(), e);
        }
    }

    private byte[] generarPDF(ComprobantePagoEntity comprobante) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yPosition = 750;

                // Título
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("COMPROBANTE DE PAGO - KARTINGRM");
                contentStream.endText();
                yPosition -= 40;

                // Línea separadora
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(545, yPosition);
                contentStream.stroke();
                yPosition -= 30;

                // Información de la reserva
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("INFORMACION DE LA RESERVA");
                contentStream.endText();
                yPosition -= 25;

                contentStream.setFont(PDType1Font.HELVETICA, 12);

                String[] infoReserva = {
                        "Codigo de reserva: " + comprobante.getReserva().getIdReserva(),
                        "Fecha: " + comprobante.getReserva().getDiaReserva().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        "Hora: " + comprobante.getReserva().getHoraInicio() + " - " + comprobante.getReserva().getHoraFin(),
                        "Numero de vueltas: " + comprobante.getTarifa().getNumeroVueltas() + " (max " + comprobante.getTarifa().getTiempoMaximo() + " min)"
                };

                for (String info : infoReserva) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(info);
                    contentStream.endText();
                    yPosition -= 20;
                }

                yPosition -= 10;

                // Información del cliente
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("INFORMACION DEL CLIENTE");
                contentStream.endText();
                yPosition -= 25;

                contentStream.setFont(PDType1Font.HELVETICA, 12);

                int totalPersonas = calcularTotalPersonas(comprobante.getReserva());

                String[] infoCliente = {
                        "Reservado por: " + comprobante.getCliente().getNombre(),
                        "RUT: " + comprobante.getCliente().getRut(),
                        "Email: " + comprobante.getCliente().getEmail(),
                        "Cantidad de personas: " + totalPersonas
                };

                for (String info : infoCliente) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(info);
                    contentStream.endText();
                    yPosition -= 20;
                }

                yPosition -= 30;

                // Detalle de costos
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("DETALLE DE COSTOS");
                contentStream.endText();
                yPosition -= 25;

                contentStream.setFont(PDType1Font.HELVETICA, 12);

                String[][] costos = {
                        {"Monto Base:", "$" + comprobante.getMontoBase().toString()},
                        {"Descuento Grupo:", "-$" + comprobante.getDescuentoGrupo().toString()},
                        {"Descuento Cliente Frecuente:", "-$" + comprobante.getDescuentoClienteFrecuente().toString()},
                        {"Descuento Cumpleanos:", "-$" + comprobante.getDescuentoCumpleanos().toString()},
                        {"Subtotal:", "$" + comprobante.getMontoFinal().toString()},
                        {"IVA (19%):", "$" + comprobante.getIva().toString()}
                };

                for (String[] costo : costos) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(costo[0]);
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(400, yPosition);
                    contentStream.showText(costo[1]);
                    contentStream.endText();
                    yPosition -= 20;
                }

                yPosition -= 10;

                // Total final
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("TOTAL A PAGAR:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(400, yPosition);
                contentStream.showText("$" + comprobante.getMontoTotalConIva().toString());
                contentStream.endText();
                yPosition -= 40;

                // Línea separadora
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(545, yPosition);
                contentStream.stroke();
                yPosition -= 30;

                // Pie del documento
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Este comprobante debe ser presentado el dia de su visita a KartingRM");
                contentStream.endText();
                yPosition -= 15;

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Generado el: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
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

            // Adjuntar PDF
            ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfAdjunto, "application/pdf");
            helper.addAttachment("Comprobante_KartingRM.pdf", dataSource);

            mailSender.send(message);
            System.out.println("Email enviado exitosamente a: " + destinatario);

        } catch (MessagingException e) {
            System.err.println("Error al enviar email a: " + destinatario);
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }
    // MÉTODO ADICIONAL PARA AGREGAR AL ComprobantePagoService

    /**
     * Método público para generar PDF (para uso en el controlador)
     */
    public byte[] generarPDFPublico(ComprobantePagoEntity comprobante) throws IOException {
        return generarPDF(comprobante);
    }

    /**
     * Método para generar comprobantes automáticamente al crear reservas
     * (se llama desde ReservaService)
     */
    @Transactional
    public ComprobantePagoEntity generarComprobanteAutomatico(Long reservaId) {
        try {
            // Verificar si ya existe un comprobante para esta reserva
            Optional<ComprobantePagoEntity> comprobanteExistente = getComprobanteByReservaId(reservaId);
            if (comprobanteExistente.isPresent()) {
                System.out.println("Comprobante ya existe para reserva ID: " + reservaId);
                return comprobanteExistente.get();
            }

            // Generar nuevo comprobante
            ComprobantePagoEntity comprobante = generarComprobantePago(reservaId);
            System.out.println("Comprobante generado automáticamente para reserva: " + reservaId);

            return comprobante;
        } catch (Exception e) {
            System.err.println("Error al generar comprobante automático para reserva " + reservaId + ": " + e.getMessage());
            throw new RuntimeException("Error al generar comprobante automático", e);
        }
    }

    /**
     * Método para generar comprobantes faltantes para reservas existentes
     */
    @Transactional
    public int generarComprobantesFaltantesService() {
        try {
            List<ReservaEntity> todasLasReservas = reservaService.obtenerTodasLasReservas();
            int comprobantesGenerados = 0;

            for (ReservaEntity reserva : todasLasReservas) {
                try {
                    // Verificar si ya existe un comprobante para esta reserva
                    Optional<ComprobantePagoEntity> comprobanteExistente =
                            getComprobanteByReservaId(reserva.getIdReserva());

                    if (comprobanteExistente.isEmpty()) {
                        // Generar comprobante faltante
                        generarComprobantePago(reserva.getIdReserva());
                        comprobantesGenerados++;
                        System.out.println("Comprobante generado para reserva: " + reserva.getIdReserva());
                    }
                } catch (Exception e) {
                    System.err.println("Error generando comprobante para reserva " +
                            reserva.getIdReserva() + ": " + e.getMessage());
                }
            }

            System.out.println("Proceso completado. Comprobantes generados: " + comprobantesGenerados);
            return comprobantesGenerados;

        } catch (Exception e) {
            System.err.println("Error en el proceso de generación de comprobantes faltantes: " + e.getMessage());
            throw new RuntimeException("Error generando comprobantes faltantes", e);
        }
    }
}
