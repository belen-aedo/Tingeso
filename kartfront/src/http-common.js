import axios from "axios";

//const kartingServer = "karting-belen.westus2.cloudapp.azure.com";
const kartingServer = "localhost:80";

export default axios.create({
    baseURL: `http://${kartingServer}`,
    headers: {
        'Content-Type': 'application/json'
    } 
});