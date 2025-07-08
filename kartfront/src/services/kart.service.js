import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get("/kart/");
};

const getByCodigo = (codigo) => {
    return httpClient.get(`/kart/${codigo}`);
};

const save = (kart) => {
    return httpClient.post("/kart/", kart);
};

// ✅ Cambiar estado de un kart
const updateEstado = (codigo, nuevoEstado) =>
  httpClient.put(`/kart/${codigo}/estado`, { estado: nuevoEstado });

// ✅ Eliminar un kart - CORREGIDO
const deleteKart = (codigo) => httpClient.delete(`/kart/${codigo}`);

export default {
  getAll,
  getByCodigo,
  save,
  updateEstado,
  delete: deleteKart  // Aquí está el problema principal
};