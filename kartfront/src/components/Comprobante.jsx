import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import comprobanteService from '../services/comprobantepago.service';

function Comprobantes() {
  const navigate = useNavigate();
  const [comprobantes, setComprobantes] = useState([]);
  const [mostrarMenu, setMostrarMenu] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('');
  const [reservaId, setReservaId] = useState('');
  const [cargando, setCargando] = useState(false);
  const [procesandoEmail, setProcesandoEmail] = useState(null);

  useEffect(() => {
    cargarComprobantes();
  }, []);

  const cargarComprobantes = () => {
    setCargando(true);
    comprobanteService.getAll()
      .then((res) => {
        setComprobantes(res.data);
        setCargando(false);
      })
      .catch(() => {
        mostrarMensaje('Error al cargar comprobantes', 'error');
        setCargando(false);
      });
  };

  const mostrarMensaje = (texto, tipo) => {
    setMensaje(texto);
    setTipoMensaje(tipo);
    setTimeout(() => {
      setMensaje('');
      setTipoMensaje('');
    }, 5000);
  };

  const validarReservaId = () => {
    if (!reservaId || reservaId.trim() === '') {
      mostrarMensaje('Debe ingresar un ID de reserva válido', 'error');
      return false;
    }
    if (isNaN(reservaId) || Number(reservaId) <= 0) {
      mostrarMensaje('El ID de reserva debe ser un número válido mayor a 0', 'error');
      return false;
    }
    return true;
  };

  const generarComprobante = () => {
    if (!validarReservaId()) return;

    setCargando(true);
    comprobanteService.generarPorReserva(reservaId)
      .then(() => {
        mostrarMensaje('Comprobante generado correctamente', 'exito');
        setReservaId('');
        cargarComprobantes();
      })
      .catch((error) => {
        if (error.response?.status === 404) {
          mostrarMensaje('No se encontró una reserva con ese ID', 'error');
        } else if (error.response?.status === 400) {
          mostrarMensaje('La reserva ya tiene un comprobante generado', 'error');
        } else {
          mostrarMensaje('Error al generar comprobante. Intente nuevamente', 'error');
        }
        setCargando(false);
      });
  };

  const enviarEmail = (id, clienteEmail) => {
    if (!clienteEmail) {
      mostrarMensaje('Este comprobante no tiene un email asociado', 'error');
      return;
    }

    if (window.confirm(`¿Enviar comprobante por correo a ${clienteEmail}?`)) {
      setProcesandoEmail(id);
      comprobanteService.enviarPorEmail(id)
        .then(() => {
          mostrarMensaje('Correo enviado exitosamente', 'exito');
          setProcesandoEmail(null);
        })
        .catch(() => {
          mostrarMensaje('Error al enviar correo. Verifique la conexión', 'error');
          setProcesandoEmail(null);
        });
    }
  };

  const formatearFecha = (fecha) => {
    return new Date(fecha).toLocaleDateString('es-CL', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      generarComprobante();
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
                Sistema Arriendo Karting - Comprobantes
              </h2>
              <div style={{
                backgroundColor: 'rgba(255,255,255,0.2)',
                padding: '4px 8px',
                borderRadius: '12px',
                fontSize: '12px',
                fontWeight: 'bold',
                marginLeft: '10px'
              }}>
                Gestión de Pagos
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
                  backgroundColor: '#4CAF50',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontWeight: 'bold'
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

        {/* Formulario para generar comprobante */}
        <div style={{ 
          backgroundColor: '#fff',
          border: '1px solid #ddd',
          borderRadius: '8px',
          padding: '20px',
          marginBottom: '30px',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
        }}>
          <h4 style={{ 
            color: '#c62828',
            marginBottom: '15px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px'
          }}>
            🧾 Generar Nuevo Comprobante
          </h4>
          
          <div style={{ display: 'flex', gap: '15px', alignItems: 'end', flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', flexDirection: 'column', minWidth: '200px' }}>
              <label style={{ 
                fontWeight: 'bold', 
                marginBottom: '5px',
                color: '#333'
              }}>
                ID de Reserva *
              </label>
              <input
                type="number"
                placeholder="Ingrese el ID de la reserva"
                value={reservaId}
                onChange={(e) => setReservaId(e.target.value)}
                onKeyPress={handleKeyPress}
                min="1"
                required
                style={{ 
                  padding: '12px', 
                  borderRadius: '4px',
                  border: '1px solid #ddd',
                  fontSize: '14px',
                  width: '100%'
                }}
              />
              
            </div>
            
            <button 
              onClick={generarComprobante}
              disabled={cargando || !reservaId}
              style={{
                backgroundColor: '#4CAF50',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '4px',
                cursor: (cargando || !reservaId) ? 'not-allowed' : 'pointer',
                fontSize: '16px',
                fontWeight: 'bold',
                opacity: (cargando || !reservaId) ? 0.6 : 1,
                minWidth: '160px'
              }}
            >
              {cargando ? 'Generando...' : 'Generar Comprobante'}
            </button>
          </div>
        </div>

        {/* Tabla de comprobantes */}
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
            color: '#c62828',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}>
            <span>Comprobantes Generados ({comprobantes.length})</span>
            <button 
              onClick={cargarComprobantes}
              disabled={cargando}
              style={{
                backgroundColor: '#2196F3',
                color: 'white',
                border: 'none',
                padding: '6px 12px',
                borderRadius: '4px',
                cursor: cargando ? 'not-allowed' : 'pointer',
                fontSize: '12px',
                opacity: cargando ? 0.6 : 1
              }}
            >
              {cargando ? 'Cargando...' : '🔄 Actualizar'}
            </button>
          </h4>

          {cargando ? (
            <div style={{ textAlign: 'center', padding: '40px' }}>
              <div style={{ color: '#666', fontSize: '16px' }}>
                <span style={{ marginRight: '10px' }}>⏳</span>
                Cargando comprobantes...
              </div>
            </div>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead style={{ backgroundColor: '#f2f2f2' }}>
                  <tr>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>ID</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Reserva</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Cliente</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Email</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Total</th>
                    
                    <th style={{ padding: '12px', textAlign: 'center', borderBottom: '1px solid #ddd' }}>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {comprobantes.length === 0 ? (
                    <tr>
                      <td colSpan="7" style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
                        <div style={{ fontSize: '48px', marginBottom: '10px' }}>📄</div>
                        <div style={{ fontSize: '16px', fontWeight: 'bold', marginBottom: '5px' }}>
                          No hay comprobantes generados
                        </div>
                        <div style={{ fontSize: '14px' }}>
                          Genere su primer comprobante usando el formulario de arriba
                        </div>
                      </td>
                    </tr>
                  ) : (
                    comprobantes.map((c, index) => (
                      <tr key={c.id} style={{ 
                        backgroundColor: index % 2 === 0 ? '#fff' : '#f9f9f9',
                        borderBottom: '1px solid #eee'
                      }}>
                        <td style={{ padding: '12px', fontWeight: 'bold' }}>
                          <span style={{
                            backgroundColor: '#2196F3',
                            color: 'white',
                            padding: '4px 8px',
                            borderRadius: '12px',
                            fontSize: '12px'
                          }}>
                            #{c.id}
                          </span>
                        </td>
                        <td style={{ padding: '12px' }}>
                          <span style={{ fontWeight: 'bold' }}>
                            #{c.reserva?.idReserva || 'N/A'}
                          </span>
                        </td>
                        <td style={{ padding: '12px' }}>
                          <span style={{ fontWeight: 'bold', color: '#333' }}>
                            {c.cliente?.nombre || 'Cliente no disponible'}
                          </span>
                        </td>
                        <td style={{ padding: '12px' }}>
                          <span style={{ color: '#666', fontSize: '12px' }}>
                            {c.cliente?.email || 'Sin email'}
                          </span>
                        </td>
                        <td style={{ padding: '12px', fontWeight: 'bold', color: '#4CAF50', fontSize: '16px' }}>
                          ${c.montoTotalConIva?.toLocaleString('es-CL') || '0'}
                        </td>
                        
                        <td style={{ padding: '12px', textAlign: 'center' }}>
                          <button
                            onClick={() => enviarEmail(c.id, c.cliente?.email)}
                            disabled={procesandoEmail === c.id || !c.cliente?.email}
                            style={{
                              backgroundColor: c.cliente?.email ? '#FF9800' : '#ccc',
                              color: 'white',
                              border: 'none',
                              padding: '8px 16px',
                              borderRadius: '4px',
                              cursor: (procesandoEmail === c.id || !c.cliente?.email) ? 'not-allowed' : 'pointer',
                              fontSize: '12px',
                              fontWeight: 'bold',
                              opacity: (procesandoEmail === c.id) ? 0.6 : 1,
                              minWidth: '100px'
                            }}
                            title={!c.cliente?.email ? 'Este cliente no tiene email registrado' : 'Enviar comprobante por email'}
                          >
                            {procesandoEmail === c.id ? 'Enviando...' : 
                             !c.cliente?.email ? 'Sin Email' : '📧 Enviar PDF'}
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

export default Comprobantes;