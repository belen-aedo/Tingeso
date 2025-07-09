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
  const [tipoMensaje, setTipoMensaje] = useState('');
  const [mostrarMenu, setMostrarMenu] = useState(false);
  const [erroresFormulario, setErroresFormulario] = useState({});
  const [cargando, setCargando] = useState(false);
  const [operacionEnCurso, setOperacionEnCurso] = useState('');
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

  // Validación completa del formulario
  const validarFormulario = () => {
    const errores = {};
    
    if (!nuevoCliente.rut.trim()) {
      errores.rut = 'El RUT es obligatorio';
    } else if (!esRutValido(nuevoCliente.rut)) {
      errores.rut = 'El RUT ingresado no es válido. Formato: 12345678-9';
    }
    
    if (!nuevoCliente.nombre.trim()) {
      errores.nombre = 'El nombre es obligatorio';
    } else if (nuevoCliente.nombre.trim().length < 2) {
      errores.nombre = 'El nombre debe tener al menos 2 caracteres';
    }
    
    if (!nuevoCliente.email.trim()) {
      errores.email = 'El email es obligatorio';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(nuevoCliente.email)) {
      errores.email = 'El email no tiene un formato válido';
    }
    
    return errores;
  };

  // Limpiar mensajes después de un tiempo
  const mostrarMensajeTemporal = (mensaje, tipo) => {
    setMensaje(mensaje);
    setTipoMensaje(tipo);
    setTimeout(() => {
      setMensaje('');
      setTipoMensaje('');
    }, 5000);
  };

  const cargarClientes = async () => {
    setCargando(true);
    setOperacionEnCurso('Cargando clientes...');
    
    try {
      const res = await clienteService.getAllClientes();
      setClientes(res.data);
      if (res.data.length === 0) {
        mostrarMensajeTemporal('No hay clientes registrados', 'info');
      }
    } catch (error) {
      console.error('Error al cargar clientes:', error);
      mostrarMensajeTemporal('Error al cargar clientes. Intente nuevamente.', 'error');
    } finally {
      setCargando(false);
      setOperacionEnCurso('');
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setNuevoCliente({ ...nuevoCliente, [name]: value });
    
    // Limpiar error específico cuando el usuario corrige
    if (erroresFormulario[name]) {
      setErroresFormulario({ ...erroresFormulario, [name]: '' });
    }
  };

  const crearCliente = async (e) => {
    e.preventDefault();
    
    const errores = validarFormulario();
    if (Object.keys(errores).length > 0) {
      setErroresFormulario(errores);
      mostrarMensajeTemporal('Por favor, corrija los errores en el formulario', 'error');
      return;
    }

    setCargando(true);
    setOperacionEnCurso('Creando cliente...');

    try {
      const rutNormalizado = normalizarRut(nuevoCliente.rut);
      const clienteAEnviar = { ...nuevoCliente, rut: rutNormalizado };

      await clienteService.saveCliente(clienteAEnviar);
      
      mostrarMensajeTemporal('Cliente creado exitosamente', 'exito');
      setNuevoCliente({ rut: '', nombre: '', email: '', fechaCumple: '' });
      setErroresFormulario({});
      cargarClientes();
    } catch (error) {
      console.error('Error al crear cliente:', error);
      
      let errorMessage = 'Error al crear cliente. ';
      if (error.response?.status === 409) {
        errorMessage += 'El RUT ya existe en el sistema.';
      } else if (error.response?.status === 400) {
        errorMessage += 'Datos inválidos. Verifique la información ingresada.';
      } else {
        errorMessage += 'Intente nuevamente más tarde.';
      }
      
      mostrarMensajeTemporal(errorMessage, 'error');
    } finally {
      setCargando(false);
      setOperacionEnCurso('');
    }
  };

  const eliminarCliente = async (rut) => {
    if (!esRutValido(rut)) {
      mostrarMensajeTemporal('El RUT no es válido para eliminar', 'error');
      return;
    }

    const confirmacion = window.confirm(
      '¿Está seguro que desea eliminar este cliente?\n' +
      'Esta acción no se puede deshacer.'
    );
    
    if (!confirmacion) return;

    setCargando(true);
    setOperacionEnCurso('Eliminando cliente...');

    try {
      const rutNormalizado = normalizarRut(rut);
      await clienteService.deleteCliente(rutNormalizado);
      
      mostrarMensajeTemporal('Cliente eliminado correctamente', 'exito');
      cargarClientes();
    } catch (error) {
      console.error("Error al eliminar cliente:", error);
      
      let errorMessage = 'Error al eliminar cliente. ';
      if (error.response?.status === 404) {
        errorMessage += 'El cliente no existe en el sistema.';
      } else if (error.response?.status === 409) {
        errorMessage += 'No se puede eliminar porque tiene reservas asociadas.';
      } else {
        errorMessage += 'Intente nuevamente más tarde.';
      }
      
      mostrarMensajeTemporal(errorMessage, 'error');
    } finally {
      setCargando(false);
      setOperacionEnCurso('');
    }
  };

  const buscarCliente = async () => {
    if (!buscarRut.trim()) {
      mostrarMensajeTemporal('Ingrese un RUT para buscar', 'error');
      return;
    }

    if (!esRutValido(buscarRut)) {
      mostrarMensajeTemporal('El RUT ingresado no es válido. Formato: 12345678-9', 'error');
      return;
    }

    setCargando(true);
    setOperacionEnCurso('Buscando cliente...');

    try {
      const rutNormalizado = normalizarRut(buscarRut);
      const res = await clienteService.getClienteByRut(rutNormalizado);
      
      setClientes([res.data]);
      mostrarMensajeTemporal('Cliente encontrado', 'exito');
    } catch (error) {
      console.error('Error al buscar cliente:', error);
      
      setClientes([]);
      let errorMessage = 'No se encontró un cliente con ese RUT. ';
      if (error.response?.status === 404) {
        errorMessage += 'Verifique el RUT ingresado.';
      } else {
        errorMessage += 'Intente nuevamente más tarde.';
      }
      
      mostrarMensajeTemporal(errorMessage, 'error');
    } finally {
      setCargando(false);
      setOperacionEnCurso('');
    }
  };

  const limpiarBusqueda = () => {
    setBuscarRut('');
    setMensaje('');
    setTipoMensaje('');
    cargarClientes();
  };

  const resetearVisitas = async () => {
    const confirmacion = window.confirm(
      "¿Está seguro que desea resetear las visitas mensuales de todos los clientes?\n" +
      "Esta acción afectará a todos los clientes y no se puede deshacer."
    );
    
    if (!confirmacion) return;

    setCargando(true);
    setOperacionEnCurso('Reseteando visitas...');

    try {
      await clienteService.resetearVisitas();
      mostrarMensajeTemporal('Visitas reseteadas correctamente', 'exito');
      cargarClientes();
    } catch (error) {
      console.error('Error al resetear visitas:', error);
      mostrarMensajeTemporal('Error al resetear visitas. Intente nuevamente más tarde.', 'error');
    } finally {
      setCargando(false);
      setOperacionEnCurso('');
    }
  };

 // Estilos mejorados
  const estilos = {
    input: (hasError) => ({
      padding: '8px',
      border: hasError ? '2px solid #dc3545' : '1px solid #ccc',
      borderRadius: '4px',
      fontSize: '14px',
      transition: 'border-color 0.3s',
    }),
    inputFocus: {
      outline: 'none',
      borderColor: '#007bff',
      boxShadow: '0 0 0 2px rgba(0,123,255,0.25)',
    },
    errorText: {
      color: '#dc3545',
      fontSize: '12px',
      marginTop: '4px',
    },
    button: (disabled = false) => ({
      padding: '8px 16px',
      backgroundColor: disabled ? '#6c757d' : '#007bff',
      color: 'white',
      border: 'none',
      borderRadius: '4px',
      cursor: disabled ? 'not-allowed' : 'pointer',
      fontSize: '14px',
      transition: 'background-color 0.3s',
    }),
    mensaje: (tipo) => ({
      padding: '12px',
      borderRadius: '4px',
      margin: '10px 0',
      fontWeight: 'bold',
      backgroundColor: tipo === 'error' ? '#f8d7da' : tipo === 'exito' ? '#d4edda' : '#d1ecf1',
      color: tipo === 'error' ? '#721c24' : tipo === 'exito' ? '#155724' : '#0c5460',
      border: `1px solid ${tipo === 'error' ? '#f5c6cb' : tipo === 'exito' ? '#c3e6cb' : '#bee5eb'}`,
    }),
  };

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      {/* Indicador de carga global */}
      {cargando && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 9999,
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '20px',
            borderRadius: '8px',
            textAlign: 'center',
          }}>
            <div style={{ marginBottom: '10px' }}>⏳</div>
            <div>{operacionEnCurso}</div>
          </div>
        </div>
      )}

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
              aria-label={mostrarMenu ? 'Cerrar menú' : 'Abrir menú'}
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
                Sistema Arriendo Karting - Clientes
              </h2>
              <div style={{
                backgroundColor: 'rgba(255,255,255,0.2)',
                padding: '4px 8px',
                borderRadius: '12px',
                fontSize: '12px',
                fontWeight: 'bold',
                marginLeft: '10px'
              }}>
                Gestión de Usuarios
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Menú lateral */}
      {mostrarMenu && (
        <aside style={{
          position: 'fixed',
          top: '100px',
          left: 0,
          width: '200px',
          height: '100%',
          backgroundColor: '#f8f9fa',
          padding: '20px',
          boxShadow: '2px 0 5px rgba(0,0,0,0.1)',
          zIndex: 999,
        }}>
          <nav aria-label="Menú principal">
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
                    backgroundColor: '#28a745',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                  aria-current="page"
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
          </nav>
        </aside>
      )}

      {/* Contenido principal */}
      <main style={{
        marginTop: '100px',
        marginLeft: mostrarMenu ? '240px' : '20px',
        marginRight: '20px',
        transition: 'margin-left 0.3s ease'
      }}>
        {/* Sección de búsqueda */}
        <section style={{ marginBottom: '30px' }}>
          <h2>🔍 Buscar Cliente</h2>
          <div style={{ 
            display: 'flex', 
            gap: '10px', 
            alignItems: 'flex-end',
            flexWrap: 'wrap',
            marginBottom: '10px' 
          }}>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label htmlFor="buscarRut" style={{ marginBottom: '5px', fontWeight: 'bold' }}>
                RUT del Cliente
              </label>
              <input
                id="buscarRut"
                type="text"
                placeholder="Ej: 12345678-9"
                value={buscarRut}
                onChange={(e) => setBuscarRut(e.target.value)}
                style={{
                  ...estilos.input(false),
                  width: '200px'
                }}
                onKeyPress={(e) => e.key === 'Enter' && buscarCliente()}
              />
            </div>
            <button
              onClick={buscarCliente}
              style={estilos.button(false)}
              disabled={cargando}
            >
              🔍 Buscar
            </button>
            <button
              onClick={limpiarBusqueda}
              style={{
                ...estilos.button(false),
                backgroundColor: '#6c757d'
              }}
              disabled={cargando}
            >
              🧹 Limpiar
            </button>
          </div>
        </section>

        {/* Mensajes de estado */}
        {mensaje && (
          <div style={estilos.mensaje(tipoMensaje)} role="alert">
            {tipoMensaje === 'error' && '❌ '}
            {tipoMensaje === 'exito' && '✅ '}
            {tipoMensaje === 'info' && 'ℹ️ '}
            {mensaje}
          </div>
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
              <label>Fecha nacimiento</label>
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