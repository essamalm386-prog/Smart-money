import PropTypes from 'prop-types';
import { scoreColor } from '../lib/format';

/**
 * Tiny SVG sparkline for score history.
 * @param {{ points: Array<{score:number, created_at?:string}>, width?:number, height?:number }} props
 */
export default function Sparkline({ points = [], width = 240, height = 56 }) {
  const values = points
    .map((p) => (typeof p === 'number' ? p : Number(p.score)))
    .filter((n) => !Number.isNaN(n));

  if (values.length === 0) {
    return <p className="text-sm text-slate-500">Aucun historique de score pour l'instant.</p>;
  }
  if (values.length === 1) {
    values.unshift(values[0]);
  }

  const pad = 4;
  const min = Math.min(...values);
  const max = Math.max(...values);
  const range = max - min || 1;
  const stepX = (width - pad * 2) / (values.length - 1);

  const coords = values.map((v, i) => {
    const x = pad + i * stepX;
    const y = pad + (1 - (v - min) / range) * (height - pad * 2);
    return [x, y];
  });

  const path = coords.map(([x, y], i) => `${i === 0 ? 'M' : 'L'}${x.toFixed(1)},${y.toFixed(1)}`).join(' ');
  const areaPath = `${path} L${coords[coords.length - 1][0].toFixed(1)},${height - pad} L${coords[0][0].toFixed(1)},${height - pad} Z`;
  const last = values[values.length - 1];
  const tokens = scoreColor(last);

  return (
    <svg
      width={width}
      height={height}
      viewBox={`0 0 ${width} ${height}`}
      className="max-w-full"
      role="img"
      aria-label={`Courbe de l'historique du score, dernier ${Math.round(last)}`}
    >
      <path d={areaPath} fill={tokens.hex} opacity="0.12" />
      <path d={path} fill="none" stroke={tokens.hex} strokeWidth="2" strokeLinejoin="round" strokeLinecap="round" />
      <circle cx={coords[coords.length - 1][0]} cy={coords[coords.length - 1][1]} r="3" fill={tokens.hex} />
    </svg>
  );
}

Sparkline.propTypes = {
  points: PropTypes.array,
  width: PropTypes.number,
  height: PropTypes.number,
};
