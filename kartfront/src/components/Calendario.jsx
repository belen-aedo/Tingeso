// Importa hooks de React para manejar el estado y efectos secundarios
import { useEffect, useState } from "react";

// Importa componentes y funciones necesarias de react-big-calendar
import { Calendar, dateFnsLocalizer } from "react-big-calendar";
import "react-big-calendar/lib/css/react-big-calendar.css";

// Importa funciones de date-fns para trabajar con fechas
import { format, parse, startOfWeek, getDay } from "date-fns";
import es from "date-fns/locale/es"; // Localización en español

// Importa el servicio que obtiene las reservas
import reservaService from "../services/reserva.service"; // Asegúrate de que el path sea correcto

// Importa el hook para navegar entre rutas
import { useNavigate } from "react-router-dom";

// Define el objeto de localizaciones, en este caso sólo se usa "es"
const locales = {
  "es": es,
};

// Configura el localizador de fechas con la localización en español
const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek,
  getDay,
  locales,
  locale: "es",
});

// Componente principal del calendario
export default function CalendarComponent() {
  const [events, setEvents] = useState([]); // Estado para almacenar los eventos
  const [mostrarMenu, setMostrarMenu] = useState(false); // Estado para mostrar/ocultar menú
  
  // ESTADOS AGREGADOS PARA CONTROLAR EL CALENDARIO
  const [currentDate, setCurrentDate] = useState(new Date()); // Fecha actual del calendario
  const [currentView, setCurrentView] = useState('week'); // Vista actual del calendario
  
  const navigate = useNavigate(); // Hook para redireccionar a otras rutas

  // Hook que se ejecuta al montar el componente
  useEffect(() => {
    // Llama al servicio de reservas y transforma los datos en eventos para el calendario
    reservaService.getAll().then((res) => {
      const eventos = res.data.map((reserva) => {
        const start = new Date(`${reserva.diaReserva}T${reserva.horaInicio}`);
        const end = new Date(`${reserva.diaReserva}T${reserva.horaFin}`);

        return {
          title: `Cliente: ${reserva.cliente?.nombre || 'Sin nombre'}`, // Título del evento
          start,
          end,
        };
      });
      setEvents(eventos); // Actualiza el estado con los eventos
    });
  }, []); // Dependencias vacías → se ejecuta una vez al montar

  // FUNCIÓN PARA MANEJAR NAVEGACIÓN DEL CALENDARIO
  const handleNavigate = (newDate) => {
    setCurrentDate(newDate);
  };

  // FUNCIÓN PARA MANEJAR CAMBIO DE VISTA
  const handleViewChange = (view) => {
    setCurrentView(view);
  };

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      {/* Header fijo rojo */}
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

      {/* Menú lateral */}
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
                  backgroundColor: '#c62828',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
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
                  backgroundColor: '#4CAF50',
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
        </aside>
      )}

      {/* Contenido principal */}
      <main style={{
        marginTop: '130px',
        marginLeft: mostrarMenu ? '220px' : '20px',
        marginRight: '20px',
        transition: 'margin-left 0.3s ease'
      }}>
        <h3>Calendario de Reservas</h3>
        
        {/* Contenedor del calendario con altura fija */}
        <div style={{ height: "600px" }}>
          <Calendar
            localizer={localizer} // Configuración de localización
            events={events} // Eventos a mostrar
            startAccessor="start" // Clave para la fecha de inicio
            endAccessor="end" // Clave para la fecha de fin
            
            // PROPIEDADES AGREGADAS PARA CONTROLAR EL CALENDARIO
            date={currentDate} // Fecha actual controlada
            view={currentView} // Vista actual controlada
            onNavigate={handleNavigate} // Función para manejar navegación
            onView={handleViewChange} // Función para cambiar vista
            
            views={["week", "day"]} // Vistas disponibles: semana y día
            step={30} // Intervalo entre bloques de tiempo (30 min)
            timeslots={2} // Número de espacios por bloque
            style={{ height: "100%" }} // Ocupa todo el contenedor
            messages={{
              // Traducción de etiquetas del calendario
              allDay: "Todo el día",
              previous: "Anterior",
              next: "Siguiente",
              today: "Hoy",
              month: "Mes",
              week: "Semana",
              day: "Día",
              agenda: "Agenda",
              date: "Fecha",
              time: "Hora",
              event: "Evento",
              noEventsInRange: "No hay eventos en este rango",
              showMore: (total) => `+ Ver ${total} más`,
            }}
          />
        </div>
      </main>
    </div>
  );
}