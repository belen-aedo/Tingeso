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

export default {
    getAll,
    getByCodigo,
    save
};
