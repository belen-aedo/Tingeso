package com.example.demo.service;
import com.example.demo.entities.ClienteEntity;
import com.example.demo.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteEntity> getAllClientes() {
        return clienteRepository.findAll();
    }

    public Optional<ClienteEntity> getClienteById(String rut) {
        return clienteRepository.findById(rut);
    }

    public Optional<ClienteEntity> getClienteByEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    public List<ClienteEntity> getClientesByRangoVisitas(int minVisitas, int maxVisitas) {
        return clienteRepository.findByVisitasMesBetween(minVisitas, maxVisitas);
    }

    public ClienteEntity saveCliente(ClienteEntity cliente) {
        // Validar RUT antes de guardar
        if (!esRutValido(cliente.getRut())) {
            throw new IllegalArgumentException("El RUT proporcionado no es válido: " + cliente.getRut());
        }

        // Normalizar RUT (formato estándar)
        cliente.setRut(normalizarRut(cliente.getRut()));

        // Actualizar categoría de frecuencia y descuento según las visitas
        actualizarCategoriaFrecuencia(cliente);
        return clienteRepository.save(cliente);
    }

    /**
     Valida si un RUT chileno es correcto
     * @param rut RUT a validar (puede estar con o sin puntos y guión)
     * @return true si el RUT es válido, false en caso contrario
     */
    public boolean esRutValido(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }

        try {
            // Limpiar el RUT (remover puntos, guiones y espacios)
            String rutLimpio = limpiarRut(rut);

            // Validar formato básico (debe tener al menos 2 caracteres)
            if (rutLimpio.length() < 2) {
                return false;
            }

            // Separar número y dígito verificador
            String numeroStr = rutLimpio.substring(0, rutLimpio.length() - 1);
            char digitoVerificador = rutLimpio.charAt(rutLimpio.length() - 1);

            // Validar que el número sea numérico
            int numero = Integer.parseInt(numeroStr);

            // Validar rango (RUT debe estar entre 1.000.000 y 99.999.999)
            if (numero < 1000000 || numero > 99999999) {
                return false;
            }

            // Calcular dígito verificador
            char dvCalculado = calcularDigitoVerificador(numero);

            // Comparar con el dígito verificador proporcionado
            return Character.toUpperCase(digitoVerificador) == Character.toUpperCase(dvCalculado);

        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Limpia el RUT removiendo puntos, guiones y espacios
     * @param rut RUT a limpiar
     * @return RUT limpio (solo números y dígito verificador)
     */
    private String limpiarRut(String rut) {
        return rut.replaceAll("[.\\-\\s]", "").toUpperCase();
    }

    /**
     * Calcula el dígito verificador de un RUT
     * @param numero Número del RUT (sin dígito verificador)
     * @return Dígito verificador calculado
     */
    private char calcularDigitoVerificador(int numero) {
        int suma = 0;
        int multiplicador = 2;

        while (numero > 0) {
            suma += (numero % 10) * multiplicador;
            numero /= 10;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }

        int resto = suma % 11;
        int dv = 11 - resto;

        return switch (dv) {
            case 11 -> '0';
            case 10 -> 'K';
            default -> (char) ('0' + dv);
        };
    }

    /**
     * Normaliza el formato del RUT (sin puntos, con guión antes del dígito verificador)
     * @param rut RUT a normalizar
     * @return RUT en formato normalizado (ej: 12345678-9)
     */
    public String normalizarRut(String rut) {
        if (!esRutValido(rut)) {
            throw new IllegalArgumentException("No se puede normalizar un RUT inválido: " + rut);
        }

        String rutLimpio = limpiarRut(rut);
        String numero = rutLimpio.substring(0, rutLimpio.length() - 1);
        char dv = rutLimpio.charAt(rutLimpio.length() - 1);

        return numero + "-" + dv;
    }

    /**
     * Formatea un RUT con puntos y guión (ej: 12.345.678-9)
     * @param rut RUT a formatear
     * @return RUT formateado con puntos y guión
     */
    public String formatearRut(String rut) {
        if (!esRutValido(rut)) {
            throw new IllegalArgumentException("No se puede formatear un RUT inválido: " + rut);
        }

        String rutLimpio = limpiarRut(rut);
        String numero = rutLimpio.substring(0, rutLimpio.length() - 1);
        char dv = rutLimpio.charAt(rutLimpio.length() - 1);

        // Agregar puntos cada 3 dígitos desde la derecha
        StringBuilder numeroFormateado = new StringBuilder();
        for (int i = numero.length() - 1, contador = 0; i >= 0; i--, contador++) {
            if (contador > 0 && contador % 3 == 0) {
                numeroFormateado.insert(0, ".");
            }
            numeroFormateado.insert(0, numero.charAt(i));
        }

        return numeroFormateado.toString() + "-" + dv;
    }

    /**
     * Valida y obtiene un cliente por RUT
     * @param rut RUT del cliente
     * @return Optional con el cliente si existe y el RUT es válido
     */
    public Optional<ClienteEntity> getClienteByRutValidado(String rut) {
        if (!esRutValido(rut)) {
            throw new IllegalArgumentException("El RUT proporcionado no es válido: " + rut);
        }

        String rutNormalizado = normalizarRut(rut);
        return clienteRepository.findById(rutNormalizado);
    }

    public void actualizarCategoriaFrecuencia(ClienteEntity cliente) {
        int visitasMes = cliente.getVisitasMes();

        if (visitasMes >= 7) {
            cliente.setDescuentoAplicable(30);
        } else if (visitasMes >= 5) {
            cliente.setDescuentoAplicable(20);
        } else if (visitasMes >= 2) {
            cliente.setDescuentoAplicable(10);
        } else {
            cliente.setDescuentoAplicable(0);
        }
    }

    public void incrementarVisitaCliente(String rut) {
        if (!esRutValido(rut)) {
            throw new IllegalArgumentException("El RUT proporcionado no es válido: " + rut);
        }

        String rutNormalizado = normalizarRut(rut);
        Optional<ClienteEntity> optCliente = clienteRepository.findById(rutNormalizado);

        if (optCliente.isPresent()) {
            ClienteEntity cliente = optCliente.get();
            cliente.setVisitasMes(cliente.getVisitasMes() + 1);
            actualizarCategoriaFrecuencia(cliente);
            clienteRepository.save(cliente);
        }
    }

    public void resetearVisitasMensuales() {
        List<ClienteEntity> clientes = clienteRepository.findAll();
        for (ClienteEntity cliente : clientes) {
            cliente.setVisitasMes(0);
            actualizarCategoriaFrecuencia(cliente);
            clienteRepository.save(cliente);
        }
    }

    public void deleteCliente(String rut) {
        if (!esRutValido(rut)) {
            throw new IllegalArgumentException("El RUT proporcionado no es válido: " + rut);
        }

        String rutNormalizado = normalizarRut(rut);
        clienteRepository.deleteById(rutNormalizado);
    }



}