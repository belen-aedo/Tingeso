import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get("/reportes/");
};

const getById = (id) => {
    return httpClient.get(`/reportes/${id}`);
};

const getPorTipo = (tipo) => {
    return httpClient.get(`/reportes/tipo/${tipo}`);
};

const getPorMes = (fecha) => {
    return httpClient.get(`/reportes/mes?fecha=${fecha}`);
};

const crear = (reporte) => {
    return httpClient.post("/reportes/", reporte);
};

const eliminar = (id) => {
    return httpClient.delete(`/reportes/${id}`);
};

export default {
    getAll,
    getById,
    getPorTipo,
    getPorMes,
    crear,
    eliminar
};
