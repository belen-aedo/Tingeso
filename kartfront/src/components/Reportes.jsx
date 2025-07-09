import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import reporteService from '../services/reporte.service';

function Reportes() {
  const navigate = useNavigate();
  const [reportes, setReportes] = useState([]);
  const [mostrarMenu, setMostrarMenu] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('');
  const [filtroMensual, setFiltroMensual] = useState('PorVueltas');
  const [filtroIndividual, setFiltroIndividual] = useState('Todos');

  const [nuevo, setNuevo] = useState({
    tipoReporte: '',
    mesGenerado: '',
    ingresoTotal: '',
    numeroVueltas: '',
    tiempoMaximo: '',
    minPersonas: '',
    maxPersonas: ''
  });

  useEffect(() => {
    cargarReportes();
  }, []);

  const cargarReportes = () => {
    reporteService.getAll()
      .then((res) => {
        console.log('Reportes cargados:', res.data);
        setReportes(res.data);
      })
      .catch((error) => {
        console.error('Error al cargar reportes:', error);
        setMensaje('Error al cargar reportes: ' + (error.response?.data || error.message));
        setTipoMensaje('error');
      });
  };

  const handleChange = (e) => {
    setNuevo({ ...nuevo, [e.target.name]: e.target.value });
  };

  const crearReporte = (e) => {
    e.preventDefault();
    
    // Validaciones adicionales
    if (!nuevo.tipoReporte || !nuevo.mesGenerado || !nuevo.ingresoTotal) {
      setMensaje('Por favor complete todos los campos requeridos');
      setTipoMensaje('error');
      return;
    }

    if (nuevo.tipoReporte === 'PorVueltas' && (!nuevo.numeroVueltas || !nuevo.tiempoMaximo)) {
      setMensaje('Para reportes por vueltas, complete número de vueltas y tiempo máximo');
      setTipoMensaje('error');
      return;
    }

    if (nuevo.tipoReporte === 'PorPersonas' && (!nuevo.minPersonas || !nuevo.maxPersonas)) {
      setMensaje('Para reportes por personas, complete mínimo y máximo de personas');
      setTipoMensaje('error');
      return;
    }

    console.log('Datos a enviar:', nuevo);
    
    reporteService.crear(nuevo)
      .then((response) => {
        console.log('Reporte creado exitosamente:', response.data);
        setMensaje('Reporte creado exitosamente');
        setTipoMensaje('exito');
        setNuevo({
          tipoReporte: '',
          mesGenerado: '',
          ingresoTotal: '',
          numeroVueltas: '',
          tiempoMaximo: '',
          minPersonas: '',
          maxPersonas: ''
        });
        cargarReportes();
      })
      .catch((error) => {
        console.error('Error al crear reporte:', error);
        setMensaje('Error al crear reporte: ' + (error.response?.data || error.message));
        setTipoMensaje('error');
      });
  };

  const eliminarReporte = (id) => {
    if (window.confirm('¿Eliminar este reporte?')) {
      reporteService.eliminar(id)
        .then(() => {
          setMensaje('Reporte eliminado exitosamente');
          setTipoMensaje('exito');
          cargarReportes();
        })
        .catch((error) => {
          console.error('Error al eliminar reporte:', error);
          setMensaje('Error al eliminar reporte: ' + (error.response?.data || error.message));
          setTipoMensaje('error');
        });
    }
  };

  // Reportes individuales (todos los tipos con filtro)
  const reportesIndividuales = filtroIndividual === 'Todos' 
    ? reportes 
    : reportes.filter(r => r.tipoReporte === filtroIndividual);

  // Reportes mensuales agrupados por mes
  const mensualesFiltrados = reportes.filter(r => r.tipoReporte === filtroMensual);
  const reportesMensuales = Object.values(
    mensualesFiltrados.reduce((acc, r) => {
      // Usar 'fecha' en lugar de 'mesGenerado'
      const mes = r.fecha ? r.fecha.slice(0, 7) : 'Sin fecha'; // YYYY-MM
      if (!acc[mes]) {
        acc[mes] = {
          mes,
          ingresoTotal: 0,
          tipoReporte: r.tipoReporte,
        };
      }
      acc[mes].ingresoTotal += parseFloat(r.ingresoTotal) || 0;
      return acc;
    }, {})
  );

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
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
            {['carros', 'clientes', 'calendario', 'comprobantes', 'reportes', 'reservar', 'tarifas'].map((ruta) => (
              <li key={ruta} style={{ marginBottom: '10px' }}>
                <button
                  onClick={() => navigate(`/${ruta}`)}
                  style={{
                    width: '100%',
                    padding: '10px',
                    backgroundColor: ruta === 'reportes' ? '#4CAF50' : '#c62828',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  {ruta.charAt(0).toUpperCase() + ruta.slice(1)}
                </button>
              </li>
            ))}
          </ul>
        </aside>
      )}

      <main style={{
        marginTop: '130px',
        marginLeft: mostrarMenu ? '220px' : '20px',
        marginRight: '20px',
        transition: 'margin-left 0.3s ease'
      }}>
        <h3>Gestión de Reportes</h3>

        {mensaje && (
          <div style={{
            padding: '10px',
            marginBottom: '20px',
            borderRadius: '4px',
            backgroundColor: tipoMensaje === 'error' ? '#ffebee' : '#e8f5e8',
            color: tipoMensaje === 'error' ? '#c62828' : '#2e7d32',
            border: `1px solid ${tipoMensaje === 'error' ? '#ffcdd2' : '#c8e6c9'}`,
            fontWeight: 'bold'
          }}>
            {mensaje}
          </div>
        )}

        {/* Formulario */}
        <form onSubmit={crearReporte} style={{ marginBottom: '30px' }}>
          <h4>Nuevo Reporte</h4>
          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '20px' }}>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Tipo de Reporte</label>
              <select name="tipoReporte" value={nuevo.tipoReporte} onChange={handleChange} required style={{ padding: '8px', width: '160px' }}>
                <option value="">Selecciona tipo</option>
                <option value="PorVueltas">Por Vueltas</option>
                <option value="PorPersonas">Por Personas</option>
              </select>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Fecha del Reporte</label>
              <input type="date" name="mesGenerado" value={nuevo.mesGenerado} onChange={handleChange} required style={{ padding: '8px', width: '160px' }} />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <label>Ingreso Total</label>
              <input type="number" name="ingresoTotal" placeholder="Ingreso Total" value={nuevo.ingresoTotal} onChange={handleChange} required style={{ padding: '8px', width: '140px' }} />
            </div>
          </div>

          {nuevo.tipoReporte === 'PorVueltas' && (
            <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '20px' }}>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <label>Número de Vueltas</label>
                <input type="number" name="numeroVueltas" placeholder="N° Vueltas" value={nuevo.numeroVueltas} onChange={handleChange} required style={{ padding: '8px', width: '140px' }} />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <label>Tiempo Máximo (min)</label>
                <input type="number" name="tiempoMaximo" placeholder="Tiempo Máximo" value={nuevo.tiempoMaximo} onChange={handleChange} required style={{ padding: '8px', width: '140px' }} />
              </div>
            </div>
          )}

          {nuevo.tipoReporte === 'PorPersonas' && (
            <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '20px' }}>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <label>Mínimo Personas</label>
                <input type="number" name="minPersonas" placeholder="Min Personas" value={nuevo.minPersonas} onChange={handleChange} required style={{ padding: '8px', width: '140px' }} />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <label>Máximo Personas</label>
                <input type="number" name="maxPersonas" placeholder="Max Personas" value={nuevo.maxPersonas} onChange={handleChange} required style={{ padding: '8px', width: '140px' }} />
              </div>
            </div>
          )}

          <button type="submit" style={{
            backgroundColor: '#4CAF50',
            color: 'white',
            border: 'none',
            padding: '10px 20px',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '16px',
            fontWeight: 'bold'
          }}>
            Crear Reporte
          </button>
        </form>

        {/* Filtro de tipo mensual */}
        <div style={{ marginBottom: '10px' }}>
          <strong>Mostrar en reportes mensuales:</strong>{' '}
          <button 
            onClick={() => setFiltroMensual('PorVueltas')} 
            style={{ 
              marginRight: '10px',
              backgroundColor: filtroMensual === 'PorVueltas' ? '#4CAF50' : '#f0f0f0',
              color: filtroMensual === 'PorVueltas' ? 'white' : 'black',
              border: 'none',
              padding: '5px 10px',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Por Vueltas
          </button>
          <button 
            onClick={() => setFiltroMensual('PorPersonas')}
            style={{ 
              backgroundColor: filtroMensual === 'PorPersonas' ? '#4CAF50' : '#f0f0f0',
              color: filtroMensual === 'PorPersonas' ? 'white' : 'black',
              border: 'none',
              padding: '5px 10px',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Por Personas
          </button>
        </div>

        {/* Reportes Mensuales */}
        <h4>Reportes Mensuales</h4>
        <table border="1" cellPadding="10" style={{ width: '100%', marginBottom: '40px', borderCollapse: 'collapse' }}>
          <thead style={{ backgroundColor: '#f2f2f2' }}>
            <tr>
              <th>Mes</th>
              <th>Ingreso Total</th>
              <th>Tipo</th>
            </tr>
          </thead>
          <tbody>
            {reportesMensuales.length === 0 ? (
              <tr><td colSpan="3" style={{ textAlign: 'center' }}>No hay reportes</td></tr>
            ) : (
              reportesMensuales.map((r, i) => (
                <tr key={i}>
                  <td>{r.mes}</td>
                  <td>${r.ingresoTotal.toLocaleString()}</td>
                  <td>{r.tipoReporte}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {/* Reportes Individuales */}
        <h4>Reportes Individuales</h4>
        
        {/* Filtro para reportes individuales */}
        <div style={{ marginBottom: '10px' }}>
          <strong>Mostrar reportes individuales:</strong>{' '}
          <button 
            onClick={() => setFiltroIndividual('Todos')} 
            style={{ 
              marginRight: '10px',
              backgroundColor: filtroIndividual === 'Todos' ? '#4CAF50' : '#f0f0f0',
              color: filtroIndividual === 'Todos' ? 'white' : 'black',
              border: 'none',
              padding: '5px 10px',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Todos
          </button>
          <button 
            onClick={() => setFiltroIndividual('PorVueltas')} 
            style={{ 
              marginRight: '10px',
              backgroundColor: filtroIndividual === 'PorVueltas' ? '#4CAF50' : '#f0f0f0',
              color: filtroIndividual === 'PorVueltas' ? 'white' : 'black',
              border: 'none',
              padding: '5px 10px',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Por Vueltas
          </button>
          <button 
            onClick={() => setFiltroIndividual('PorPersonas')}
            style={{ 
              backgroundColor: filtroIndividual === 'PorPersonas' ? '#4CAF50' : '#f0f0f0',
              color: filtroIndividual === 'PorPersonas' ? 'white' : 'black',
              border: 'none',
              padding: '5px 10px',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Por Personas
          </button>
        </div>
        <table border="1" cellPadding="10" style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead style={{ backgroundColor: '#f2f2f2' }}>
            <tr>
              <th>ID</th>
              <th>Tipo</th>
              <th>Fecha</th>
              <th>Ingreso</th>
              <th>Detalles</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {reportesIndividuales.length === 0 ? (
              <tr><td colSpan="6" style={{ textAlign: 'center' }}>No hay reportes</td></tr>
            ) : (
              reportesIndividuales.map(r => (
                <tr key={r.id}>
                  <td>{r.id}</td>
                  <td>{r.tipoReporte}</td>
                  <td>{r.fecha}</td>
                  <td>${r.ingresoTotal?.toLocaleString()}</td>
                  <td>
                    {r.tipoReporte === 'PorVueltas' 
                      ? `${r.numeroVueltas} vueltas - ${r.tiempoMaximo} min`
                      : `${r.minPersonas} - ${r.maxPersonas} personas`
                    }
                  </td>
                  <td>
                    <button
                      onClick={() => eliminarReporte(r.id)}
                      style={{
                        backgroundColor: '#f44336',
                        color: 'white',
                        border: 'none',
                        padding: '6px 10px',
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

export default Reportes;