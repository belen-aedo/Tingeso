package com.example.demo;

import com.example.demo.entities.ClienteEntity;
import com.example.demo.repositories.ClienteRepository;
import com.example.demo.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteEntity cliente;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cliente = new ClienteEntity();
        cliente.setRut("20401575-9");
        cliente.setNombre("joaquin");
        cliente.setEmail("joaking.alambritox@gmail.com");
        cliente.setVisitasMes(0);
        cliente.setFechaCumple(LocalDate.of(2000, 2, 4));
        cliente.setDescuentoAplicable(20);
    }

    @Test
    void testGetAllClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));
        List<ClienteEntity> clientes = clienteService.getAllClientes();
        assertEquals(1, clientes.size());
        assertEquals("joaquin", clientes.get(0).getNombre());
    }

    @Test
    void testGetClienteById() {
        when(clienteRepository.findById("20401575-9")).thenReturn(Optional.of(cliente));
        Optional<ClienteEntity> result = clienteService.getClienteById("20401575-9");
        assertTrue(result.isPresent());
        assertEquals("joaquin", result.get().getNombre());
    }

    @Test
    void testActualizarCategoriaFrecuencia() {
        cliente.setVisitasMes(6); // debería aplicar 20%
        clienteService.actualizarCategoriaFrecuencia(cliente);
        assertEquals(20, cliente.getDescuentoAplicable());
    }

    @Test
    void testIncrementarVisitaCliente() {
        when(clienteRepository.findById("20401575-9")).thenReturn(Optional.of(cliente));
        clienteService.incrementarVisitaCliente("20401575-9");
        verify(clienteRepository, times(1)).save(cliente);
        assertEquals(1, cliente.getVisitasMes());
    }

    @Test
    void testResetearVisitasMensuales() {
        ClienteEntity cliente2 = new ClienteEntity();
        cliente2.setRut("21556446-0");
        cliente2.setNombre("belen");
        cliente2.setVisitasMes(3);

        when(clienteRepository.findAll()).thenReturn(List.of(cliente, cliente2));
        clienteService.resetearVisitasMensuales();
        verify(clienteRepository, times(2)).save(any(ClienteEntity.class));
        assertEquals(0, cliente.getVisitasMes());
        assertEquals(0, cliente2.getVisitasMes());
    }

    @Test
    void testGetClienteByEmail() {
        when(clienteRepository.findByEmail("joaking.alambritox@gmail.com")).thenReturn(Optional.of(cliente));
        Optional<ClienteEntity> result = clienteService.getClienteByEmail("joaking.alambritox@gmail.com");
        assertTrue(result.isPresent());
        assertEquals("joaquin", result.get().getNombre());
    }

    @Test
    void testGetClientesByRangoVisitas() {
        when(clienteRepository.findByVisitasMesBetween(2, 5)).thenReturn(List.of(cliente));
        List<ClienteEntity> result = clienteService.getClientesByRangoVisitas(2, 5);
        assertEquals(1, result.size());
        assertEquals("joaquin", result.get(0).getNombre());
    }

    @Test
    void testSaveCliente() {
        cliente.setVisitasMes(7); // debería aplicar 30%
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        ClienteEntity result = clienteService.saveCliente(cliente);
        assertEquals(30, result.getDescuentoAplicable());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void testDeleteCliente() {
        clienteService.deleteCliente("20401575-9");
        verify(clienteRepository).deleteById("20401575-9");
    }

    @Test
    void testIncrementarVisitaCliente_NotFound() {
        when(clienteRepository.findById("20401575-9")).thenReturn(Optional.empty());
        clienteService.incrementarVisitaCliente("20401575-9");
        verify(clienteRepository, never()).save(any());
    }



}
