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
  const navigate = useNavigate();

  useEffect(() => {
    cargarClientes();
  }, []);

  const cargarClientes = () => {
    clienteService.getAllClientes().then((res) => {
      setClientes(res.data);
    });
  };

  const handleChange = (e) => {
    setNuevoCliente({ ...nuevoCliente, [e.target.name]: e.target.value });
  };

  const crearCliente = (e) => {
    e.preventDefault();
    clienteService.saveCliente(nuevoCliente).then(() => {
      setNuevoCliente({ rut: '', nombre: '', email: '', fechaCumple: '' });
      cargarClientes();
    });
  };

  const eliminarCliente = (rut) => {
    if (window.confirm('¿Seguro que deseas eliminar este cliente?')) {
      clienteService.deleteCliente(rut).then(cargarClientes);
    }
  };

  const buscarCliente = () => {
    if (!buscarRut) return cargarClientes();
    clienteService.getClienteByRut(buscarRut).then((res) => {
      setClientes([res.data]);
    }).catch(() => {
      alert('Cliente no encontrado');
      cargarClientes();
    });
  };
  
  return (
    <div className="container">
      {/* Botones de navegación */}
      <div>
        <button onClick={() => navigate('/carros')}>Karts</button>
        
        <button onClick={() => navigate('/comprobantes')}>Comprobante</button>
        <button onClick={() => navigate('/reportes')}>Reportes</button>
        <button onClick={() => navigate('/reservar')}>Reservar</button>
        <button onClick={() => navigate('/tarifas')}>Tarifas</button>
        <button onClick={() => navigate('/calendario')}>Pista</button>
      </div>

      <h2>Gestión de clientes</h2>

      <div>
        <input
          type="text"
          placeholder="Buscar por RUT"
          value={buscarRut}
          onChange={(e) => setBuscarRut(e.target.value)}
        />
        <button onClick={buscarCliente}>Buscar</button>
      </div>

      <form onSubmit={crearCliente}>
        <h3>Agregar Cliente</h3>
        <input
          type="text"
          name="rut"
          placeholder="RUT"
          value={nuevoCliente.rut}
          onChange={handleChange}
          required
        />
        <input
          type="text"
          name="nombre"
          placeholder="Nombre"
          value={nuevoCliente.nombre}
          onChange={handleChange}
          required
        />
        <input
          type="email"
          name="email"
          placeholder="Correo"
          value={nuevoCliente.email}
          onChange={handleChange}
          required
        />
        <input
          type="date"
          name="fechaCumple"
          value={nuevoCliente.fechaCumple}
          onChange={handleChange}
        />
        <button type="submit">Crear</button>
      </form>

      <table>
        <thead>
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
          {clientes.map((cli) => (
            <tr key={cli.rut}>
              <td>{cli.rut}</td>
              <td>{cli.nombre}</td>
              <td>{cli.email}</td>
              <td>{cli.visitasMes}</td>
              <td>{cli.fechaCumple}</td>
              <td>{cli.descuentoAplicable}%</td>
              <td>
                <button onClick={() => eliminarCliente(cli.rut)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Clientes;