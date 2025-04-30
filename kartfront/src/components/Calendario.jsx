import { useEffect, useState } from "react";
import { Calendar, dateFnsLocalizer } from "react-big-calendar";
import "react-big-calendar/lib/css/react-big-calendar.css";
import { format, parse, startOfWeek, getDay } from "date-fns";
import es from "date-fns/locale/es"; // Español
import reservaService from "../services/reserva.service"; // Ajusta el path según corresponda
import { useNavigate } from "react-router-dom"; // Importa useNavigate

const locales = {
  "es": es,
};

const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek,
  getDay,
  locales,
  locale: "es",
});

export default function CalendarComponent() {
  const [events, setEvents] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    reservaService.getAll().then((res) => {
      const eventos = res.data.map((reserva) => {
        const start = new Date(`${reserva.diaReserva}T${reserva.horaInicio}`);
        const end = new Date(`${reserva.diaReserva}T${reserva.horaFin}`);

        return {
          title: `Cliente: ${reserva.cliente?.nombre || 'Sin nombre'}`,
          start,
          end,
        };
      });
      setEvents(eventos);
    });
  }, []);

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

      <div style={{ height: "600px" }}>
        <Calendar
          localizer={localizer}
          events={events}
          startAccessor="start"
          endAccessor="end"
          defaultView="week"
          views={["week", "day"]}
          step={30}
          timeslots={2}
          style={{ height: "100%" }}
          messages={{
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
