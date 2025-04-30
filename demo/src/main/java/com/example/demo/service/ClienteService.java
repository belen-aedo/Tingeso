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
        // Cambiado de Long id a String rut para coincidir con la entidad
        return clienteRepository.findById(rut);
    }

    public Optional<ClienteEntity> getClienteByEmail(String email) {
        return clienteRepository.findByEmail(email);
    }



    public List<ClienteEntity> getClientesByRangoVisitas(int minVisitas, int maxVisitas) {
        // Nota: Necesitas asegurarte que este método exista en tu repositorio
        return clienteRepository.findByVisitasMesBetween(minVisitas, maxVisitas);
    }

    public ClienteEntity saveCliente(ClienteEntity cliente) {
        // Actualizar categoría de frecuencia y descuento según las visitas
        actualizarCategoriaFrecuencia(cliente);
        return clienteRepository.save(cliente);
    }

    public void actualizarCategoriaFrecuencia(ClienteEntity cliente) {
        int visitasMes = cliente.getVisitasMes();

        // Necesitas agregar estos campos a tu entidad ClienteEntity
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
        // Cambiado de Long clienteId a String rut
        Optional<ClienteEntity> optCliente = clienteRepository.findById(rut);
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
        clienteRepository.deleteById(rut);
    }
}