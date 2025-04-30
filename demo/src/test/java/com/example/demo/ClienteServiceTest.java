package com.example.demo;

import com.example.demo.entities.ClienteEntity;
import com.example.demo.repositories.ClienteRepository;
import com.example.demo.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ClienteServiceTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    private ClienteEntity clienteBase;
    private ClienteEntity clienteExtra;

    @BeforeEach
    void setup() {
        clienteRepository.deleteAll();

        // Cliente principal
        clienteBase = new ClienteEntity();
        clienteBase.setRut("21556446-0");
        clienteBase.setNombre("belen aedo");
        clienteBase.setEmail("belen.aedo@usach.cl");
        clienteBase.setVisitasMes(3);
        clienteBase.setDescuentoAplicable(0);
        clienteRepository.save(clienteBase);

        // Cliente adicional para pruebas (usado en incrementar, borrar, etc.)
        clienteExtra = new ClienteEntity();
        clienteExtra.setRut("12345678-9");
        clienteExtra.setNombre("Juan Perez");
        clienteExtra.setEmail("juan@example.com");
        clienteExtra.setVisitasMes(3);
        clienteExtra.setDescuentoAplicable(0);
        clienteRepository.save(clienteExtra);
    }

    @Test
    void whenGetAllClientes_thenReturnList() {
        List<ClienteEntity> clientes = clienteService.getAllClientes();
        assertThat(clientes).hasSize(2); // ahora hay 2 clientes
    }

    @Test
    void whenGetClienteById_thenReturnCliente() {
        Optional<ClienteEntity> found = clienteService.getClienteById("21556446-0");
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("belen aedo");
    }

    @Test
    void whenGetClienteByEmail_thenReturnCliente() {
        Optional<ClienteEntity> found = clienteService.getClienteByEmail("belen.aedo@usach.cl");
        assertThat(found).isPresent();
        assertThat(found.get().getRut()).isEqualTo("21556446-0");
    }

    @Test
    void whenGetClientesByRangoVisitas_thenReturnList() {
        List<ClienteEntity> clientes = clienteService.getClientesByRangoVisitas(2, 5);
        assertThat(clientes).hasSize(2); // ambos clientes tienen 3 visitas
    }

    @Test
    void whenSaveCliente_thenDescuentoIsUpdated() {
        ClienteEntity nuevo = new ClienteEntity();
        nuevo.setRut("98765432-1");
        nuevo.setNombre("Maria Lopez");
        nuevo.setEmail("maria@example.com");
        nuevo.setVisitasMes(6); // debe generar 20% de descuento

        ClienteEntity saved = clienteService.saveCliente(nuevo);
        assertThat(saved.getDescuentoAplicable()).isEqualTo(20);
    }

    @Test
    void whenIncrementarVisita_thenUpdateVisitas() {
        clienteService.incrementarVisitaCliente("12345678-9");
        Optional<ClienteEntity> updated = clienteRepository.findById("12345678-9");
        assertThat(updated).isPresent();
        assertThat(updated.get().getVisitasMes()).isEqualTo(4); // antes tenía 3
    }

    @Test
    void whenResetearVisitas_thenAllSetToZero() {
        clienteService.resetearVisitasMensuales();
        List<ClienteEntity> clientes = clienteRepository.findAll();
        for (ClienteEntity c : clientes) {
            assertThat(c.getVisitasMes()).isZero();
        }
    }

    @Test
    void whenDeleteCliente_thenNotFound() {
        clienteService.deleteCliente("12345678-9");
        Optional<ClienteEntity> deleted = clienteRepository.findById("12345678-9");
        assertThat(deleted).isEmpty();
    }
}
