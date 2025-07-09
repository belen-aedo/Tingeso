// Importa hooks de React para manejar el estado y efectos secundarios
import { useEffect, useState } from "react";

// Importa componentes y funciones necesarias de react-big-calendar
import { Calendar, dateFnsLocalizer } from "react-big-calendar";
import "react-big-calendar/lib/css/react-big-calendar.css";

// Importa funciones de date-fns para trabajar con fechas
import { format, parse, startOfWeek, getDay } from "date-fns";
import es from "date-fns/locale/es"; // Localización en español

// Importa el servicio que obtiene las reservas
import reservaService from "../services/reserva.service";

// Importa el hook para navegar entre rutas
import { useNavigate } from "react-router-dom";

// Define el objeto de localizaciones
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
  const [events, setEvents] = useState([]);
  const [mostrarMenu, setMostrarMenu] = useState(false);
  const [currentDate, setCurrentDate] = useState(new Date());
  const [currentView, setCurrentView] = useState('week');
  
  // NUEVOS ESTADOS PARA MEJORAR USABILIDAD
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [retryCount, setRetryCount] = useState(0);
  
  const navigate = useNavigate();



  // FUNCIÓN MEJORADA PARA CARGAR RESERVAS CON MANEJO DE ERRORES
  const cargarReservas = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const res = await reservaService.getAll();
      
      if (res.data) {
        const eventos = res.data
          .map((reserva) => {
            // Validación de datos
            if (!reserva.diaReserva || !reserva.horaInicio || !reserva.horaFin) {
              console.warn('Reserva con datos incompletos:', reserva);
              return null;
            }

            try {
              const start = new Date(`${reserva.diaReserva}T${reserva.horaInicio}`);
              const end = new Date(`${reserva.diaReserva}T${reserva.horaFin}`);
              
              // Validar que las fechas sean válidas
              if (isNaN(start.getTime()) || isNaN(end.getTime())) {
                console.warn('Fechas inválidas en reserva:', reserva);
                return null;
              }

              return {
                title: `Cliente: ${reserva.cliente?.nombre || 'Sin nombre'}`,
                start,
                end,
                resource: reserva, // Guardamos la reserva completa para referencia
              };
            } catch (dateError) {
              console.warn('Error procesando fechas de reserva:', reserva, dateError);
              return null;
            }
          })
          .filter(Boolean); // Filtrar elementos nulos

        setEvents(eventos);
        setRetryCount(0); // Reset retry count en caso de éxito
      }
    } catch (error) {
      console.error('Error cargando reservas:', error);
      setError('Error al cargar las reservas. Por favor, intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  // Hook que se ejecuta al montar el componente
  useEffect(() => {
    cargarReservas();
  }, []);

  // FUNCIÓN PARA REINTENTAR CARGA
  const handleRetry = () => {
    setRetryCount(prev => prev + 1);
    cargarReservas();
  };

  // FUNCIÓN PARA MANEJAR NAVEGACIÓN DEL CALENDARIO
  const handleNavigate = (newDate) => {
    setCurrentDate(newDate);
  };

  // FUNCIÓN PARA MANEJAR CAMBIO DE VISTA
  const handleViewChange = (view) => {
    setCurrentView(view);
  };

  // FUNCIÓN PARA CERRAR MENÚ AL HACER CLIC FUERA
  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) {
      setMostrarMenu(false);
    }
  };

  // COMPONENTE DE LOADING
  const LoadingSpinner = () => (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      height: '400px',
      flexDirection: 'column',
      gap: '20px'
    }}>
      <div style={{
        border: '4px solid #f3f3f3',
        borderTop: '4px solid #c62828',
        borderRadius: '50%',
        width: '40px',
        height: '40px',
        animation: 'spin 1s linear infinite'
      }}></div>
      <p style={{ color: '#666', fontSize: '16px' }}>Cargando reservas...</p>
      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}
      </style>
    </div>
  );

  // COMPONENTE DE ERROR
  const ErrorMessage = () => (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      height: '400px',
      flexDirection: 'column',
      gap: '20px',
      padding: '20px',
      backgroundColor: '#ffebee',
      borderRadius: '8px',
      border: '1px solid #ffcdd2'
    }}>
      <div style={{ fontSize: '48px', color: '#c62828' }}>⚠️</div>
      <p style={{ color: '#c62828', fontSize: '18px', textAlign: 'center' }}>
        {error}
      </p>
      <button
        onClick={handleRetry}
        style={{
          backgroundColor: '#c62828',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          padding: '12px 24px',
          fontSize: '16px',
          cursor: 'pointer',
          transition: 'background-color 0.2s'
        }}
        onMouseOver={(e) => e.target.style.backgroundColor = '#b71c1c'}
        onMouseOut={(e) => e.target.style.backgroundColor = '#c62828'}
      >
        Reintentar{retryCount > 0 ? ` (${retryCount})` : ''}
      </button>
    </div>
  );

  // COMPONENTE DE BREADCRUMBS
  const Breadcrumbs = () => (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      gap: '10px',
      marginBottom: '20px',
      fontSize: '14px',
      color: '#666'
    }}>
      <span>📍</span>
      <span>Calendario</span>
      <span>›</span>
      <span style={{ color: '#c62828', fontWeight: 'bold' }}>
        Vista {currentView === 'week' ? 'Semanal' : 'Diaria'}
      </span>
      <span>›</span>
      <span style={{ color: '#c62828' }}>
        {format(currentDate, 'MMMM yyyy', { locale: es })}
      </span>
    </div>
  );



  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      {/* Header mejorado con indicador de estado */}
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
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
      }}>
        <div style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
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
                marginRight: '20px',
                transition: 'all 0.2s'
              }}
              onMouseOver={(e) => {
                e.target.style.backgroundColor = '#f5f5f5';
                e.target.style.transform = 'scale(1.05)';
              }}
              onMouseOut={(e) => {
                e.target.style.backgroundColor = 'white';
                e.target.style.transform = 'scale(1)';
              }}
            >
              {mostrarMenu ? '✕ Cerrar' : '☰ Menú'}
            </button>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div style={{ 
                fontSize: '24px',
                background: 'linear-gradient(45deg, #ffffff, #f0f0f0)',
                borderRadius: '50%',
                padding: '5px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
              }}>
                🏁
              </div>
              <h2 style={{ 
                margin: 0, 
                fontSize: '24px',
                fontWeight: '700',
                textShadow: '1px 1px 2px rgba(0,0,0,0.3)',
                letterSpacing: '0.5px'
              }}>
                Sistema Arriendo Karting - Calendario
              </h2>
              <div style={{
                backgroundColor: 'rgba(255,255,255,0.2)',
                padding: '4px 8px',
                borderRadius: '12px',
                fontSize: '12px',
                fontWeight: 'bold',
                marginLeft: '10px'
              }}>
                Gestión de Reservas
              </div>
            </div>
          </div>
          
      
        </div>
      </header>

      {/* Overlay para cerrar menú */}
      {mostrarMenu && (
        <div
          onClick={handleOverlayClick}
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            backgroundColor: 'rgba(0,0,0,0.5)',
            zIndex: 998,
          }}
        />
      )}

      {/* Menú lateral */}
      {mostrarMenu && (
        <>
          {/* Overlay para cerrar menú */}
          <div 
            style={{
              position: 'fixed',
              top: '70px',
              left: '200px',
              width: 'calc(100vw - 200px)',
              height: 'calc(100vh - 70px)',
              backgroundColor: 'rgba(0,0,0,0.3)',
              zIndex: 998
            }}
            onClick={() => setMostrarMenu(false)}
          />
          
          <aside style={{
            position: 'fixed',
            top: '70px',
            left: 0,
            width: '200px',
            height: 'calc(100vh - 70px)',
            backgroundColor: '#f5f5f5',
            padding: '20px',
            boxShadow: '2px 0 10px rgba(0,0,0,0.1)',
            zIndex: 999,
            overflowY: 'auto',
            borderRight: '3px solid #c62828'
          }}>
            <nav role="navigation" aria-label="Menú principal">
              <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                <li style={{ marginBottom: '10px' }}>
                  <button 
                    onClick={() => navigate('/carros')}
                    style={{
                      width: '100%',
                      padding: '12px',
                      backgroundColor: '#c62828',
                      color: 'white',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      fontWeight: 'bold'
                    }}
                    aria-current="page"
                  >
                    Karts
                  </button>
                </li>
                <li style={{ marginBottom: '10px' }}>
                  <button 
                    onClick={() => navigate('/clientes')}
                    style={{
                      width: '100%',
                      padding: '12px',
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
                      padding: '12px',
                      backgroundColor: '#4ca90d',
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
                      padding: '12px',
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
                      padding: '12px',
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
                      padding: '12px',
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
                      padding: '12px',
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
            </nav>
          </aside>
        </>
      )}


      {/* Contenido principal */}
      <main style={{
        marginTop: '90px',
        marginLeft: mostrarMenu ? '270px' : '20px',
        marginRight: '20px',
        transition: 'margin-left 0.3s ease'
      }}>
        <Breadcrumbs />
        
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          marginBottom: '20px'
        }}>
          <h3 style={{ margin: 0, color: '#c62828' }}>📅 Calendario de Reservas</h3>
          
          {/* Controles adicionales */}
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            <button
              onClick={() => setCurrentDate(new Date())}
              style={{
                backgroundColor: '#4CAF50',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                padding: '8px 16px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              📍 Hoy
            </button>
            
            <select
              value={currentView}
              onChange={(e) => setCurrentView(e.target.value)}
              style={{
                padding: '8px 12px',
                borderRadius: '4px',
                border: '1px solid #ddd',
                backgroundColor: 'white',
                cursor: 'pointer'
              }}
            >
              <option value="week">👁️ Vista Semanal</option>
              <option value="day">🔍 Vista Diaria</option>
            </select>
          </div>
        </div>

        {/* Información del estado actual */}
        {!loading && !error && (
          <div style={{
            backgroundColor: '#e8f5e8',
            padding: '10px 15px',
            borderRadius: '6px',
            marginBottom: '20px',
            border: '1px solid #c8e6c9'
          }}>
            <span style={{ color: '#2e7d32', fontSize: '14px' }}>
              ✅ {events.length} reservas cargadas • Vista: {currentView === 'week' ? 'Semanal' : 'Diaria'}
            </span>
          </div>
        )}
        
        {/* Contenedor del calendario */}
        <div style={{ height: "600px", position: 'relative' }}>
          {loading ? (
            <LoadingSpinner />
          ) : error ? (
            <ErrorMessage />
          ) : (
            <Calendar
              localizer={localizer}
              events={events}
              startAccessor="start"
              endAccessor="end"
              date={currentDate}
              view={currentView}
              onNavigate={handleNavigate}
              onView={handleViewChange}
              views={["week", "day"]}
              step={30}
              timeslots={2}
              style={{ height: "100%" }}
              messages={{
                allDay: "Todo el día",
                previous: "← Anterior",
                next: "Siguiente →",
                today: "📍 Hoy",
                month: "Mes",
                week: "Semana",
                day: "Día",
                agenda: "Agenda",
                date: "Fecha",
                time: "Hora",
                event: "Evento",
                noEventsInRange: "No hay eventos en este rango de fechas",
                showMore: (total) => `+ Ver ${total} más eventos`,
              }}
              // Mejorar el formato de eventos
              eventPropGetter={(event) => ({
                style: {
                  backgroundColor: '#c62828',
                  borderRadius: '4px',
                  opacity: 0.8,
                  color: 'white',
                  border: '1px solid #b71c1c',
                  fontSize: '12px'
                }
              })}
            />
          )}
        </div>
      </main>
    </div>
  );
}