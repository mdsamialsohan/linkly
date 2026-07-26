import { useState } from 'react'
import { shortenUrl } from '../api'

export default function ShortenForm({ onShortened }) {
  const [longUrl, setLongUrl] = useState('')
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const result = await shortenUrl(longUrl.trim())
      onShortened(result)
      setLongUrl('')
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="shorten-form" onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="https://example.com/some/long/path"
        value={longUrl}
        onChange={(e) => setLongUrl(e.target.value)}
        required
      />
      <button type="submit" disabled={submitting}>
        {submitting ? 'Shortening…' : 'Shorten'}
      </button>
      {error && <p className="error">{error}</p>}
    </form>
  )
}
