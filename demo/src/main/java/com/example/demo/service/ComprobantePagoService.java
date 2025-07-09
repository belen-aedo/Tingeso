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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.Optional;

@Service
public class ComprobantePagoService {

    // Excepciones personalizadas
    public static class ComprobanteException extends Exception {
        public ComprobanteException(String message) {
            super(message);
        }

        public ComprobanteException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ReservaException extends Exception {
        public ReservaException(String message) {
            super(message);
        }

        public ReservaException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class EmailException extends Exception {
        public EmailException(String message) {
            super(message);
        }

        public EmailException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ComprobantePagoService.class);
    private static final BigDecimal IVA_RATE = new BigDecimal("0.19");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ComprobantePagoRepository comprobantePagoRepository;
    private final ClienteService clienteService;
    private final ReservaService reservaService;
    private final TarifaService tarifaService;
    private final JavaMailSender mailSender;

    @Autowired
    public ComprobantePagoService(
            ComprobantePagoRepository comprobantePagoRepository,
            ClienteService clienteService,
            @Lazy ReservaService reservaService,
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

    public void eliminarComprobante(Long idComprobante) {
        comprobantePagoRepository.deleteById(idComprobante);
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
    public ComprobantePagoEntity generarComprobantePago(Long reservaId) throws ComprobanteException {
        try {
            // Verificar si ya existe un comprobante para esta reserva
            Optional<ComprobantePagoEntity> comprobanteExistente = getComprobanteByReservaId(reservaId);
            if (comprobanteExistente.isPresent()) {
                logger.info("Ya existe un comprobante para la reserva ID: {}", reservaId);
                return comprobanteExistente.get();
            }

            return crearNuevoComprobante(reservaId);


        } catch (ReservaException e) {
            throw new ComprobanteException("Error al generar comprobante para reserva: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ComprobanteException("Error inesperado al generar comprobante: " + e.getMessage(), e);
        }
    }

    private ComprobantePagoEntity crearNuevoComprobante(Long reservaId) throws ReservaException {
        // Obtener la reserva
        Optional<ReservaEntity> optReserva = reservaService.obtenerReservaPorId(reservaId);
        if (optReserva.isEmpty()) {
            throw new ReservaException("No se encontró la reserva con ID: " + reservaId);
        }

        ReservaEntity reserva = optReserva.get();
        ClienteEntity cliente = reserva.getCliente();

        if (cliente == null) {
            throw new ReservaException("La reserva no tiene cliente asociado");
        }

        TarifaEntity tarifa = obtenerTarifa(reserva.getTiempoReserva());
        return procesarComprobante(reserva, cliente, tarifa);
    }

    private TarifaEntity obtenerTarifa(int tiempoReserva) throws ReservaException {
        Long tarifaId = obtenerTarifaIdPorTiempoReserva(tiempoReserva);
        Optional<TarifaEntity> optTarifa = tarifaService.getTarifaById(tarifaId);
        if (optTarifa.isEmpty()) {
            throw new ReservaException("No se encontró una tarifa para el tiempo de reserva: " +
                    tiempoReserva + " minutos");
        }
        return optTarifa.get();
    }

    private ComprobantePagoEntity procesarComprobante(ReservaEntity reserva, ClienteEntity cliente, TarifaEntity tarifa) {
        // Calcular número total de personas
        int totalPersonas = calcularTotalPersonas(reserva);

        // Calcular el monto base
        BigDecimal precioBase = new BigDecimal(tarifa.getPrecioBase());
        BigDecimal montoBase = precioBase.multiply(new BigDecimal(totalPersonas));

        // Calcular descuentos
        BigDecimal descuentoGrupo = calcularDescuentoGrupo(totalPersonas, montoBase);
        BigDecimal descuentoCumpleanos = calcularDescuentoCumpleanos();

        // Calcular totales
        BigDecimal descuentoTotal = descuentoGrupo
                .add(calcularDescuentoClienteFrecuente(montoBase))
                .add(descuentoCumpleanos)
                .setScale(0, RoundingMode.HALF_UP);
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
        comprobante.setDescuentoClienteFrecuente(calcularDescuentoClienteFrecuente(montoBase));
        comprobante.setDescuentoCumpleanos(descuentoCumpleanos);
        comprobante.setMontoBase(montoBase);
        comprobante.setMontoFinal(montoFinal);
        comprobante.setIva(iva);
        comprobante.setMontoTotalConIva(montoTotalConIva);

        return comprobantePagoRepository.save(comprobante);
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

    private BigDecimal calcularDescuentoClienteFrecuente(BigDecimal montoBase) {
        BigDecimal porcentajeDescuentoTotal = BigDecimal.ZERO;

        return procesarDescuentoClienteFrecuente( montoBase, porcentajeDescuentoTotal);
    }

    private BigDecimal procesarDescuentoClienteFrecuente(BigDecimal montoBase, BigDecimal porcentajeDescuentoTotal) {
        try {
            // Descuento del cliente principal
            // Aquí se puede agregar lógica para el descuento del cliente



            return montoBase.multiply(porcentajeDescuentoTotal).setScale(0, RoundingMode.HALF_UP);
        } catch (Exception e) {
            logger.error("Error calculando descuento cliente frecuente: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }





    private BigDecimal calcularDescuentoCumpleanos() {
        return BigDecimal.ZERO;
    }



    @Transactional
    public void enviarComprobantePorEmail(Long comprobanteId) throws EmailException {
        ComprobantePagoEntity comprobante = obtenerComprobante(comprobanteId);
        ReservaEntity reserva = comprobante.getReserva();
        ClienteEntity cliente = comprobante.getCliente();

        validarEmailCliente(cliente);

        try {
            // Generar el PDF
            byte[] pdfBytes = generarPDF(comprobante);

            // Enviar email al cliente principal
            enviarEmailCliente(cliente, reserva, pdfBytes);

            // Enviar emails a los acompañantes
            enviarEmailsAcompanantes(reserva, cliente, pdfBytes);

        } catch (IOException e) {
            throw new EmailException("Error al generar PDF: " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new EmailException("Error al enviar email: " + e.getMessage(), e);
        }
    }

    private ComprobantePagoEntity obtenerComprobante(Long comprobanteId) throws EmailException {
        Optional<ComprobantePagoEntity> optComprobante = comprobantePagoRepository.findById(comprobanteId);
        if (optComprobante.isEmpty()) {
            throw new EmailException("No se encontró el comprobante con ID: " + comprobanteId);
        }
        return optComprobante.get();
    }

    private void validarEmailCliente(ClienteEntity cliente) throws EmailException {
        if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            throw new EmailException("El cliente no tiene email registrado");
        }
    }

    private void enviarEmailCliente(ClienteEntity cliente, ReservaEntity reserva, byte[] pdfBytes) throws MessagingException {
        String mensajeCliente = String.format(
                "Estimado/a %s,%n%n" +
                        "Adjunto encontrará el comprobante de pago para su reserva en KartingRM.%n%n" +
                        "Código de reserva: %s%n" +
                        "Fecha: %s%n" +
                        "Hora: %s - %s%n%n" +
                        "Recuerde presentar este comprobante el día de su visita.%n%n" +
                        "Saludos cordiales,%n" +
                        "Equipo KartingRM",
                cliente.getNombre(),
                reserva.getIdReserva(),
                reserva.getDiaReserva().format(DATE_FORMAT),
                reserva.getHoraInicio(),
                reserva.getHoraFin()
        );

        enviarEmail(cliente.getEmail(), "Comprobante de Pago - KartingRM", mensajeCliente, pdfBytes);
    }

    private void enviarEmailsAcompanantes(ReservaEntity reserva, ClienteEntity cliente, byte[] pdfBytes) {
        if (reserva.getAcompanantes() != null && !reserva.getAcompanantes().isEmpty()) {
            for (String rutAcompanante : reserva.getAcompanantes()) {
                enviarEmailAcompanante(rutAcompanante, cliente, reserva, pdfBytes);
            }
        }
    }

    private void enviarEmailAcompanante(String rutAcompanante, ClienteEntity cliente, ReservaEntity reserva, byte[] pdfBytes) {
        try {
            Optional<ClienteEntity> optAcompanante = clienteService.getClienteById(rutAcompanante);
            if (optAcompanante.isPresent()) {
                ClienteEntity acompanante = optAcompanante.get();
                if (acompanante.getEmail() != null && !acompanante.getEmail().trim().isEmpty()) {
                    String mensajeAcompanante = crearMensajeAcompanante(acompanante, cliente, reserva);
                    enviarEmail(acompanante.getEmail(), "Comprobante de Pago - KartingRM", mensajeAcompanante, pdfBytes);
                }
            }
        } catch (Exception e) {
            logger.error("Error enviando email a acompañante {}: {}", rutAcompanante, e.getMessage());
        }
    }

    private String crearMensajeAcompanante(ClienteEntity acompanante, ClienteEntity cliente, ReservaEntity reserva) {
        return String.format(
                "Estimado/a %s,%n%n" +
                        "Adjunto encontrará el comprobante de pago para la reserva en KartingRM donde usted es acompañante.%n%n" +
                        "Reserva realizada por: %s%n" +
                        "Código de reserva: %s%n" +
                        "Fecha: %s%n" +
                        "Hora: %s - %s%n%n" +
                        "Recuerde presentar este comprobante el día de su visita.%n%n" +
                        "Saludos cordiales,%n" +
                        "Equipo KartingRM",
                acompanante.getNombre(),
                cliente.getNombre(),
                reserva.getIdReserva(),
                reserva.getDiaReserva().format(DATE_FORMAT),
                reserva.getHoraInicio(),
                reserva.getHoraFin()
        );
    }

    private byte[] generarPDF(ComprobantePagoEntity comprobante) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                generarContenidoPDF(contentStream, comprobante);
            }

            return convertirPDFAByteArray(document);
        }
    }

    private void generarContenidoPDF(PDPageContentStream contentStream, ComprobantePagoEntity comprobante) throws IOException {
        float margin = 50;
        float yPosition = 750;

        yPosition = escribirTitulo(contentStream, margin, yPosition);
        yPosition = escribirInformacionReserva(contentStream, comprobante, margin, yPosition);
        yPosition = escribirInformacionCliente(contentStream, comprobante, margin, yPosition);
        yPosition = escribirDetalleCostos(contentStream, comprobante, margin, yPosition);
        yPosition = escribirTotalFinal(contentStream, comprobante, margin, yPosition);
        escribirPieDocumento(contentStream, margin, yPosition);
    }

    private float escribirTitulo(PDPageContentStream contentStream, float margin, float yPosition) throws IOException {
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

        return yPosition;
    }

    private float escribirInformacionReserva(PDPageContentStream contentStream, ComprobantePagoEntity comprobante, float margin, float yPosition) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("INFORMACION DE LA RESERVA");
        contentStream.endText();
        yPosition -= 25;

        contentStream.setFont(PDType1Font.HELVETICA, 12);

        String[] infoReserva = {
                "Codigo de reserva: " + comprobante.getReserva().getIdReserva(),
                "Fecha: " + comprobante.getReserva().getDiaReserva().format(DATE_FORMAT),
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

        return yPosition - 10;
    }

    private float escribirInformacionCliente(PDPageContentStream contentStream, ComprobantePagoEntity comprobante, float margin, float yPosition) throws IOException {
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

        return yPosition - 30;
    }

    private float escribirDetalleCostos(PDPageContentStream contentStream, ComprobantePagoEntity comprobante, float margin, float yPosition) throws IOException {
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

        return yPosition - 10;
    }

    private float escribirTotalFinal(PDPageContentStream contentStream, ComprobantePagoEntity comprobante, float margin, float yPosition) throws IOException {
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

        return yPosition;
    }

    private void escribirPieDocumento(PDPageContentStream contentStream, float margin, float yPosition) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Este comprobante debe ser presentado el dia de su visita a KartingRM");
        contentStream.endText();
        yPosition -= 15;

        contentStream.beginText();
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Generado el: " + LocalDate.now().format(DATE_FORMAT));
        contentStream.endText();
    }

    private byte[] convertirPDFAByteArray(PDDocument document) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return baos.toByteArray();
    }

    private void enviarEmail(String destinatario, String asunto, String mensaje, byte[] pdfAdjunto) throws MessagingException {
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
        logger.info("Email enviado exitosamente a: {}", destinatario);
    }

    /**
     * Método público para generar PDF (para uso en el controlador)
     */
    public byte[] generarPDFPublico(ComprobantePagoEntity comprobante) throws IOException {
        return generarPDF(comprobante);
    }
}