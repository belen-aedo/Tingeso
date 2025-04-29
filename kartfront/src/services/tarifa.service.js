import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get("/tarifas");
};

const getById = (id) => {
    return httpClient.get(`/tarifas/${id}`);
};

const getPorVueltas = (vueltas) => {
    return httpClient.get(`/tarifas/buscarPorVueltas/${vueltas}`);
};

const getPorVueltasYTiempo = (vueltas, tiempo) => {
    return httpClient.get(`/tarifas/buscarPorVueltasYTiempo?numeroVueltas=${vueltas}&tiempoMaximo=${tiempo}`);
};

const getPorRangoPrecio = (min, max) => {
    return httpClient.get(`/tarifas/rango-precio?precioMin=${min}&precioMax=${max}`);
};

const getOrdenadas = () => {
    return httpClient.get("/tarifas/ordenadas-precio");
};

const crear = (tarifa) => {
    return httpClient.post("/tarifas", tarifa);
};

const actualizar = (id, tarifa) => {
    return httpClient.put(`/tarifas/${id}`, tarifa);
};

const eliminar = (id) => {
    return httpClient.delete(`/tarifas/${id}`);
};

export default {
    getAll,
    getById,
    getPorVueltas,
    getPorVueltasYTiempo,
    getPorRangoPrecio,
    getOrdenadas,
    crear,
    actualizar,
    eliminar
};
