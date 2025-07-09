import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import clienteService from '../services/clienteService';

function Clientes() {
  const [clientes, setClientes] = useState([]);
  const [nuevoCliente, setNuevoCliente] = useState({
    rut: '',
    nombre: '',
    email: '',
    fechaCumple: '',
  });
  const [buscarRut, setBuscarRut] = useState('');
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState(''); // 'error' o 'exito'
  const [mostrarMenu, setMostrarMenu] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    cargarClientes();
  }, []);

  // Función para validar RUT (misma lógica que el backend)
  const esRutValido = (rut) => {
    if (!rut || rut.trim() === '') return false;
    
    try {
      const rutLimpio = limpiarRut(rut);
      
      if (rutLimpio.length < 2) return false;
      
      const numeroStr = rutLimpio.substring(0, rutLimpio.length - 1);
      const digitoVerificador = rutLimpio.charAt(rutLimpio.length - 1);
      
      const numero = parseInt(numeroStr);
      
      if (numero < 1000000 || numero > 99999999) return false;
      
      const dvCalculado = calcularDigitoVerificador(numero);
      
      return digitoVerificador.toUpperCase() === dvCalculado.toUpperCase();
    } catch (e) {
      return false;
    }
  };

  const limpiarRut = (rut) => {
    return rut.replace(/[.\-\s]/g, '').toUpperCase();
  };

  const calcularDigitoVerificador = (numero) => {
    let suma = 0;
    let multiplicador = 2;
    
    while (numero > 0) {
      suma += (numero % 10) * multiplicador;
      numero = Math.floor(numero / 10);
      multiplicador = multiplicador === 7 ? 2 : multiplicador + 1;
    }
    
    const resto = suma % 11;
    const dv = 11 - resto;
    
    if (dv === 11) return '0';
    if (dv === 10) return 'K';
    return dv.toString();
  };

  const normalizarRut = (rut) => {
    if (!esRutValido(rut)) return null;
    
    const rutLimpio = limpiarRut(rut);
    const numero = rutLimpio.substring(0, rutLimpio.length - 1);
    const dv = rutLimpio.charAt(rutLimpio.length - 1);
    
    return numero + '-' + dv;
  };

  const cargarClientes = () => {
    clienteService.getAllClientes().then((res) => {
      setClientes(res.data);
      setMensaje('');
    }).catch((error) => {
      console.error('Error al cargar clientes:', error);
      setMensaje('Error al cargar clientes');
      setTipoMensaje('error');
    });
  };

  const handleChange = (e) => {
    setNuevoCliente({ ...nuevoCliente, [e.target.name]: e.target.value });
  };

  const crearCliente = (e) => {
    e.preventDefault();
    
    // Validar RUT antes de enviar
    if (!esRutValido(nuevoCliente.rut)) {
      setMensaje('El RUT ingresado no es válido');
      setTipoMensaje('error');
      return;
    }

    // Normalizar RUT antes de enviar
    const rutNormalizado = normalizarRut(nuevoCliente.rut);
    const clienteAEnviar = { ...nuevoCliente, rut: rutNormalizado };

    clienteService.saveCliente(clienteAEnviar).then(() => {
      setMensaje('Cliente creado con éxito');
      setTipoMensaje('exito');
      setNuevoCliente({ rut: '', nombre: '', email: '', fechaCumple: '' });
      cargarClientes();
    }).catch((error) => {
      console.error('Error al crear cliente:', error);
      const errorMessage = error.response?.data?.message || error.response?.data || 'Error al crear cliente';
      setMensaje(errorMessage);
      setTipoMensaje('error');
    });
  };

  const eliminarCliente = (rut) => {
    console.log("RUT original recibido:", rut);
    
    // Validar que el RUT sea válido antes de proceder
    if (!esRutValido(rut)) {
      setMensaje('El RUT no es válido para eliminar');
      setTipoMensaje('error');
      return;
    }

    // Normalizar el RUT (mismo formato que usa el backend)
    const rutNormalizado = normalizarRut(rut);
    console.log("RUT normalizado para eliminar:", rutNormalizado);

    if (window.confirm('¿Seguro que deseas eliminar este cliente?')) {
      clienteService.deleteCliente(rutNormalizado)
        .then(() => {
          setMensaje('Cliente eliminado correctamente');
          setTipoMensaje('exito');
          cargarClientes();
        })
        .catch((error) => {
          console.error("Error al eliminar cliente:", error.response || error);
          const errorMessage = error.response?.data || 'Error al eliminar cliente';
          setMensaje(errorMessage);
          setTipoMensaje('error');
        });
    }
  };

  const buscarCliente = () => {
    if (!buscarRut) {
      setMensaje('');
      return cargarClientes();
    }

    // Validar RUT antes de buscar
    if (!esRutValido(buscarRut)) {
      setMensaje('El RUT ingresado no es válido');
      setTipoMensaje('error');
      return;
    }

    const rutNormalizado = normalizarRut(buscarRut);
    
    clienteService.getClienteByRut(rutNormalizado)
      .then((res) => {
        setClientes([res.data]);
        setMensaje('Cliente encontrado');
        setTipoMensaje('exito');
      })
      .catch((error) => {
        console.error('Error al buscar cliente:', error);
        setClientes([]);
        setMensaje('Cliente no encontrado');
        setTipoMensaje('error');
      });
  };

  // Función para resetear visitas
  const resetearVisitas = () => {
    if (window.confirm("¿Seguro que deseas resetear las visitas mensuales de todos los clientes?")) {
      clienteService.resetearVisitas()
        .then(() => {
          setMensaje('Visitas reseteadas correctamente');
          setTipoMensaje('exito');
          cargarClientes();
        })
        .catch((error) => {
          console.error('Error al resetear visitas:', error);
          setMensaje('Error al resetear visitas');
          setTipoMensaje('error');
        });
    }
  };

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      {/* Header rojo fijo */}
      <header style={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '70px',
        backgroundColor: '#c62828',
        color: 'white',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        padding: '10px 20px',
        zIndex: 1000,
      }}>
        <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
          <button
            onClick={() => setMostrarMenu(!mostrarMenu)}
            style={{
              backgroundColor: 'white',
              color: '#c62828',
              border: 'none',
              borderRadius: '4px',
              padding: '8px 12px',
              fontWeight: 'bold',
              cursor: 'pointer',
              marginRight: '20px'
            }}
          >
            Menú
          </button>
        </div>
        <h2 style={{ marginTop: '0px' }}>Arriendo de Karting</h2>
      </header>

      {/* Menú lateral */}
      {mostrarMenu && (
        <aside style={{
          position: 'fixed',
          top: '100px',
          left: 0,
          width: '200px',
          height: '100%',
          backgroundColor: '#f5f5f5',
          padding: '20px',
          boxShadow: '2px 0 5px rgba(0,0,0,0.1)',
          zIndex: 999,
        }}>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            <li style={{ marginBottom: '10px' }}>
              <button 
                onClick={() => navigate('/carros')}
                style={{
                  width: '100%',
                  padding: '10px',
                  backgroundColor: '#c62828',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Karts
              </button>
            </li>
            <li style={{ marginBottom: '10px' }}>
              <button 
                onClick={() => navigate('/clientes')}
                style={{
                  width: '100%',
                  padding: '10px',
                  backgroundColor: '#4CAF50',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Clientes
              </button>
            </li>
            <li style={{ marginBottom: '10px' }}>
              <button 
                onClick={() => navigate('/calendario')}
                style={{
                  width: '100%',
                  padding: '10px',
                  backgroundColor: '#c62828',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Calendario
              </button>
            </li>
            <li style={{ marginBottom: '10px' }}>
              <button 
                onClick={() => navigate('/comprobantes')}
                style={{
                  width: '100%',
                  padding: '10px',
                  backgroundColor: '#c62828',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Comprobantes
              </button>
            </li>
            <li style={{ marginBottom: '10px' }}>
              <button 
                onClick={() => navigate('/reportes')}
                style={{
                  width: '100%',
                  padding: '10px',
                  backgroundColor: '#c62828',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Reportes
              </button>
            </li>
            <li style={{ marginBottom: '10px' }}>
              <button 
                onClick={() => navigate('/reservar')}
                style={{
                  width: '100%',
                  padding: '10px',
                  backgroundColor: '#c62828',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Reservas
              </button>
            </li>
            <li style={{ marginBottom: '10px' }}>
              <button 
                onClick={() => navigate('/tarifas')}
                style={{
                  width: '100%',
                  padding: '10px',
                  backgroundColor: '#c62828',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Tarifas
              </button>
            </li>
          </ul>
        </aside>
      )}

      {/* Contenido principal */}
      <main style={{
        marginTop: '130px',
        marginLeft: mostrarMenu ? '220px' : '20px',
        marginRight: '20px',
        transition: 'margin-left 0.3s ease'
      }}>
        <h3>Buscar clientes por RUT</h3>
        <div style={{ marginBottom: '10px' }}>
          <input
            type="text"
            placeholder="12345678-9"
            value={buscarRut}
            onChange={(e) => setBuscarRut(e.target.value)}
            style={{ padding: '8px', width: '200px', marginRight: '10px' }}
          />
          <button
            onClick={buscarCliente}
            style={{ padding: '8px 12px', backgroundColor: '#007bff', color: '#fff', border: 'none', borderRadius: '4px', marginRight: '5px' }}
          >
            Buscar
          </button>
          <button
            onClick={() => { setBuscarRut(''); cargarClientes(); }}
            style={{ padding: '8px 12px', backgroundColor: '#6c757d', color: '#fff', border: 'none', borderRadius: '4px' }}
          >
            Limpiar
          </button>
        </div>

        {/* Mensaje dinámico */}
        {mensaje && (
          <p style={{
            color: tipoMensaje === 'error' ? 'red' : 'green',
            fontWeight: 'bold'
          }}>
            {mensaje}
          </p>
        )}
        
        <button
          onClick={resetearVisitas}
          style={{ padding: '8px 12px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px', marginLeft: '10px' }}
        >
          Resetear Visitas Mensuales
        </button>

        {/* Formulario agregar cliente */}
        <form onSubmit={crearCliente} style={{ marginBottom: '20px' }}>
          <h3>Agregar Cliente</h3>
          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>RUT</label>
              <input
                type="text"
                name="rut"
                placeholder="12345678-9"
                value={nuevoCliente.rut}
                onChange={handleChange}
                required
                style={{ 
                  padding: '6px', 
                  width: '140px',
                  borderColor: nuevoCliente.rut && !esRutValido(nuevoCliente.rut) ? 'red' : 'initial'
                }}
              />
              {nuevoCliente.rut && !esRutValido(nuevoCliente.rut) && (
                <small style={{ color: 'red' }}>RUT inválido</small>
              )}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Nombre</label>
              <input
                type="text"
                name="nombre"
                placeholder="Juan Pérez"
                value={nuevoCliente.nombre}
                onChange={handleChange}
                required
                style={{ padding: '6px', width: '140px' }}
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Correo</label>
              <input
                type="email"
                name="email"
                placeholder="juan@example.com"
                value={nuevoCliente.email}
                onChange={handleChange}
                required
                style={{ padding: '6px', width: '180px' }}
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Fecha</label>
              <input
                type="date"
                name="fechaCumple"
                value={nuevoCliente.fechaCumple}
                onChange={handleChange}
                style={{ padding: '6px', width: '150px' }}
              />
            </div>
            <div style={{ display: 'flex', alignItems: 'end' }}>
              <button 
                type="submit" 
                style={{ 
                  padding: '8px 16px',
                  backgroundColor: esRutValido(nuevoCliente.rut) ? '#007bff' : '#ccc',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: esRutValido(nuevoCliente.rut) ? 'pointer' : 'not-allowed'
                }}
                disabled={!esRutValido(nuevoCliente.rut)}
              >
                Crear
              </button>
            </div>
          </div>
        </form>

        {/* Tabla de clientes */}
        <table border="1" cellPadding="5" style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead style={{ backgroundColor: '#f2f2f2' }}>
            <tr>
              <th>RUT</th>
              <th>Nombre</th>
              <th>Email</th>
              <th>Visitas</th>
              <th>Cumpleaños</th>
              <th>Descuento</th>
              <th>Acción</th>
            </tr>
          </thead>
          <tbody>
            {clientes.length === 0 ? (
              <tr><td colSpan="7" style={{ textAlign: 'center' }}>No hay clientes para mostrar</td></tr>
            ) : (
              clientes.map((cli) => (
                <tr key={cli.rut}>
                  <td>{cli.rut}</td>
                  <td>{cli.nombre}</td>
                  <td>{cli.email}</td>
                  <td>{cli.visitasMes}</td>
                  <td>{cli.fechaCumple}</td>
                  <td>{cli.descuentoAplicable}%</td>
                  <td>
                    <button 
                      onClick={() => eliminarCliente(cli.rut)}
                      style={{
                        backgroundColor: '#dc3545',
                        color: 'white',
                        border: 'none',
                        padding: '6px 12px',
                        borderRadius: '4px',
                        cursor: 'pointer'
                      }}
                    >
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </main>
    </div>
  );
}

export default Clientes;