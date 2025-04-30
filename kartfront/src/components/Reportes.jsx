import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import reporteService from '../services/reporte.service';

function Reportes() {
  const navigate = useNavigate(); 
  const [reportes, setReportes] = useState([]);
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
    reporteService.getAll().then((res) => setReportes(res.data));
  };

  const handleChange = (e) => {
    setNuevo({ ...nuevo, [e.target.name]: e.target.value });
  };

  const crearReporte = (e) => {
    e.preventDefault();
    reporteService.crear(nuevo).then(() => {
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
    });
  };

  const eliminarReporte = (id) => {
    if (window.confirm('¿Eliminar este reporte?')) {
      reporteService.eliminar(id).then(cargarReportes);
    }
  };

  return (
    <div className="container">
      {/* Botones de navegación */}
      <div>
        <button onClick={() => navigate('/carros')}>Karts</button>
        <button onClick={() => navigate('/clientes')}>Clientes</button>
        <button onClick={() => navigate('/comprobantes')}>Comprobante</button>
      
        <button onClick={() => navigate('/reservar')}>Reservar</button>
        <button onClick={() => navigate('/tarifas')}>Tarifas</button>
        <button onClick={() => navigate('/calendario')}>Pista</button>
      
      </div>
      <h2>Reportes</h2>

      <form onSubmit={crearReporte}>
        <h3>Nuevo Reporte</h3>
        <select name="tipoReporte" value={nuevo.tipoReporte} onChange={handleChange}>
          <option value="">Selecciona tipo</option>
          <option value="PorVueltas">Por Vueltas</option>
          <option value="PorPersonas">Por Personas</option>
        </select>
        <input type="date" name="mesGenerado" value={nuevo.mesGenerado} onChange={handleChange} />
        <input type="number" name="ingresoTotal" placeholder="Ingreso Total" value={nuevo.ingresoTotal} onChange={handleChange} />

        {nuevo.tipoReporte === 'PorVueltas' && (
          <>
            <input type="number" name="numeroVueltas" placeholder="N° Vueltas" value={nuevo.numeroVueltas} onChange={handleChange} />
            <input type="number" name="tiempoMaximo" placeholder="Tiempo Máximo" value={nuevo.tiempoMaximo} onChange={handleChange} />
          </>
        )}

        {nuevo.tipoReporte === 'PorPersonas' && (
          <>
            <input type="number" name="minPersonas" placeholder="Min Personas" value={nuevo.minPersonas} onChange={handleChange} />
            <input type="number" name="maxPersonas" placeholder="Max Personas" value={nuevo.maxPersonas} onChange={handleChange} />
          </>
        )}

        <button type="submit">Crear</button>
      </form>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Tipo</th>
            <th>Mes</th>
            <th>Ingreso</th>
            <th>Extra</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
          {reportes.map((r) => (
            <tr key={r.id}>
              <td>{r.id}</td>
              <td>{r.tipoReporte}</td>
              <td>{r.mesGenerado}</td>
              <td>${r.ingresoTotal}</td>
              <td>
                {r.tipoReporte === 'PorVueltas' && (
                  <>
                    {r.numeroVueltas} vueltas, {r.tiempoMaximo} min
                  </>
                )}
                {r.tipoReporte === 'PorPersonas' && (
                  <>
                    {r.minPersonas}-{r.maxPersonas} personas
                  </>
                )}
              </td>
              <td>
                <button onClick={() => eliminarReporte(r.id)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Reportes;
