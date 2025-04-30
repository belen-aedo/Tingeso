import axios from "axios";

const kartingServer = "172.203.153.126";
//const kartingServer = "localhost:80";

export default axios.create({
    baseURL: `http://${kartingServer}`,
    headers: {
        'Content-Type': 'application/json'
    } 
});