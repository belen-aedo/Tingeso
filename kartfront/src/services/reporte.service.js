import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get("/reportes/individual");
};

const getById = (id) => {
    return httpClient.get(`/reportes/individual/${id}`);
};

const getPorTipo = (tipo) => {
    return httpClient.get(`/reportes/individual/tipo/${tipo}`);
};

const getPorMes = (fecha) => {
    return httpClient.get(`/reportes/individual/fecha?fecha=${fecha}`);
};

const crear = (reporte) => {
    // Transformar los datos para que coincidan con el backend
    const reporteTransformado = {
        tipoReporte: reporte.tipoReporte,
        fecha: reporte.mesGenerado, // Cambiar mesGenerado por fecha
        ingresoTotal: parseFloat(reporte.ingresoTotal),
        numeroVueltas: reporte.numeroVueltas ? parseInt(reporte.numeroVueltas) : null,
        tiempoMaximo: reporte.tiempoMaximo ? parseInt(reporte.tiempoMaximo) : null,
        minPersonas: reporte.minPersonas ? parseInt(reporte.minPersonas) : null,
        maxPersonas: reporte.maxPersonas ? parseInt(reporte.maxPersonas) : null,
        descripcion: `Reporte ${reporte.tipoReporte} del ${reporte.mesGenerado}` // Agregar descripción
    };
    
    return httpClient.post("/reportes/individual", reporteTransformado);
};

const eliminar = (id) => {
    return httpClient.delete(`/reportes/individual/${id}`);
};

export default {
    getAll,
    getById,
    getPorTipo,
    getPorMes,
    crear,
    eliminar
};