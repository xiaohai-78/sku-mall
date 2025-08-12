import axios from 'axios';

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
});

http.interceptors.response.use(
  (res) => {
    const data = res.data;
    if (data && typeof data === 'object' && 'data' in data) {
      return data.data;
    }
    return data;
  },
  (err) => Promise.reject(err)
);

export default http; 