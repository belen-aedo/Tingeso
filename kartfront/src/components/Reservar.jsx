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
  const [erroresFormulario, setErroresFormulario] = useState({});
  const [acompananteInput, setAcompananteInput] = useState('');

  useEffect(() => {
    cargarReservas();
  }, []);

  // Función para validar RUT
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

  // Función para validar fecha
  const esFechaValida = (fecha) => {
    if (!fecha) return false;
    
    const hoy = new Date();
    const fechaSeleccionada = new Date(fecha);
    
    // Normalizar fechas para comparar solo día/mes/año
    hoy.setHours(0, 0, 0, 0);
    fechaSeleccionada.setHours(0, 0, 0, 0);
    
    return fechaSeleccionada >= hoy;
  };

  // Función para validar horas
  const sonHorasValidas = (horaInicio, horaFin) => {
    if (!horaInicio || !horaFin) return false;
    
    const inicio = new Date(`2000-01-01T${horaInicio}:00`);
    const fin = new Date(`2000-01-01T${horaFin}:00`);
    
    return inicio < fin;
  };

  // Función para validar formulario completo
  const validarFormulario = () => {
    const errores = {};
    
    // Validar RUT
    if (!nueva.cliente.rut.trim()) {
      errores.rut = 'El RUT es obligatorio';
    } else if (!esRutValido(nueva.cliente.rut)) {
      errores.rut = 'Error en el ingreso del RUT. Formato válido: 12345678-9';
    }
    
    // Validar fecha
    if (!nueva.diaReserva) {
      errores.diaReserva = 'La fecha es obligatoria';
    } else if (!esFechaValida(nueva.diaReserva)) {
      errores.diaReserva = 'Error en el ingreso del día';
    }
    
    // Validar hora de inicio
    if (!nueva.horaInicio) {
      errores.horaInicio = 'La hora de inicio es obligatoria';
    }
    
    // Validar hora de fin
    if (!nueva.horaFin) {
      errores.horaFin = 'La hora de fin es obligatoria';
    }
    
    // Validar relación entre horas
    if (nueva.horaInicio && nueva.horaFin) {
      if (!sonHorasValidas(nueva.horaInicio, nueva.horaFin)) {
        errores.horaInicio = 'La hora de inicio debe ser anterior a la hora de fin';
        errores.horaFin = 'La hora de fin debe ser posterior a la hora de inicio';
      }
    }
    
    // Validar duración
    if (!nueva.tiempoReserva || nueva.tiempoReserva <= 0) {
      errores.tiempoReserva = 'La duración debe ser mayor a 0 minutos';
    }
    
    return errores;
  };

  // Función para determinar si el botón debe estar habilitado
  const esFormularioValido = () => {
    return (
      nueva.cliente.rut && 
      nueva.diaReserva && 
      nueva.horaInicio && 
      nueva.horaFin && 
      nueva.tiempoReserva &&
      !Object.keys(erroresFormulario).some(key => erroresFormulario[key])
    );
  };

  // Función para mostrar mensaje temporal
  const mostrarMensajeTemporal = (texto, tipo) => {
    setMensaje(texto);
    setTipoMensaje(tipo);
    setTimeout(() => {
      setMensaje('');
      setTipoMensaje('');
    }, 5000);
  };

  // Función para manejar errores del backend
  const manejarErrorBackend = (error) => {
    if (error.response && error.response.data && error.response.data.message) {
      const errorMessage = error.response.data.message;
      
      // Mapear errores específicos del backend
      if (errorMessage.includes('Cliente no encontrado')) {
        mostrarMensajeTemporal('Error: Cliente no encontrado. Verifique el RUT ingresado.', 'error');
      } else if (errorMessage.includes('Debe especificar hora de inicio y fin')) {
        mostrarMensajeTemporal('Error: Debe especificar hora de inicio y fin', 'error');
      } else if (errorMessage.includes('La hora de inicio debe ser anterior a la hora de fin')) {
        mostrarMensajeTemporal('Error: La hora de inicio debe ser anterior a la hora de fin', 'error');
      } else if (errorMessage.includes('El horario seleccionado no está disponible')) {
        mostrarMensajeTemporal('Error: El horario seleccionado no está disponible. Ya existe una reserva que se superpone con este horario.', 'error');
      } else {
        mostrarMensajeTemporal(`Error: ${errorMessage}`, 'error');
      }
    } else {
      mostrarMensajeTemporal('Error al crear reserva. Intente nuevamente.', 'error');
    }
  };

  useEffect(() => {
    cargarReservas();
  }, []);

  const cargarReservas = () => {
    reservaService.getAll()
      .then((res) => setReservas(res.data))
      .catch(() => {
        mostrarMensajeTemporal('Error al cargar reservas', 'error');
      });
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    
    if (['rut'].includes(name)) {
      setNueva({ ...nueva, cliente: { ...nueva.cliente, rut: value } });
    } else {
      setNueva({ ...nueva, [name]: value });
    }
    
    // Limpiar error específico cuando el usuario corrige
    if (erroresFormulario[name]) {
      const nuevosErrores = { ...erroresFormulario };
      delete nuevosErrores[name];
      setErroresFormulario(nuevosErrores);
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
    
    // Validar formulario antes de enviar
    const errores = validarFormulario();
    if (Object.keys(errores).length > 0) {
      setErroresFormulario(errores);
      mostrarMensajeTemporal('Por favor, corrija los errores en el formulario', 'error');
      return;
    }
    
    // Limpiar errores previos
    setErroresFormulario({});
    
    reservaService.create(nueva)
      .then(() => {
        mostrarMensajeTemporal('Reserva creada exitosamente', 'exito');
        setNueva({
          cliente: { rut: '' },
          diaReserva: '',
          horaInicio: '',
          horaFin: '',
          tiempoReserva: '',
          acompanantes: [],
        });
        setErroresFormulario({}); // Asegurar que los errores se limpien
        cargarReservas();
      })
      .catch((error) => {
        // No establecer errores de formulario para errores del backend
        // Solo mostrar el mensaje de error
        setErroresFormulario({});
        manejarErrorBackend(error);
      });
  };

  const eliminarReserva = (id) => {
    if (window.confirm('¿Eliminar esta reserva?')) {
      reservaService.remove(id)
        .then(() => {
          mostrarMensajeTemporal('Reserva eliminada exitosamente', 'exito');
          cargarReservas();
        })
        .catch(() => {
          mostrarMensajeTemporal('Error al eliminar reserva', 'error');
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

        {/* Mensajes de estado */}
        {mensaje && (
          <div style={{
            backgroundColor: tipoMensaje === 'error' ? '#ffebee' : '#e8f5e8',
            border: `1px solid ${tipoMensaje === 'error' ? '#f44336' : '#4CAF50'}`,
            borderRadius: '4px',
            padding: '12px',
            marginBottom: '20px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px'
          }}>
            <span style={{ 
              fontSize: '18px',
              color: tipoMensaje === 'error' ? '#f44336' : '#4CAF50'
            }}>
              {tipoMensaje === 'error' ? '⚠️' : '✅'}
            </span>
            <span style={{
              color: tipoMensaje === 'error' ? '#f44336' : '#4CAF50',
              fontWeight: 'bold'
            }}>
              {mensaje}
            </span>
          </div>
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
                style={{ 
                  padding: '8px', 
                  width: '140px',
                  border: erroresFormulario.rut ? '2px solid #f44336' : '1px solid #ccc',
                  borderRadius: '4px'
                }}
              />
              {erroresFormulario.rut && (
                <small style={{ color: '#f44336', fontSize: '12px', marginTop: '2px' }}>
                  {erroresFormulario.rut}
                </small>
              )}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Fecha</label>
              <input 
                type="date" 
                name="diaReserva" 
                value={nueva.diaReserva} 
                onChange={handleChange} 
                required
                style={{ 
                  padding: '8px', 
                  width: '140px',
                  border: erroresFormulario.diaReserva ? '2px solid #f44336' : '1px solid #ccc',
                  borderRadius: '4px'
                }}
              />
              {erroresFormulario.diaReserva && (
                <small style={{ color: '#f44336', fontSize: '12px', marginTop: '2px' }}>
                  {erroresFormulario.diaReserva}
                </small>
              )}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Hora Inicio</label>
              <input 
                type="time" 
                name="horaInicio" 
                value={nueva.horaInicio} 
                onChange={handleChange} 
                required
                style={{ 
                  padding: '8px', 
                  width: '140px',
                  border: erroresFormulario.horaInicio ? '2px solid #f44336' : '1px solid #ccc',
                  borderRadius: '4px'
                }}
              />
              {erroresFormulario.horaInicio && (
                <small style={{ color: '#f44336', fontSize: '12px', marginTop: '2px' }}>
                  {erroresFormulario.horaInicio}
                </small>
              )}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Hora Fin</label>
              <input 
                type="time" 
                name="horaFin" 
                value={nueva.horaFin} 
                onChange={handleChange} 
                required
                style={{ 
                  padding: '8px', 
                  width: '140px',
                  border: erroresFormulario.horaFin ? '2px solid #f44336' : '1px solid #ccc',
                  borderRadius: '4px'
                }}
              />
              {erroresFormulario.horaFin && (
                <small style={{ color: '#f44336', fontSize: '12px', marginTop: '2px' }}>
                  {erroresFormulario.horaFin}
                </small>
              )}
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
                style={{ 
                  padding: '8px', 
                  width: '120px',
                  border: erroresFormulario.tiempoReserva ? '2px solid #f44336' : '1px solid #ccc',
                  borderRadius: '4px'
                }}
              />
              {erroresFormulario.tiempoReserva && (
                <small style={{ color: '#f44336', fontSize: '12px', marginTop: '2px' }}>
                  {erroresFormulario.tiempoReserva}
                </small>
              )}
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
            disabled={!esFormularioValido()}
            style={{
              backgroundColor: esFormularioValido() ? '#4CAF50' : '#ccc',
              color: 'white',
              border: 'none',
              padding: '12px 24px',
              borderRadius: '4px',
              cursor: esFormularioValido() ? 'pointer' : 'not-allowed',
              fontSize: '16px',
              fontWeight: 'bold',
              opacity: esFormularioValido() ? 1 : 0.6
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