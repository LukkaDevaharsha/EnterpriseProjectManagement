import axios from "axios";

const api = axios.create({
    baseURL: (
        import.meta.env.VITE_API_BASE_URL ||
        (import.meta.env.DEV
            ? "http://localhost:8080/api"
            : "https://enterpriseprojectmanagement.onrender.com/api")
    ).replace(/\/+$/, ""),
    timeout: 120000,
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

export default api;