import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import StatTile from '../components/StatTile'
import LineChart from '../components/charts/LineChart'
import BarChart from '../components/charts/BarChart'
import { fetchHourly, fetchReferrers, fetchStats } from '../api'

const RANGES = [
  { label: '24h', hours: 24 },
  { label: '7d', hours: 24 * 7 },
  { label: '30d', hours: 24 * 30 },
]

function formatDate(isoString) {
  return isoString ? new Date(isoString).toLocaleString() : '—'
}

export default function LinkDetails() {
  const { shortCode } = useParams()
  const [hoursBack, setHoursBack] = useState(24)
  const [stats, setStats] = useState(null)
  const [hourly, setHourly] = useState(null)
  const [referrers, setReferrers] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [statsRes, hourlyRes, referrersRes] = await Promise.all([
        fetchStats(shortCode),
        fetchHourly(shortCode, hoursBack),
        fetchReferrers(shortCode, hoursBack, 10),
      ])
      setStats(statsRes)
      setHourly(hourlyRes)
      setReferrers(referrersRes)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [shortCode, hoursBack])

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

      <h1 className="mono-code">{shortCode}</h1>

      {error && <p className="error">{error}</p>}
      {loading && !stats && <p className="status">Loading…</p>}

      {stats && (
        <>
          <div className="stat-tile-row">
            <StatTile label="Total clicks" value={stats.totalClicks} />
            <StatTile label="First click" value={formatDate(stats.firstClick)} />
            <StatTile label="Last click" value={formatDate(stats.lastClick)} />
          </div>

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

          <LineChart
            data={(hourly ?? []).map((b) => ({ hour: b.hour, clicks: b.clicks }))}
            title="Clicks over time"
          />

          <BarChart
            data={(referrers ?? []).map((r) => ({ label: r.referrer, value: r.clicks }))}
            title="Top referrers"
            emptyMessage="No referrer data in this window."
          />
        </>
      )}
    </div>
  )
}
