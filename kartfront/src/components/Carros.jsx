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
  const [cargando, setCargando] = useState(false);
  const [procesando, setProcesando] = useState(null); // Para mostrar qué acción se está procesando
  const [errores, setErrores] = useState({}); // Para errores de validación específicos
  const navigate = useNavigate();

  useEffect(() => {
    cargarKarts();
  }, []);

  // Función para limpiar mensajes después de un tiempo
  const mostrarMensajeTemporal = (texto, tipo, duracion = 5000) => {
    setMensaje(texto);
    setTipoMensaje(tipo);
    setTimeout(() => {
      setMensaje('');
      setTipoMensaje('');
    }, duracion);
  };

  // Función para manejar errores específicos
  const manejarError = (error, accion) => {
    let mensajeError = '';
    
    if (error.response) {
      // Error del servidor con respuesta
      switch (error.response.status) {
        case 400:
          mensajeError = `Error de validación: ${error.response.data.message || 'Datos inválidos'}`;
          break;
        case 404:
          mensajeError = `No encontrado: El kart no existe`;
          break;
        case 409:
          mensajeError = `Conflicto: Ya existe un kart con ese código`;
          break;
        case 500:
          mensajeError = `Error interno del servidor. Intente más tarde`;
          break;
        default:
          mensajeError = `Error ${error.response.status}: ${error.response.data.message || 'Error desconocido'}`;
      }
    } else if (error.request) {
      // Error de conexión
      mensajeError = 'Sin conexión al servidor. Verifique su conexión a internet';
    } else {
      // Error general
      mensajeError = `Error al ${accion}: ${error.message}`;
    }
    
    mostrarMensajeTemporal(mensajeError, 'error');
  };

  // Validaciones del formulario
  const validarFormulario = () => {
    const nuevosErrores = {};
    
    if (!nuevoKart.codigo.trim()) {
      nuevosErrores.codigo = 'El código es obligatorio';
    } else if (!/^[A-Z]\d{3}$/.test(nuevoKart.codigo)) {
      nuevosErrores.codigo = 'El código debe tener formato: K001 (una letra mayúscula seguida de 3 dígitos)';
    }
    
    if (!nuevoKart.modelo.trim()) {
      nuevosErrores.modelo = 'El modelo es obligatorio';
    } else if (nuevoKart.modelo.length < 3) {
      nuevosErrores.modelo = 'El modelo debe tener al menos 3 caracteres';
    }
    
    setErrores(nuevosErrores);
    return Object.keys(nuevosErrores).length === 0;
  };

  const cargarKarts = async () => {
    setCargando(true);
    try {
      const res = await kartService.getAll();
      setKarts(res.data);
      setMensaje('');
    } catch (error) {
      manejarError(error, 'cargar karts');
    } finally {
      setCargando(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setNuevoKart({ ...nuevoKart, [name]: value });
    
    // Limpiar error específico cuando el usuario corrige
    if (errores[name]) {
      setErrores({ ...errores, [name]: '' });
    }
  };

  const crearKart = async (e) => {
    e.preventDefault();
    
    if (!validarFormulario()) {
      mostrarMensajeTemporal('Por favor, corrija los errores en el formulario', 'error');
      return;
    }

    setProcesando('crear');
    try {
      await kartService.save(nuevoKart);
      mostrarMensajeTemporal('Kart creado exitosamente', 'exito');
      setNuevoKart({ codigo: '', modelo: '', estado: 'Disponible' });
      setErrores({});
      cargarKarts();
    } catch (error) {
      manejarError(error, 'crear kart');
    } finally {
      setProcesando(null);
    }
  };

  const cambiarEstado = async (codigo, nuevoEstado) => {
    setProcesando(`estado-${codigo}`);
    try {
      await kartService.updateEstado(codigo, nuevoEstado);
      mostrarMensajeTemporal(`Estado del kart ${codigo} actualizado a ${nuevoEstado}`, 'exito');
      cargarKarts();
    } catch (error) {
      manejarError(error, 'cambiar estado');
    } finally {
      setProcesando(null);
    }
  };

  const eliminarKart = async (codigo) => {
    const confirmar = window.confirm(
      `¿Está seguro que desea eliminar el kart ${codigo}?\n\nEsta acción no se puede deshacer.`
    );
    
    if (!confirmar) return;

    setProcesando(`eliminar-${codigo}`);
    try {
      await kartService.delete(codigo);
      mostrarMensajeTemporal(`Kart ${codigo} eliminado exitosamente`, 'exito');
      cargarKarts();
    } catch (error) {
      manejarError(error, 'eliminar kart');
    } finally {
      setProcesando(null);
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
              aria-label={mostrarMenu ? 'Ocultar menú' : 'Mostrar menú'}
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
                Sistema Arriendo Karting - Karts
              </h2>
              <div style={{
                backgroundColor: 'rgba(255,255,255,0.2)',
                padding: '4px 8px',
                borderRadius: '12px',
                fontSize: '12px',
                fontWeight: 'bold',
                marginLeft: '10px'
              }}>
                Gestión de Vehículos
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Menú lateral */}
      {mostrarMenu && (
        <>
          {/* Overlay para cerrar menú */}
          <div 
            style={{
              position: 'fixed',
              top: '70px',
              left: '200px',
              width: 'calc(100vw - 200px)',
              height: 'calc(100vh - 70px)',
              backgroundColor: 'rgba(0,0,0,0.3)',
              zIndex: 998
            }}
            onClick={() => setMostrarMenu(false)}
          />
          
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
            <nav role="navigation" aria-label="Menú principal">
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
                      cursor: 'pointer',
                      fontWeight: 'bold'
                    }}
                    aria-current="page"
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
                      cursor: 'pointer'
                    }}
                  >
                    Tarifas
                  </button>
                </li>
              </ul>
            </nav>
          </aside>
        </>
      )}

      {/* Contenido principal */}
      <main style={{
        marginTop: '90px',
        marginLeft: mostrarMenu ? '220px' : '20px',
        marginRight: '20px',
        transition: 'margin-left 0.3s ease'
      }}>
        
        

        {/* Indicador de carga */}
        {cargando && (
          <div style={{
            position: 'fixed',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            backgroundColor: 'rgba(255,255,255,0.9)',
            padding: '20px',
            borderRadius: '8px',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
            zIndex: 1001
          }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ 
                width: '40px', 
                height: '40px', 
                border: '4px solid #f3f3f3',
                borderTop: '4px solid #c62828',
                borderRadius: '50%',
                animation: 'spin 1s linear infinite',
                margin: '0 auto 10px'
              }}></div>
              <p>Cargando karts...</p>
            </div>
          </div>
        )}

        {/* Mensajes de estado */}
        {mensaje && (
          <div style={{
            backgroundColor: tipoMensaje === 'error' ? '#ffebee' : '#e8f5e8',
            color: tipoMensaje === 'error' ? '#c62828' : '#2e7d32',
            padding: '12px',
            borderRadius: '4px',
            marginBottom: '20px',
            border: `1px solid ${tipoMensaje === 'error' ? '#ffcdd2' : '#c8e6c9'}`,
            display: 'flex',
            alignItems: 'center'
          }}>
            <span style={{ marginRight: '8px' }}>
              {tipoMensaje === 'error' ? '⚠️' : '✅'}
            </span>
            {mensaje}
            <button
              onClick={() => setMensaje('')}
              style={{
                marginLeft: 'auto',
                background: 'none',
                border: 'none',
                fontSize: '16px',
                cursor: 'pointer',
                color: 'inherit'
              }}
              aria-label="Cerrar mensaje"
            >
              ✕
            </button>
          </div>
        )}

        {/* Formulario de nuevo kart */}
        <section style={{ marginBottom: '30px' }}>
          <h2 style={{ marginBottom: '15px', color: '#c62828' }}>Agregar Nuevo Kart</h2>
          <form onSubmit={crearKart} style={{ 
            backgroundColor: '#f9f9f9',
            padding: '20px',
            borderRadius: '8px',
            border: '1px solid #ddd'
          }}>
            <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', flexDirection: 'column', minWidth: '150px' }}>
                <label htmlFor="codigo" style={{ fontWeight: 'bold', marginBottom: '5px' }}>
                  Código <span style={{ color: 'red' }}>*</span>
                </label>
                <input
                  id="codigo"
                  type="text"
                  name="codigo"
                  placeholder="K001"
                  value={nuevoKart.codigo}
                  onChange={handleChange}
                  required
                  style={{
                    padding: '8px',
                    border: errores.codigo ? '2px solid #f44336' : '1px solid #ccc',
                    borderRadius: '4px',
                    fontSize: '14px'
                  }}
                  aria-describedby={errores.codigo ? 'codigo-error' : null}
                />
                {errores.codigo && (
                  <span id="codigo-error" style={{ color: '#f44336', fontSize: '12px', marginTop: '2px' }}>
                    {errores.codigo}
                  </span>
                )}
              </div>
              
              <div style={{ display: 'flex', flexDirection: 'column', minWidth: '150px' }}>
                <label htmlFor="modelo" style={{ fontWeight: 'bold', marginBottom: '5px' }}>
                  Modelo <span style={{ color: 'red' }}>*</span>
                </label>
                <input
                  id="modelo"
                  type="text"
                  name="modelo"
                  placeholder="Sodikart RT8"
                  value={nuevoKart.modelo}
                  onChange={handleChange}
                  required
                  style={{
                    padding: '8px',
                    border: errores.modelo ? '2px solid #f44336' : '1px solid #ccc',
                    borderRadius: '4px',
                    fontSize: '14px'
                  }}
                  aria-describedby={errores.modelo ? 'modelo-error' : null}
                />
                {errores.modelo && (
                  <span id="modelo-error" style={{ color: '#f44336', fontSize: '12px', marginTop: '2px' }}>
                    {errores.modelo}
                  </span>
                )}
              </div>
              
              <div style={{ display: 'flex', flexDirection: 'column', minWidth: '150px' }}>
                <label htmlFor="estado" style={{ fontWeight: 'bold', marginBottom: '5px' }}>
                  Estado Inicial
                </label>
                <select
                  id="estado"
                  name="estado"
                  value={nuevoKart.estado}
                  onChange={handleChange}
                  style={{
                    padding: '8px',
                    border: '1px solid #ccc',
                    borderRadius: '4px',
                    fontSize: '14px'
                  }}
                >
                  <option value="Disponible">Disponible</option>
                  <option value="En uso">En uso</option>
                  <option value="En mantenimiento">En mantenimiento</option>
                </select>
              </div>
              
              <div style={{ display: 'flex', alignItems: 'end' }}>
                <button 
                  type="submit" 
                  disabled={procesando === 'crear'}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: procesando === 'crear' ? '#ccc' : '#4CAF50',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: procesando === 'crear' ? 'not-allowed' : 'pointer',
                    fontSize: '14px',
                    fontWeight: 'bold'
                  }}
                >
                  {procesando === 'crear' ? 'Creando...' : 'Crear Kart'}
                </button>
              </div>
            </div>
          </form>
        </section>

        {/* Tabla de karts */}
        <section>
          <h2 style={{ marginBottom: '15px', color: '#c62828' }}>
            Lista de Karts ({karts.length})
          </h2>
          
          <div style={{ overflowX: 'auto' }}>
            <table style={{ 
              width: '100%', 
              borderCollapse: 'collapse',
              backgroundColor: 'white',
              boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
            }}>
              <thead style={{ backgroundColor: '#f2f2f2' }}>
                <tr>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #ddd' }}>Código</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #ddd' }}>Modelo</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #ddd' }}>Estado</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #ddd' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {karts.length === 0 ? (
                  <tr>
                    <td colSpan="4" style={{ 
                      textAlign: 'center', 
                      padding: '40px',
                      color: '#666',
                      fontStyle: 'italic'
                    }}>
                      {cargando ? 'Cargando karts...' : 'No hay karts registrados'}
                    </td>
                  </tr>
                ) : (
                  karts.map((k) => (
                    <tr key={k.codigo} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={{ padding: '12px', fontWeight: 'bold' }}>{k.codigo}</td>
                      <td style={{ padding: '12px' }}>{k.modelo}</td>
                      <td style={{ padding: '12px' }}>
                        <span style={{
                          ...getEstadoColor(k.estado),
                          padding: '6px 12px',
                          borderRadius: '20px',
                          fontSize: '12px',
                          fontWeight: 'bold'
                        }}>
                          {k.estado}
                        </span>
                      </td>
                      <td style={{ padding: '12px' }}>
                        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                          {/* Dropdown para cambiar estado */}
                          <select
                            onChange={(e) => cambiarEstado(k.codigo, e.target.value)}
                            value=""
                            disabled={procesando === `estado-${k.codigo}`}
                            style={{
                              padding: '8px 12px',
                              borderRadius: '4px',
                              border: '1px solid #ccc',
                              backgroundColor: procesando === `estado-${k.codigo}` ? '#ccc' : '#2196F3',
                              color: 'white',
                              cursor: procesando === `estado-${k.codigo}` ? 'not-allowed' : 'pointer',
                              fontSize: '14px'
                            }}
                            aria-label={`Cambiar estado de ${k.codigo}`}
                          >
                            <option value="" disabled>
                              {procesando === `estado-${k.codigo}` ? 'Procesando...' : 'Cambiar estado'}
                            </option>
                            {getOtrosEstados(k.estado).map(estado => (
                              <option key={estado} value={estado} style={{ backgroundColor: 'white', color: 'black' }}>
                                {estado}
                              </option>
                            ))}
                          </select>

                          {/* Botón eliminar */}
                          <button
                            onClick={() => eliminarKart(k.codigo)}
                            disabled={procesando === `eliminar-${k.codigo}`}
                            style={{
                              backgroundColor: procesando === `eliminar-${k.codigo}` ? '#ccc' : '#f44336',
                              color: 'white',
                              border: 'none',
                              padding: '8px 12px',
                              borderRadius: '4px',
                              cursor: procesando === `eliminar-${k.codigo}` ? 'not-allowed' : 'pointer',
                              fontSize: '14px'
                            }}
                            aria-label={`Eliminar ${k.codigo}`}
                          >
                            {procesando === `eliminar-${k.codigo}` ? 'Eliminando...' : 'Eliminar'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      </main>

      {/* Estilos para la animación de carga */}
      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}
      </style>
    </div>
  );
}

export default Carros;