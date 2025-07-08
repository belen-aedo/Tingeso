import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import reservaService from '../services/reserva.service';

function Reservar() {
  const navigate = useNavigate(); 
  const [reservas, setReservas] = useState([]);
  const [mostrarMenu, setMostrarMenu] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('');
  const [nueva, setNueva] = useState({
    cliente: { rut: '' },
    diaReserva: '',
    horaInicio: '',
    horaFin: '',
    tiempoReserva: '',
    acompanantes: [],
  });

  const [acompananteInput, setAcompananteInput] = useState('');

  useEffect(() => {
    cargarReservas();
  }, []);

  const cargarReservas = () => {
    reservaService.getAll()
      .then((res) => setReservas(res.data))
      .catch(() => {
        setMensaje('Error al cargar reservas');
        setTipoMensaje('error');
      });
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (['rut'].includes(name)) {
      setNueva({ ...nueva, cliente: { ...nueva.cliente, rut: value } });
    } else {
      setNueva({ ...nueva, [name]: value });
    }
  };

  const agregarAcompanante = () => {
    if (acompananteInput.trim()) {
      setNueva({
        ...nueva,
        acompanantes: [...nueva.acompanantes, acompananteInput.trim()],
      });
      setAcompananteInput('');
    }
  };

  const quitarAcompanante = (index) => {
    const nuevosAcompanantes = nueva.acompanantes.filter((_, i) => i !== index);
    setNueva({ ...nueva, acompanantes: nuevosAcompanantes });
  };

  const crearReserva = (e) => {
    e.preventDefault();
    reservaService.create(nueva)
      .then(() => {
        setMensaje('Reserva creada exitosamente');
        setTipoMensaje('exito');
        setNueva({
          cliente: { rut: '' },
          diaReserva: '',
          horaInicio: '',
          horaFin: '',
          tiempoReserva: '',
          acompanantes: [],
        });
        cargarReservas();
      })
      .catch(() => {
        setMensaje('Error al crear reserva');
        setTipoMensaje('error');
      });
  };

  const eliminarReserva = (id) => {
    if (window.confirm('¿Eliminar esta reserva?')) {
      reservaService.delete(id)
        .then(() => {
          setMensaje('Reserva eliminada exitosamente');
          setTipoMensaje('exito');
          cargarReservas();
        })
        .catch(() => {
          setMensaje('Error al eliminar reserva');
          setTipoMensaje('error');
        });
    }
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
                  backgroundColor: '#4CAF50',
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
        <h3>Gestión de Reservas</h3>

        {mensaje && (
          <p style={{
            color: tipoMensaje === 'error' ? 'red' : 'green',
            fontWeight: 'bold',
            marginBottom: '20px'
          }}>
            {mensaje}
          </p>
        )}

        {/* Formulario de nueva reserva */}
        <form onSubmit={crearReserva} style={{ marginBottom: '30px' }}>
          <h4>Nueva Reserva</h4>
          
          {/* Primera fila de campos */}
          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '15px' }}>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>RUT Cliente</label>
              <input 
                type="text" 
                name="rut" 
                placeholder="12345678-9" 
                value={nueva.cliente.rut} 
                onChange={handleChange} 
                required
                style={{ padding: '8px', width: '140px' }}
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Fecha</label>
              <input 
                type="date" 
                name="diaReserva" 
                value={nueva.diaReserva} 
                onChange={handleChange} 
                required
                style={{ padding: '8px', width: '140px' }}
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Hora Inicio</label>
              <input 
                type="time" 
                name="horaInicio" 
                value={nueva.horaInicio} 
                onChange={handleChange} 
                required
                style={{ padding: '8px', width: '140px' }}
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Hora Fin</label>
              <input 
                type="time" 
                name="horaFin" 
                value={nueva.horaFin} 
                onChange={handleChange} 
                required
                style={{ padding: '8px', width: '140px' }}
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Duración (min)</label>
              <input 
                type="number" 
                name="tiempoReserva" 
                placeholder="60" 
                value={nueva.tiempoReserva} 
                onChange={handleChange} 
                required
                style={{ padding: '8px', width: '120px' }}
              />
            </div>
          </div>

          {/* Sección de acompañantes */}
          <div style={{ marginBottom: '20px' }}>
            <h5>Acompañantes</h5>
            <div style={{ display: 'flex', gap: '10px', alignItems: 'end', marginBottom: '10px' }}>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <label>RUT Acompañante</label>
                <input 
                  type="text" 
                  placeholder="12345678-9" 
                  value={acompananteInput} 
                  onChange={(e) => setAcompananteInput(e.target.value)} 
                  style={{ padding: '8px', width: '140px' }}
                />
              </div>
              <button 
                type="button" 
                onClick={agregarAcompanante}
                style={{
                  backgroundColor: '#2196F3',
                  color: 'white',
                  border: 'none',
                  padding: '8px 12px',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Añadir
              </button>
            </div>
            
            {/* Lista de acompañantes */}
            {nueva.acompanantes.length > 0 && (
              <div style={{ 
                backgroundColor: '#f5f5f5', 
                padding: '10px', 
                borderRadius: '4px',
                border: '1px solid #ddd'
              }}>
                <strong>Acompañantes agregados:</strong>
                <div style={{ marginTop: '5px' }}>
                  {nueva.acompanantes.map((acompanante, index) => (
                    <span 
                      key={index}
                      style={{
                        display: 'inline-block',
                        backgroundColor: '#e3f2fd',
                        padding: '4px 8px',
                        margin: '2px',
                        borderRadius: '12px',
                        fontSize: '12px'
                      }}
                    >
                      {acompanante}
                      <button
                        type="button"
                        onClick={() => quitarAcompanante(index)}
                        style={{
                          backgroundColor: '#f44336',
                          color: 'white',
                          border: 'none',
                          borderRadius: '50%',
                          width: '16px',
                          height: '16px',
                          fontSize: '10px',
                          marginLeft: '5px',
                          cursor: 'pointer'
                        }}
                      >
                        ×
                      </button>
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>

          <button 
            type="submit" 
            style={{
              backgroundColor: '#4CAF50',
              color: 'white',
              border: 'none',
              padding: '12px 24px',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: 'bold'
            }}
          >
            Crear Reserva
          </button>
        </form>

        {/* Tabla de reservas */}
        <table border="1" cellPadding="10" style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead style={{ backgroundColor: '#f2f2f2' }}>
            <tr>
              <th>ID</th>
              <th>Cliente</th>
              <th>Fecha</th>
              <th>Hora Inicio</th>
              <th>Hora Fin</th>
              <th>Duración</th>
              <th>Acompañantes</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {reservas.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', padding: '20px' }}>
                  No hay reservas registradas
                </td>
              </tr>
            ) : (
              reservas.map((r) => (
                <tr key={r.idReserva}>
                  <td>{r.idReserva}</td>
                  <td style={{ fontWeight: 'bold' }}>{r.cliente?.rut}</td>
                  <td>{r.diaReserva}</td>
                  <td>{r.horaInicio}</td>
                  <td>{r.horaFin}</td>
                  <td>
                    <span style={{
                      backgroundColor: '#e3f2fd',
                      padding: '4px 8px',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: 'bold'
                    }}>
                      {r.tiempoReserva} min
                    </span>
                  </td>
                  <td>
                    {r.acompanantes && r.acompanantes.length > 0 ? (
                      <div>
                        {r.acompanantes.map((acomp, index) => (
                          <span 
                            key={index}
                            style={{
                              display: 'inline-block',
                              backgroundColor: '#e8f5e8',
                              padding: '2px 6px',
                              margin: '1px',
                              borderRadius: '8px',
                              fontSize: '11px'
                            }}
                          >
                            {acomp}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <span style={{ color: '#999', fontStyle: 'italic' }}>Sin acompañantes</span>
                    )}
                  </td>
                  <td>
                    <button
                      onClick={() => eliminarReserva(r.idReserva)}
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

export default Reservar;