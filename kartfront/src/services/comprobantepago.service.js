import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get("/api/comprobantes");
};

const getById = (id) => {
    return httpClient.get(`/api/comprobantes/${id}`);
};

const generarPorReserva = (reservaId) => {
    return httpClient.post(`/api/comprobantes/generar/${reservaId}`);
};

const enviarPorEmail = (comprobanteId) => {
    return httpClient.post(`/api/comprobantes/enviar/${comprobanteId}`);
};

export default {
    getAll,
    getById,
    generarPorReserva,
    enviarPorEmail
};