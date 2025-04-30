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
  const navigate = useNavigate();

  useEffect(() => {
    cargarKarts();
  }, []);

  const cargarKarts = () => {
    kartService.getAll().then((res) => setKarts(res.data));
  };

  const handleChange = (e) => {
    setNuevoKart({ ...nuevoKart, [e.target.name]: e.target.value });
  };

  const crearKart = (e) => {
    e.preventDefault();
    kartService.save(nuevoKart).then(() => {
      setNuevoKart({ codigo: '', modelo: '', estado: 'Disponible' });
      cargarKarts();
    });
  };

  return (
    <div>
      {/* Barra de navegación */}
      <div className="bg-gray-800 p-4 mb-6">
        <div className="flex flex-wrap gap-2 justify-center">
          
          
          <button
            onClick={() => navigate('/clientes')}
            className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
          >
            Clientes
          </button>
          <button
            onClick={() => navigate('/comprobante')}
            className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
          >
            Comprobantes
          </button>
          <button
            onClick={() => navigate('/reportes')}
            className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
          >
            Reportes
          </button>
          <button
            onClick={() => navigate('/reservar')}
            className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
          >
            Reservar
          </button>
          <button
            onClick={() => navigate('/tarifas')}
            className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
          >
            Tarifas
            </button>
            <button
            onClick={() => navigate('/Calendario')}
            className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
          >
            Pista
          </button>
          
        </div>
      </div>

      {/* Contenido original */}
      <div className="container mx-auto px-4">
        <h2 className="text-2xl font-bold mb-4">Gestión de Karts</h2>

        <form onSubmit={crearKart} className="mb-8 p-4 border rounded-lg bg-gray-50">
          <h3 className="text-xl font-semibold mb-3">Agregar Kart</h3>
          <div className="flex flex-wrap gap-4 mb-4">
            <input
              type="text"
              name="codigo"
              placeholder="Código"
              value={nuevoKart.codigo}
              onChange={handleChange}
              required
              className="px-3 py-2 border rounded"
            />
            <input
              type="text"
              name="modelo"
              placeholder="Modelo"
              value={nuevoKart.modelo}
              onChange={handleChange}
              required
              className="px-3 py-2 border rounded"
            />
            <select 
              name="estado" 
              value={nuevoKart.estado} 
              onChange={handleChange}
              className="px-3 py-2 border rounded"
            >
              <option value="Disponible">Disponible</option>
              <option value="En uso">En uso</option>
              <option value="En mantenimiento">En mantenimiento</option>
            </select>
            <button 
              type="submit"
              className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
            >
              Crear
            </button>
          </div>
        </form>

        <div className="overflow-x-auto">
          <table className="min-w-full bg-white border">
            <thead className="bg-gray-100">
              <tr>
                <th className="py-2 px-4 border">Código</th>
                <th className="py-2 px-4 border">Modelo</th>
                <th className="py-2 px-4 border">Estado</th>
              </tr>
            </thead>
            <tbody>
              {karts.map((k) => (
                <tr key={k.codigo}>
                  <td className="py-2 px-4 border">{k.codigo}</td>
                  <td className="py-2 px-4 border">{k.modelo}</td>
                  <td className="py-2 px-4 border">{k.estado}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default Carros;