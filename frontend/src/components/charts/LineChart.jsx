import { useState } from 'react'

const WIDTH = 640
const HEIGHT = 220
const PAD_LEFT = 40
const PAD_RIGHT = 12
const PAD_TOP = 12
const PAD_BOTTOM = 28

function formatHour(isoString) {
  return new Date(isoString).toLocaleString(undefined, {
    weekday: undefined,
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function LineChart({ data, title }) {
  const [hoverIndex, setHoverIndex] = useState(null)

  if (!data || data.length === 0) {
    return (
      <div className="chart-card">
        {title && <h3 className="chart-title">{title}</h3>}
        <p className="status">No clicks in this window yet.</p>
      </div>
    )
  }

  const plotWidth = WIDTH - PAD_LEFT - PAD_RIGHT
  const plotHeight = HEIGHT - PAD_TOP - PAD_BOTTOM
  const maxClicks = Math.max(1, ...data.map((d) => d.clicks))

  const x = (i) =>
    data.length === 1
      ? PAD_LEFT + plotWidth / 2
      : PAD_LEFT + (i / (data.length - 1)) * plotWidth
  const y = (clicks) => PAD_TOP + plotHeight - (clicks / maxClicks) * plotHeight

  const linePath = data
    .map((d, i) => `${i === 0 ? 'M' : 'L'} ${x(i)} ${y(d.clicks)}`)
    .join(' ')
  const areaPath = `${linePath} L ${x(data.length - 1)} ${PAD_TOP + plotHeight} L ${x(0)} ${PAD_TOP + plotHeight} Z`

  const gridLines = [0, 0.25, 0.5, 0.75, 1]
  const tickIndices =
    data.length <= 1
      ? [0]
      : [0, Math.floor((data.length - 1) / 2), data.length - 1]

  function handleMove(e) {
    const rect = e.currentTarget.getBoundingClientRect()
    const relX = ((e.clientX - rect.left) / rect.width) * WIDTH
    let nearest = 0
    let nearestDist = Infinity
    data.forEach((_, i) => {
      const dist = Math.abs(x(i) - relX)
      if (dist < nearestDist) {
        nearestDist = dist
        nearest = i
      }
    })
    setHoverIndex(nearest)
  }

  const hovered = hoverIndex !== null ? data[hoverIndex] : null

  return (
    <div className="chart-card">
      {title && <h3 className="chart-title">{title}</h3>}
      <svg
        viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
        className="chart-svg"
        onMouseMove={handleMove}
        onMouseLeave={() => setHoverIndex(null)}
      >
        {gridLines.map((frac) => {
          const gy = PAD_TOP + plotHeight * (1 - frac)
          return (
            <g key={frac}>
              <line
                x1={PAD_LEFT}
                x2={WIDTH - PAD_RIGHT}
                y1={gy}
                y2={gy}
                className="chart-gridline"
              />
              <text x={PAD_LEFT - 8} y={gy + 4} className="chart-axis-label" textAnchor="end">
                {Math.round(maxClicks * frac)}
              </text>
            </g>
          )
        })}

        <path d={areaPath} className="chart-area" />
        <path d={linePath} className="chart-line" />
        {data.length === 1 && (
          <circle cx={x(0)} cy={y(data[0].clicks)} r={4} className="chart-dot" />
        )}

        {tickIndices.map((i) => (
          <text
            key={i}
            x={x(i)}
            y={HEIGHT - 8}
            className="chart-axis-label"
            textAnchor="middle"
          >
            {formatHour(data[i].hour)}
          </text>
        ))}

        {hovered && (
          <g>
            <line
              x1={x(hoverIndex)}
              x2={x(hoverIndex)}
              y1={PAD_TOP}
              y2={PAD_TOP + plotHeight}
              className="chart-crosshair"
            />
            <circle cx={x(hoverIndex)} cy={y(hovered.clicks)} r={4} className="chart-dot" />
          </g>
        )}
      </svg>
      {hovered && (
        <div className="chart-tooltip">
          <strong>{hovered.clicks}</strong> clicks at {formatHour(hovered.hour)}
        </div>
      )}
    </div>
  )
}
