import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import ShortenForm from '../components/ShortenForm'
import UrlList from '../components/UrlList'
import { fetchUrls } from '../api'

export default function Home() {
  const [urls, setUrls] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const loadUrls = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setUrls(await fetchUrls())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadUrls()
  }, [loadUrls])

  function handleShortened(newUrl) {
    setUrls((prev) => [{ ...newUrl, clickCount: 0 }, ...prev])
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Linkly</h1>
        <Link to="/top" className="nav-link">
          Top Links →
        </Link>
      </div>
      <ShortenForm onShortened={handleShortened} />
      <UrlList urls={urls} loading={loading} error={error} />
    </div>
  )
}
