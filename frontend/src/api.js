const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

async function parseErrorMessage(response) {
  try {
    const body = await response.json()
    if (Array.isArray(body.message)) return body.message.join(', ')
    if (typeof body.message === 'string') return body.message
  } catch {
    // response had no JSON body
  }
  return `Request failed with status ${response.status}`
}

async function get(path) {
  const response = await fetch(`${API_BASE_URL}${path}`)
  if (!response.ok) {
    throw new Error(await parseErrorMessage(response))
  }
  return response.json()
}

export async function shortenUrl(longUrl) {
  const response = await fetch(`${API_BASE_URL}/api/shorten`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ longUrl }),
  })
  if (!response.ok) {
    throw new Error(await parseErrorMessage(response))
  }
  return response.json()
}

export function fetchUrls() {
  return get('/api/urls')
}

export function fetchStats(shortCode) {
  return get(`/api/analytics/${shortCode}`)
}

export function fetchHourly(shortCode, hoursBack = 24) {
  return get(`/api/analytics/${shortCode}/hourly?hoursBack=${hoursBack}`)
}

export function fetchReferrers(shortCode, hoursBack = 24, limit = 10) {
  return get(`/api/analytics/${shortCode}/referrers?hoursBack=${hoursBack}&limit=${limit}`)
}

export function fetchTopLinks(hoursBack = 24, limit = 10) {
  return get(`/api/analytics/top?hoursBack=${hoursBack}&limit=${limit}`)
}
