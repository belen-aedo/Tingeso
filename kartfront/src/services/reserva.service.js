import httpClient from "../http-common";

const getAll = () => httpClient.get("/reservas");
const getById = (id) => httpClient.get(`/reservas/${id}`);
const getByDia = (fecha) => httpClient.get(`/reservas/dia/${fecha}`);
const getByCliente = (rut) => httpClient.get(`/reservas/cliente/${rut}`);
const create = (reserva) => httpClient.post("/reservas", reserva);
const update = (id, reserva) => httpClient.put(`/reservas/${id}`, reserva);
const remove = (id) => httpClient.delete(`/reservas/${id}`);
const getAllDTO = () => httpClient.get("/reservas/dto");

export default {
    getAll,
    getById,
    getByDia,
    getByCliente,
    create,
    update,
    remove,       // ✅ Importante
    getAllDTO
};
