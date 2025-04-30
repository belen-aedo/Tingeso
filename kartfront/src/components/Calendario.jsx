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

  return (
    <div className="container">
      {/* Botones de navegación hacia otras páginas del sistema */}
      <div>
        <button onClick={() => navigate('/carros')}>Karts</button>
        <button onClick={() => navigate('/comprobantes')}>Comprobante</button>
        <button onClick={() => navigate('/reportes')}>Reportes</button>
        <button onClick={() => navigate('/reservar')}>Reservar</button>
        <button onClick={() => navigate('/tarifas')}>Tarifas</button>
        <button onClick={() => navigate('/calendario')}>Pista</button>
      </div>

      {/* Contenedor del calendario con altura fija */}
      <div style={{ height: "600px" }}>
        <Calendar
          localizer={localizer} // Configuración de localización
          events={events} // Eventos a mostrar
          startAccessor="start" // Clave para la fecha de inicio
          endAccessor="end" // Clave para la fecha de fin
          defaultView="week" // Vista predeterminada: semana
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
    </div>
  );
}
