import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import kartService from '../services/kart.service';

function Carros() {
  const [karts, setKarts] = useState([]);
  const [nuevoKart, setNuevoKart] = useState({
    codigo: '',
    modelo: '',
    estado: 'Disponible',
  });
  const [mostrarMenu, setMostrarMenu] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    cargarKarts();
  }, []);

  const cargarKarts = () => {
    kartService.getAll()
      .then((res) => setKarts(res.data))
      .catch(() => {
        setMensaje('Error al cargar karts');
        setTipoMensaje('error');
      });
  };

  const handleChange = (e) => {
    setNuevoKart({ ...nuevoKart, [e.target.name]: e.target.value });
  };

  const crearKart = (e) => {
    e.preventDefault();
    kartService.save(nuevoKart).then(() => {
      setMensaje('Kart creado exitosamente');
      setTipoMensaje('exito');
      setNuevoKart({ codigo: '', modelo: '', estado: 'Disponible' });
      cargarKarts();
    }).catch(() => {
      setMensaje('Error al crear kart');
      setTipoMensaje('error');
    });
  };

  const cambiarEstado = (codigo, nuevoEstado) => {
    kartService.updateEstado(codigo, nuevoEstado)
      .then(() => {
        setMensaje(`Estado actualizado a ${nuevoEstado}`);
        setTipoMensaje('exito');
        cargarKarts();
      })
      .catch(() => {
        setMensaje('Error al cambiar estado');
        setTipoMensaje('error');
      });
  };

  const eliminarKart = (codigo) => {
    if (window.confirm('¿Seguro que deseas eliminar este kart?')) {
      kartService.delete(codigo)
        .then(() => {
          setMensaje('Kart eliminado exitosamente');
          setTipoMensaje('exito');
          cargarKarts();
        })
        .catch(() => {
          setMensaje('Error al eliminar kart');
          setTipoMensaje('error');
        });
    }
  };

  const getEstadoColor = (estado) => {
    switch (estado) {
      case 'Disponible':
        return { backgroundColor: '#4CAF50', color: 'white' };
      case 'En uso':
        return { backgroundColor: '#FF9800', color: 'white' };
      case 'En mantenimiento':
        return { backgroundColor: '#f44336', color: 'white' };
      default:
        return { backgroundColor: '#9E9E9E', color: 'white' };
    }
  };

  const getOtrosEstados = (estadoActual) => {
    const todosEstados = ['Disponible', 'En uso', 'En mantenimiento'];
    return todosEstados.filter(estado => estado !== estadoActual);
  };

  return (
   <div style={{ fontFamily: 'Arial, sans-serif' }}>
      {/* Header fijo rojo */}
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
        boxShadow: '0 2px 10px rgba(0,0,0,0.3)'
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
          <div style={{ fontSize: '12px', opacity: 0.8 }}>
            Sistema de Gestión de Karting
          </div>
        </div>
        <h2 style={{ marginTop: '0px', fontSize: '18px' }}>Gestión de Tarifas</h2>
      </header>

      {/* Menú lateral */}
      {mostrarMenu && (
        <aside style={{
          position: 'fixed',
          top: '70px',
          left: 0,
          width: '200px',
          height: 'calc(100vh - 70px)',
          backgroundColor: '#f5f5f5',
          padding: '20px',
          boxShadow: '2px 0 10px rgba(0,0,0,0.1)',
          zIndex: 999,
          overflowY: 'auto',
          borderRight: '3px solid #c62828'
        }}>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
            <li style={{ marginBottom: '10px' }}>
              <button 
                onClick={() => navigate('/carros')}
                style={{
                  width: '100%',
                  padding: '12px',
                  backgroundColor: '#4CAF50',
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
                  padding: '12px',
                  backgroundColor: '#c62828',
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
                  padding: '12px',
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
                  padding: '12px',
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
                  padding: '12px',
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
                  padding: '12px',
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
                  padding: '12px',
                  backgroundColor: '#c62828',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontWeight: 'bold'
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
        <h3>Gestión de Karts</h3>

        {mensaje && (
          <p style={{
            color: tipoMensaje === 'error' ? 'red' : 'green',
            fontWeight: 'bold'
          }}>
            {mensaje}
          </p>
        )}

        {/* Formulario de nuevo kart */}
        <form onSubmit={crearKart} style={{ marginBottom: '20px' }}>
          <h4>Agregar nuevo kart</h4>
          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Código</label>
              <input
                type="text"
                name="codigo"
                placeholder="K001"
                value={nuevoKart.codigo}
                onChange={handleChange}
                required
                style={{ padding: '6px', width: '140px' }}
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Modelo</label>
              <input
                type="text"
                name="modelo"
                placeholder="Sodikart RT8"
                value={nuevoKart.modelo}
                onChange={handleChange}
                required
                style={{ padding: '6px', width: '140px' }}
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Estado</label>
              <select
                name="estado"
                value={nuevoKart.estado}
                onChange={handleChange}
                style={{ padding: '6px', width: '160px' }}
              >
                <option value="Disponible">Disponible</option>
                <option value="En uso">En uso</option>
                <option value="En mantenimiento">En mantenimiento</option>
              </select>
            </div>
            <div style={{ display: 'flex', alignItems: 'end' }}>
              <button type="submit" style={{ padding: '8px 16px' }}>Crear</button>
            </div>
          </div>
        </form>

        {/* Tabla de karts */}
        <table border="1" cellPadding="10" style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead style={{ backgroundColor: '#f2f2f2' }}>
            <tr>
              <th>Código</th>
              <th>Modelo</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {karts.length === 0 ? (
              <tr><td colSpan="4" style={{ textAlign: 'center' }}>No hay karts registrados</td></tr>
            ) : (
              karts.map((k) => (
                <tr key={k.codigo}>
                  <td>{k.codigo}</td>
                  <td>{k.modelo}</td>
                  <td>
                    <span style={{
                      ...getEstadoColor(k.estado),
                      padding: '5px 10px',
                      borderRadius: '15px',
                      fontSize: '12px',
                      fontWeight: 'bold'
                    }}>
                      {k.estado}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                      {/* Dropdown para cambiar estado */}
                      <select
                        onChange={(e) => cambiarEstado(k.codigo, e.target.value)}
                        value=""
                        style={{
                          padding: '8px 12px',
                          borderRadius: '4px',
                          border: '1px solid #ccc',
                          backgroundColor: '#2196F3',
                          color: 'white',
                          cursor: 'pointer',
                          fontSize: '14px'
                        }}
                      >
                        <option value="" disabled>Cambiar estado</option>
                        {getOtrosEstados(k.estado).map(estado => (
                          <option key={estado} value={estado} style={{ backgroundColor: 'white', color: 'black' }}>
                            {estado}
                          </option>
                        ))}
                      </select>

                      {/* Botón eliminar */}
                      <button
                        onClick={() => eliminarKart(k.codigo)}
                        style={{
                          backgroundColor: '#f44336',
                          color: 'white',
                          border: 'none',
                          padding: '8px 12px',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          fontSize: '14px'
                        }}
                      >
                        Eliminar
                      </button>
                    </div>
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

export default Carros;