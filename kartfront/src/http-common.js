import axios from "axios"; 

const kartingServer = "localhost:8090";

export default axios.create({
    baseURL: `http://${kartingServer}`,
    headers: {
        'Content-Type': 'application/json'
    } 
});
