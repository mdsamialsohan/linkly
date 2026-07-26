import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import BarChart from '../components/charts/BarChart'
import { fetchTopLinks } from '../api'

const RANGES = [
  { label: '24h', hours: 24 },
  { label: '7d', hours: 24 * 7 },
  { label: '30d', hours: 24 * 30 },
]

export default function TopLinks() {
  const navigate = useNavigate()
  const [hoursBack, setHoursBack] = useState(24)
  const [topLinks, setTopLinks] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setTopLinks(await fetchTopLinks(hoursBack, 10))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [hoursBack])

  useEffect(() => {
    load()
  }, [load])

  return (
    <div className="page">
      <div className="page-header">
        <Link to="/" className="nav-link">
          ← Back
        </Link>
      </div>

      <h1>Top Links</h1>

      <div className="range-picker">
        {RANGES.map((r) => (
          <button
            key={r.hours}
            type="button"
            className={`range-button ${hoursBack === r.hours ? 'range-button-active' : ''}`}
            onClick={() => setHoursBack(r.hours)}
          >
            {r.label}
          </button>
        ))}
      </div>

      {error && <p className="error">{error}</p>}
      {loading && !topLinks && <p className="status">Loading…</p>}

      {topLinks && (
        <BarChart
          data={topLinks.map((t) => ({
            label: t.shortCode,
            value: t.totalClicks,
            longUrl: t.longUrl,
          }))}
          title={`Most clicked (last ${hoursBack}h)`}
          emptyMessage="No clicks in this window."
          onBarClick={(d) => navigate(`/links/${d.label}`)}
        />
      )}
    </div>
  )
}
