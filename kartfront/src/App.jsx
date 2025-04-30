import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Carros from './components/Carros.jsx';
import Clientes from './components/Clientes.jsx';
import Comprobante from './components/Comprobante.jsx';

import Reportes from './components/Reportes.jsx';
import Reservar from './components/Reservar.jsx';
import Tarifas from './components/Tarifas.jsx';
import Pista from './components/Calendario.jsx';
import './App.css';

function App() {
  const isLoggedIn = true;

  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/carros" element={<Carros />} />
          <Route path="/clientes" element={<Clientes />} />
          <Route path="/comprobante" element={<Comprobante />} />
   
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
