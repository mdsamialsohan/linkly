import { useState } from 'react'
import { Link } from 'react-router-dom'

function formatDate(isoString) {
  return new Date(isoString).toLocaleString()
}

function CopyButton({ text }) {
  const [copied, setCopied] = useState(false)

  async function handleCopy() {
    await navigator.clipboard.writeText(text)
    setCopied(true)
    setTimeout(() => setCopied(false), 1500)
  }

  return (
    <button type="button" className="copy-button" onClick={handleCopy}>
      {copied ? 'Copied!' : 'Copy'}
    </button>
  )
}

export default function UrlList({ urls, loading, error }) {
  if (loading) return <p className="status">Loading…</p>
  if (error) return <p className="error">{error}</p>
  if (urls.length === 0) return <p className="status">No short links yet.</p>

  return (
    <table className="url-table">
      <thead>
        <tr>
          <th>Short URL</th>
          <th>Destination</th>
          <th>Clicks</th>
          <th>Created</th>
          <th>Stats</th>
        </tr>
      </thead>
      <tbody>
        {urls.map((url) => (
          <tr key={url.shortCode}>
            <td>
              <a href={url.shortUrl} target="_blank" rel="noreferrer">
                {url.shortUrl}
              </a>
              <CopyButton text={url.shortUrl} />
            </td>
            <td className="destination" title={url.longUrl}>
              {url.longUrl}
            </td>
            <td className="tabular-nums">{url.clickCount}</td>
            <td>{formatDate(url.createdAt)}</td>
            <td className="stats-cell">
              <Link to={`/links/${url.shortCode}`}>View stats</Link>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
