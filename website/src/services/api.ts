const API_BASE = 'http://localhost:5000/api';

const getHeaders = () => {
  const token = localStorage.getItem('ascend_token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };
};

export const api = {
  async get(endpoint: string) {
    const res = await fetch(`${API_BASE}${endpoint}`, { headers: getHeaders() });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },
  async post(endpoint: string, body: any) {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(body)
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.error || 'Request failed');
    }
    return res.json();
  }
};
