import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import tarifaService from '../services/tarifa.service';

function Tarifas() {
  const navigate = useNavigate();
  const [tarifas, setTarifas] = useState([]);
  const [mostrarMenu, setMostrarMenu] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('');
  const [nueva, setNueva] = useState({
    numeroVueltas: '',
    tiempoMaximo: '',
    precioBase: '',
    duracionReserva: ''
  });
  const [editando, setEditando] = useState(null);
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    cargarTarifas();
  }, []);

  const cargarTarifas = () => {
    setCargando(true);
    tarifaService.getAll()
      .then((res) => {
        setTarifas(res.data);
        setCargando(false);
      })
      .catch(() => {
        setMensaje('Error al cargar tarifas');
        setTipoMensaje('error');
        setCargando(false);
      });
  };

  const mostrarMensaje = (texto, tipo) => {
    setMensaje(texto);
    setTipoMensaje(tipo);
    setTimeout(() => {
      setMensaje('');
      setTipoMensaje('');
    }, 3000);
  };

  const handleChange = (e) => {
    setNueva({ ...nueva, [e.target.name]: e.target.value });
  };

  const validarFormulario = () => {
    if (!nueva.numeroVueltas || nueva.numeroVueltas <= 0) {
      mostrarMensaje('El número de vueltas debe ser mayor a 0', 'error');
      return false;
    }
    if (!nueva.tiempoMaximo || nueva.tiempoMaximo <= 0) {
      mostrarMensaje('El tiempo máximo debe ser mayor a 0', 'error');
      return false;
    }
    if (!nueva.precioBase || nueva.precioBase <= 0) {
      mostrarMensaje('El precio base debe ser mayor a 0', 'error');
      return false;
    }
    if (!nueva.duracionReserva || nueva.duracionReserva <= 0) {
      mostrarMensaje('La duración de reserva debe ser mayor a 0', 'error');
      return false;
    }
    return true;
  };

  const crearTarifa = (e) => {
    e.preventDefault();
    
    if (!validarFormulario()) return;

    setCargando(true);
    
    if (editando) {
      tarifaService.actualizar(editando, nueva)
        .then(() => {
          mostrarMensaje('Tarifa actualizada exitosamente', 'exito');
          resetearFormulario();
          cargarTarifas();
        })
        .catch(() => {
          mostrarMensaje('Error al actualizar tarifa', 'error');
          setCargando(false);
        });
    } else {
      tarifaService.crear(nueva)
        .then(() => {
          mostrarMensaje('Tarifa creada exitosamente', 'exito');
          resetearFormulario();
          cargarTarifas();
        })
        .catch(() => {
          mostrarMensaje('Error al crear tarifa', 'error');
          setCargando(false);
        });
    }
  };

  const eliminarTarifa = (id) => {
    if (window.confirm('¿Está seguro de eliminar esta tarifa? Esta acción no se puede deshacer.')) {
      setCargando(true);
      tarifaService.eliminar(id)
        .then(() => {
          mostrarMensaje('Tarifa eliminada exitosamente', 'exito');
          cargarTarifas();
        })
        .catch(() => {
          mostrarMensaje('Error al eliminar tarifa', 'error');
          setCargando(false);
        });
    }
  };

  const cargarParaEditar = (tarifa) => {
    setEditando(tarifa.id);
    setNueva({
      numeroVueltas: tarifa.numeroVueltas,
      tiempoMaximo: tarifa.tiempoMaximo,
      precioBase: tarifa.precioBase,
      duracionReserva: tarifa.duracionReserva
    });
  };

  const resetearFormulario = () => {
    setNueva({
      numeroVueltas: '',
      tiempoMaximo: '',
      precioBase: '',
      duracionReserva: ''
    });
    setEditando(null);
    setCargando(false);
  };

  const cancelarEdicion = () => {
    resetearFormulario();
    mostrarMensaje('Edición cancelada', 'info');
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
        <div style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
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
                marginRight: '20px',
                transition: 'all 0.2s'
              }}
              onMouseOver={(e) => {
                e.target.style.backgroundColor = '#f5f5f5';
                e.target.style.transform = 'scale(1.05)';
              }}
              onMouseOut={(e) => {
                e.target.style.backgroundColor = 'white';
                e.target.style.transform = 'scale(1)';
              }}
            >
              {mostrarMenu ? '✕ Cerrar' : '☰ Menú'}
            </button>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div style={{ 
                fontSize: '24px',
                background: 'linear-gradient(45deg, #ffffff, #f0f0f0)',
                borderRadius: '50%',
                padding: '5px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
              }}>
                🏁
              </div>
              <h2 style={{ 
                margin: 0, 
                fontSize: '24px',
                fontWeight: '700',
                textShadow: '1px 1px 2px rgba(0,0,0,0.3)',
                letterSpacing: '0.5px'
              }}>
                Sistema Arriendo Karting - Tarifas
              </h2>
              <div style={{
                backgroundColor: 'rgba(255,255,255,0.2)',
                padding: '4px 8px',
                borderRadius: '12px',
                fontSize: '12px',
                fontWeight: 'bold',
                marginLeft: '10px'
              }}>
                Gestión de Precios
              </div>
            </div>
          </div>
        </div>
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
                  backgroundColor: '#4CAF50',
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
        marginTop: '90px',
        marginLeft: mostrarMenu ? '240px' : '20px',
        marginRight: '20px',
        transition: 'margin-left 0.3s ease',
        paddingBottom: '50px'
      }}>
        

        {/* Mensajes de feedback */}
        {mensaje && (
          <div style={{
            backgroundColor: tipoMensaje === 'error' ? '#ffebee' : 
                           tipoMensaje === 'exito' ? '#e8f5e8' : '#fff3e0',
            border: `1px solid ${tipoMensaje === 'error' ? '#f44336' : 
                                tipoMensaje === 'exito' ? '#4CAF50' : '#ff9800'}`,
            borderRadius: '4px',
            padding: '12px',
            marginBottom: '20px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px'
          }}>
            <span style={{ 
              fontSize: '18px',
              color: tipoMensaje === 'error' ? '#f44336' : 
                     tipoMensaje === 'exito' ? '#4CAF50' : '#ff9800'
            }}>
              {tipoMensaje === 'error' ? '⚠️' : tipoMensaje === 'exito' ? '✅' : 'ℹ️'}
            </span>
            <span style={{
              color: tipoMensaje === 'error' ? '#f44336' : 
                     tipoMensaje === 'exito' ? '#4CAF50' : '#ff9800',
              fontWeight: 'bold'
            }}>
              {mensaje}
            </span>
          </div>
        )}

        {/* Formulario de tarifa */}
        <form onSubmit={crearTarifa} style={{ 
          backgroundColor: '#fff',
          border: '1px solid #ddd',
          borderRadius: '8px',
          padding: '20px',
          marginBottom: '30px',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
        }}>
          <h4 style={{ 
            color: '#c62828',
            marginBottom: '20px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px'
          }}>
            {editando ? 'Editar Tarifa' : 'Nueva Tarifa'}
            {editando && (
              <span style={{
                backgroundColor: '#ff9800',
                color: 'white',
                padding: '4px 8px',
                borderRadius: '12px',
                fontSize: '12px',
                fontWeight: 'bold'
              }}>
                EDITANDO ID: {editando}
              </span>
            )}
          </h4>

          <div style={{ display: 'flex', gap: '15px', flexWrap: 'wrap', marginBottom: '20px' }}>
            <div style={{ display: 'flex', flexDirection: 'column', minWidth: '150px' }}>
              <label style={{ 
                fontWeight: 'bold', 
                marginBottom: '5px',
                color: '#333'
              }}>
                Número de Vueltas *
              </label>
              <input 
                type="number" 
                name="numeroVueltas" 
                placeholder="Ej: 10"
                value={nueva.numeroVueltas} 
                onChange={handleChange}
                required
                min="1"
                style={{ 
                  padding: '10px', 
                  borderRadius: '4px',
                  border: '1px solid #ddd',
                  fontSize: '14px'
                }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', minWidth: '150px' }}>
              <label style={{ 
                fontWeight: 'bold', 
                marginBottom: '5px',
                color: '#333'
              }}>
                Tiempo Máximo (min) *
              </label>
              <input 
                type="number" 
                name="tiempoMaximo" 
                placeholder="Ej: 15"
                value={nueva.tiempoMaximo} 
                onChange={handleChange}
                required
                min="1"
                style={{ 
                  padding: '10px', 
                  borderRadius: '4px',
                  border: '1px solid #ddd',
                  fontSize: '14px'
                }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', minWidth: '150px' }}>
              <label style={{ 
                fontWeight: 'bold', 
                marginBottom: '5px',
                color: '#333'
              }}>
                Precio Base ($) *
              </label>
              <input 
                type="number" 
                name="precioBase" 
                placeholder="Ej: 5000"
                value={nueva.precioBase} 
                onChange={handleChange}
                required
                min="1"
                style={{ 
                  padding: '10px', 
                  borderRadius: '4px',
                  border: '1px solid #ddd',
                  fontSize: '14px'
                }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', minWidth: '150px' }}>
              <label style={{ 
                fontWeight: 'bold', 
                marginBottom: '5px',
                color: '#333'
              }}>
                Duración Reserva (min) *
              </label>
              <input 
                type="number" 
                name="duracionReserva" 
                placeholder="Ej: 30"
                value={nueva.duracionReserva} 
                onChange={handleChange}
                required
                min="1"
                style={{ 
                  padding: '10px', 
                  borderRadius: '4px',
                  border: '1px solid #ddd',
                  fontSize: '14px'
                }}
              />
            </div>
          </div>

          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            <button 
              type="submit" 
              disabled={cargando}
              style={{
                backgroundColor: editando ? '#ff9800' : '#4CAF50',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '4px',
                cursor: cargando ? 'not-allowed' : 'pointer',
                fontSize: '16px',
                fontWeight: 'bold',
                opacity: cargando ? 0.6 : 1
              }}
            >
              {cargando ? 'Procesando...' : editando ? 'Actualizar' : 'Crear Tarifa'}
            </button>

            {editando && (
              <button 
                type="button"
                onClick={cancelarEdicion}
                style={{
                  backgroundColor: '#f44336',
                  color: 'white',
                  border: 'none',
                  padding: '12px 24px',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '16px',
                  fontWeight: 'bold'
                }}
              >
                Cancelar
              </button>
            )}
          </div>
        </form>

        {/* Tabla de tarifas */}
        <div style={{
          backgroundColor: '#fff',
          border: '1px solid #ddd',
          borderRadius: '8px',
          overflow: 'hidden',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
        }}>
          <h4 style={{ 
            backgroundColor: '#f8f9fa',
            margin: 0,
            padding: '15px 20px',
            borderBottom: '1px solid #ddd',
            color: '#c62828'
          }}>
            Tarifas Registradas ({tarifas.length})
          </h4>

          {cargando ? (
            <div style={{ textAlign: 'center', padding: '40px' }}>
              <div style={{ color: '#666' }}>Cargando tarifas...</div>
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead style={{ backgroundColor: '#f2f2f2' }}>
                <tr>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>ID</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Vueltas</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Tiempo Máx</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Precio Base</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Duración</th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '1px solid #ddd' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {tarifas.length === 0 ? (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
                      <div>No hay tarifas registradas</div>
                      <div style={{ fontSize: '14px', marginTop: '5px' }}>
                        Crea tu primera tarifa usando el formulario de arriba
                      </div>
                    </td>
                  </tr>
                ) : (
                  tarifas.map((t, index) => (
                    <tr key={t.id} style={{ 
                      backgroundColor: index % 2 === 0 ? '#fff' : '#f9f9f9',
                      borderBottom: '1px solid #eee'
                    }}>
                      <td style={{ padding: '12px', fontWeight: 'bold' }}>
                        {editando === t.id && (
                          <span style={{
                            backgroundColor: '#ff9800',
                            color: 'white',
                            padding: '2px 6px',
                            borderRadius: '8px',
                            fontSize: '10px',
                            marginRight: '5px'
                          }}>
                            EDITANDO
                          </span>
                        )}
                        #{t.id}
                      </td>
                      <td style={{ padding: '12px' }}>
                        <span style={{
                          backgroundColor: '#2196F3',
                          color: 'white',
                          padding: '4px 8px',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: 'bold'
                        }}>
                          {t.numeroVueltas} vueltas
                        </span>
                      </td>
                      <td style={{ padding: '12px' }}>
                        <span style={{ color: '#666' }}>
                          {t.tiempoMaximo} min
                        </span>
                      </td>
                      <td style={{ padding: '12px', fontWeight: 'bold', color: '#4CAF50' }}>
                        ${t.precioBase?.toLocaleString()}
                      </td>
                      <td style={{ padding: '12px' }}>
                        <span style={{ color: '#666' }}>
                          {t.duracionReserva} min
                        </span>
                      </td>
                      <td style={{ padding: '12px', textAlign: 'center' }}>
                        <div style={{ display: 'flex', gap: '5px', justifyContent: 'center' }}>
                          <button
                            onClick={() => cargarParaEditar(t)}
                            disabled={cargando}
                            style={{
                              backgroundColor: '#ff9800',
                              color: 'white',
                              border: 'none',
                              padding: '6px 12px',
                              borderRadius: '4px',
                              cursor: cargando ? 'not-allowed' : 'pointer',
                              fontSize: '12px',
                              fontWeight: 'bold',
                              opacity: cargando ? 0.6 : 1
                            }}
                          >
                            Editar
                          </button>
                          <button
                            onClick={() => eliminarTarifa(t.id)}
                            disabled={cargando}
                            style={{
                              backgroundColor: '#f44336',
                              color: 'white',
                              border: 'none',
                              padding: '6px 12px',
                              borderRadius: '4px',
                              cursor: cargando ? 'not-allowed' : 'pointer',
                              fontSize: '12px',
                              fontWeight: 'bold',
                              opacity: cargando ? 0.6 : 1
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
          )}
        </div>
        
          
        
      </main>
    </div>
  );
}

export default Tarifas;