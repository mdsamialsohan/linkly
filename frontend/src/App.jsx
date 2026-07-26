import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Home from './pages/Home'
import LinkDetails from './pages/LinkDetails'
import TopLinks from './pages/TopLinks'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/links/:shortCode" element={<LinkDetails />} />
        <Route path="/top" element={<TopLinks />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
