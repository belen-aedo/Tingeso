import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import tarifaService from '../services/tarifa.service';

function Tarifas() {
  const navigate = useNavigate();
  const [tarifas, setTarifas] = useState([]);
  const [nueva, setNueva] = useState({
    numeroVueltas: '',
    tiempoMaximo: '',
    precioBase: '',
    duracionReserva: ''
  });

  const [editando, setEditando] = useState(null); // ID de tarifa en edición

  useEffect(() => {
    cargarTarifas();
  }, []);

  const cargarTarifas = () => {
    tarifaService.getAll().then((res) => setTarifas(res.data));
  };

  const handleChange = (e) => {
    setNueva({ ...nueva, [e.target.name]: e.target.value });
  };

  const crearTarifa = (e) => {
    e.preventDefault();
    if (editando) {
      // Si estamos editando, actualizamos
      tarifaService.actualizar(editando, nueva).then(() => {
        setNueva({
          numeroVueltas: '',
          tiempoMaximo: '',
          precioBase: '',
          duracionReserva: ''
        });
        setEditando(null);
        cargarTarifas();
      });
    } else {
      // Si no estamos editando, creamos nueva
      tarifaService.crear(nueva).then(() => {
        setNueva({
          numeroVueltas: '',
          tiempoMaximo: '',
          precioBase: '',
          duracionReserva: ''
        });
        cargarTarifas();
      });
    }
  };

  const eliminarTarifa = (id) => {
    if (window.confirm('¿Eliminar esta tarifa?')) {
      tarifaService.eliminar(id).then(cargarTarifas);
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

  return (
    <div className="container">
      {/* Botones de navegación */}
      <div>
        <button onClick={() => navigate('/carros')}>Karts</button>
        <button onClick={() => navigate('/clientes')}>Clientes</button>
        <button onClick={() => navigate('/comprobantes')}>Comprobante</button>
        <button onClick={() => navigate('/reportes')}>Reportes</button>
        <button onClick={() => navigate('/reservar')}>Reservar</button>
      </div>

      <h2>Gestión de Tarifas</h2>

      <form onSubmit={crearTarifa}>
        <h3>{editando ? 'Editar Tarifa' : 'Agregar Tarifa'}</h3>
        <input type="number" name="numeroVueltas" placeholder="Vueltas" value={nueva.numeroVueltas} onChange={handleChange} />
        <input type="number" name="tiempoMaximo" placeholder="Tiempo Máximo" value={nueva.tiempoMaximo} onChange={handleChange} />
        <input type="number" name="precioBase" placeholder="Precio Base" value={nueva.precioBase} onChange={handleChange} />
        <input type="number" name="duracionReserva" placeholder="Duración (min)" value={nueva.duracionReserva} onChange={handleChange} />
        <button type="submit">{editando ? 'Actualizar' : 'Crear'}</button>
      </form>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Vueltas</th>
            <th>Tiempo Máx</th>
            <th>Precio</th>
            <th>Duración</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
          {tarifas.map((t) => (
            <tr key={t.id}>
              <td>{t.id}</td>
              <td>{t.numeroVueltas}</td>
              <td>{t.tiempoMaximo} min</td>
              <td>${t.precioBase}</td>
              <td>{t.duracionReserva} min</td>
              <td>
                <button onClick={() => cargarParaEditar(t)}>Editar</button>
                <button onClick={() => eliminarTarifa(t.id)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Tarifas;
