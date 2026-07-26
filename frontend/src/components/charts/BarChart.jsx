import { useState } from 'react'

export default function BarChart({ data, title, onBarClick, emptyMessage }) {
  const [hoverIndex, setHoverIndex] = useState(null)

  if (!data || data.length === 0) {
    return (
      <div className="chart-card">
        {title && <h3 className="chart-title">{title}</h3>}
        <p className="status">{emptyMessage ?? 'No data yet.'}</p>
      </div>
    )
  }

  const maxValue = Math.max(1, ...data.map((d) => d.value))

  return (
    <div className="chart-card">
      {title && <h3 className="chart-title">{title}</h3>}
      <div className="bar-chart">
        {data.map((d, i) => {
          const pct = (d.value / maxValue) * 100
          const clickable = Boolean(onBarClick)
          return (
            <div
              key={d.label}
              className={`bar-row ${clickable ? 'bar-row-clickable' : ''}`}
              onMouseEnter={() => setHoverIndex(i)}
              onMouseLeave={() => setHoverIndex(null)}
              onClick={clickable ? () => onBarClick(d) : undefined}
              role={clickable ? 'button' : undefined}
              tabIndex={clickable ? 0 : undefined}
            >
              <div className="bar-label" title={d.label}>
                {d.label}
              </div>
              <div className="bar-track">
                <div
                  className={`bar-fill ${hoverIndex === i ? 'bar-fill-hover' : ''}`}
                  style={{ width: `${pct}%` }}
                />
              </div>
              <div className="bar-value tabular-nums">{d.value}</div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
