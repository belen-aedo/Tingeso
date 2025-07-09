import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Carros from './components/carros.jsx';
import Clientes from './components/clientes.jsx';
import Comprobante from './components/comprobante.jsx';

import Reportes from './components/reportes.jsx';
import Reservar from './components/reservar.jsx';
import Tarifas from './components/tarifas.jsx';
import Pista from './components/calendario.jsx';
import './App.css';

function App() {
  

  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/carros" element={<Carros />} />
          <Route path="/clientes" element={<Clientes />} />
          <Route path="/comprobantes" element={<Comprobante />} />
   
          <Route path="/reportes" element={<Reportes />} />
          <Route path="/reservar" element={<Reservar />} />
          <Route path="/tarifas" element={<Tarifas />} />
          <Route path="/" element={<Navigate to="/carros" />} />
          <Route path="*" element={<Navigate to="/carros" />} />
          <Route path="/calendario" element={<Pista />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
