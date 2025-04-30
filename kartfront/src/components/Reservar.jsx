import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import reservaService from '../services/reserva.service';

function Reservar() {
  const navigate = useNavigate(); 
  const [reservas, setReservas] = useState([]);
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
    reservaService.getAll().then((res) => setReservas(res.data));
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

  const crearReserva = (e) => {
    e.preventDefault();
    reservaService.create(nueva).then(() => {
      setNueva({
        cliente: { rut: '' },
        diaReserva: '',
        horaInicio: '',
        horaFin: '',
        tiempoReserva: '',
        acompanantes: [],
      });
      cargarReservas();
    });
  };

  const eliminarReserva = (id) => {
    if (window.confirm('¿Eliminar esta reserva?')) {
      reservaService.delete(id).then(cargarReservas);
    }
  };

  return (
    <div className="container">
      {/* Botones de navegación */}
      <div>
        <button onClick={() => navigate('/carros')}>Karts</button>
        <button onClick={() => navigate('/clientes')}>Clientes</button>
        <button onClick={() => navigate('/comprobantes')}>Comprobante</button>
        <button onClick={() => navigate('/reportes')}>Reportes</button>
       
        <button onClick={() => navigate('/tarifas')}>Tarifas</button>
        <button onClick={() => navigate('/calendario')}>Pista</button>
      </div>
      
      <h2>Reservas</h2>

      <form onSubmit={crearReserva}>
        <h3>Nueva Reserva</h3>
        <input type="text" name="rut" placeholder="RUT Cliente" value={nueva.cliente.rut} onChange={handleChange} />
        <input type="date" name="diaReserva" value={nueva.diaReserva} onChange={handleChange} />
        <input type="time" name="horaInicio" value={nueva.horaInicio} onChange={handleChange} />
        <input type="time" name="horaFin" value={nueva.horaFin} onChange={handleChange} />
        <input type="number" name="tiempoReserva" placeholder="Duración (min)" value={nueva.tiempoReserva} onChange={handleChange} />
        
        <div>
          <input type="text" placeholder="RUT Acompañante" value={acompananteInput} onChange={(e) => setAcompananteInput(e.target.value)} />
          <button type="button" onClick={agregarAcompanante}>Agregar Acompañante</button>
        </div>

        <div>
          Acompañantes: {nueva.acompanantes.join(', ')}
        </div>

        <button type="submit">Crear</button>
      </form>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Cliente</th>
            <th>Día</th>
            <th>Inicio</th>
            <th>Fin</th>
            <th>Duración</th>
            <th>Acompañantes</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
          {reservas.map((r) => (
            <tr key={r.idReserva}>
              <td>{r.idReserva}</td>
              <td>{r.cliente?.rut}</td>
              <td>{r.diaReserva}</td>
              <td>{r.horaInicio}</td>
              <td>{r.horaFin}</td>
              <td>{r.tiempoReserva} min</td>
              <td>{r.acompanantes?.join(', ')}</td>
              <td>
                <button onClick={() => eliminarReserva(r.idReserva)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Reservar;
