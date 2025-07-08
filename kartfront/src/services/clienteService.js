import httpClient from "../http-common";

const getAllClientes = () => {
    return httpClient.get("/clientes/");
};

const getClienteByRut = (rut) => {
    return httpClient.get(`/clientes/${rut}`);
};

const getClienteByEmail = (email) => {
    return httpClient.get(`/clientes/email/${email}`);
};

const getByVisitasRango = (min, max) => {
    return httpClient.get(`/clientes/visitas?min=${min}&max=${max}`);
};

const saveCliente = (cliente) => {
    return httpClient.post("/clientes/", cliente);
};

const incrementarVisita = (rut) => {
    return httpClient.put(`/clientes/incrementar-visita/${rut}`);
};

const resetearVisitas = () => {
    return httpClient.put("/clientes/resetear-visitas")
      
};

const deleteCliente = (rut) => {
  return httpClient.delete(`/clientes/${rut}`);
};



export default {
    getAllClientes,
    getClienteByRut,
    getClienteByEmail,
    getByVisitasRango,
    saveCliente,
    incrementarVisita,
    resetearVisitas,
    deleteCliente
};
