import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import comprobanteService from '../services/comprobantepago.service';

function Pista() {
  const navigate = useNavigate(); 
  const [comprobantes, setComprobantes] = useState([]);
  const [reservaId, setReservaId] = useState('');

  useEffect(() => {
    cargarComprobantes();
  }, []);

  const cargarComprobantes = () => {
    comprobanteService.getAll().then((res) => setComprobantes(res.data));
  };

  const generarComprobante = () => {
    if (!reservaId) return alert('Ingresa el ID de una reserva');
    comprobanteService.generarPorReserva(reservaId)
      .then(() => {
        alert('Comprobante generado correctamente');
        setReservaId('');
        cargarComprobantes();
      })
      .catch(() => alert('Error al generar comprobante'));
  };

  const enviarEmail = (id) => {
    if (window.confirm('¿Enviar comprobante por correo?')) {
      comprobanteService.enviarPorEmail(id)
        .then(() => alert('Correo enviado'))
        .catch(() => alert('Error al enviar correo'));
    }
  };

  return (
    <div className="container">
      {/* Botones de navegación */}
      <div>
        <button onClick={() => navigate('/carros')}>Karts</button>
        <button onClick={() => navigate('/clientes')}>Clientes</button>
        <button onClick={() => navigate('/reportes')}>Reportes</button>
        <button onClick={() => navigate('/reservar')}>Reservar</button>
        <button onClick={() => navigate('/tarifas')}>Tarifas</button>
      </div>

      <h2>Comprobantes de Pago</h2>

      <div>
        <input
          type="number"
          placeholder="ID Reserva"
          value={reservaId}
          onChange={(e) => setReservaId(e.target.value)}
        />
        <button onClick={generarComprobante}>Generar Comprobante</button>
      </div>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Reserva</th>
            <th>Cliente</th>
            <th>Total</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
          {comprobantes.map((c) => (
            <tr key={c.id}>
              <td>{c.id}</td>
              <td>{c.reserva?.idReserva}</td>
              <td>{c.cliente?.nombre}</td>
              <td>${c.montoTotalConIva}</td>
              <td>
                <button onClick={() => enviarEmail(c.id)}>Enviar PDF</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Pista;
