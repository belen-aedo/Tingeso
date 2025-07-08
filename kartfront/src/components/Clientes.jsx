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

  const cargarClientes = () => {
    clienteService.getAllClientes().then((res) => {
      setClientes(res.data);
      setMensaje('');
    });
  };

  const handleChange = (e) => {
    setNuevoCliente({ ...nuevoCliente, [e.target.name]: e.target.value });
  };

  const crearCliente = (e) => {
    e.preventDefault();
    clienteService.saveCliente(nuevoCliente).then(() => {
      setMensaje('Cliente creado con éxito');
      setTipoMensaje('exito');
      setNuevoCliente({ rut: '', nombre: '', email: '', fechaCumple: '' });
      cargarClientes();
    }).catch(() => {
      setMensaje('Error al crear cliente');
      setTipoMensaje('error');
    });
  };
  


  const eliminarCliente = (rut) => {
  const rutLimpio = rut.replace(/\./g, '').replace(/-/g, '').trim(); // Elimina puntos y guiones
  const rutFormateado = rutLimpio.slice(0, -1) + '-' + rutLimpio.slice(-1); // Agrega guion antes del dígito verificador
  console.log("Intentando eliminar cliente", rutFormateado);

  if (window.confirm('¿Seguro que deseas eliminar este cliente?')) {
    clienteService.deleteCliente(rutFormateado)
      .then(() => {
        setMensaje('Cliente eliminado');
        setTipoMensaje('exito');
        cargarClientes();
      })
      .catch((err) => {
        console.error("Error al eliminar cliente", err.response || err);
        setMensaje('Error al eliminar cliente');
        setTipoMensaje('error');
      });
  }
};


  const buscarCliente = () => {
    if (!buscarRut) {
      setMensaje('');
      return cargarClientes();
    }

    clienteService.getClienteByRut(buscarRut)
      .then((res) => {
        setClientes([res.data]);
        setMensaje('Cliente encontrado');
        setTipoMensaje('exito');
      })
      .catch(() => {
        setClientes([]);
        setMensaje('RUT erróneo');
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
      .catch(() => {
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
                style={{ padding: '6px', width: '140px' }}
              />
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
              <button type="submit" style={{ padding: '8px 16px' }}>Crear</button>
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
                    <button onClick={() => eliminarCliente(cli.rut)}>Eliminar</button>
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
