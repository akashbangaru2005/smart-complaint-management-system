const BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

async function jsonRequest(path, options = {}) {
  const response = await fetch(`${BASE}${path}`, options)
  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.message || `Request failed: ${response.status}`)
  return data
}

export const api = {
  base: BASE,
  userLogin: pin => jsonRequest('/api/auth/user', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({pin})
  }),
  adminLogin: password => jsonRequest('/api/auth/admin', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({password})
  }),
  createComplaint: form => jsonRequest('/api/complaints', {
    method: 'POST',
    body: form
  }),
  allComplaints: () => jsonRequest('/api/complaints'),
  getComplaint: id => jsonRequest(`/api/complaints/${encodeURIComponent(id)}`),
  updateComplaint: (id, body) => jsonRequest(`/api/complaints/${encodeURIComponent(id)}`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(body)
  }),
  proofUrl: id => `${BASE}/api/complaints/${encodeURIComponent(id)}/proof`
}
