import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get("/reservas");
};

const getById = (id) => {
    return httpClient.get(`/reservas/${id}`);
};

const getByDia = (fecha) => {
    return httpClient.get(`/reservas/dia/${fecha}`);
};

const getByCliente = (rut) => {
    return httpClient.get(`/reservas/cliente/${rut}`);
};

const create = (reserva) => {
    return httpClient.post("/reservas", reserva);
};

const update = (id, reserva) => {
    return httpClient.put(`/reservas/${id}`, reserva);
};

const remove = (id) => {
    return httpClient.delete(`/reservas/${id}`);
};

export default {
    getAll,
    getById,
    getByDia,
    getByCliente,
    create,
    update,
    remove
};
