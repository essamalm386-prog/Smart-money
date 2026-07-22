import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

/**
 * Monospace ticker chip. Links to the asset detail page unless `linkTo` is false.
 * @param {{ ticker: string, linkTo?: boolean, className?: string }} props
 */
export default function TickerBadge({ ticker, linkTo = true, className = '' }) {
  const symbol = (ticker || '—').toUpperCase();
  const base =
    'pill bg-slate-800 font-mono font-semibold text-slate-100 ring-1 ring-terminal-border';

  if (!linkTo || !ticker) {
    return <span className={`${base} ${className}`}>{symbol}</span>;
  }

  return (
    <Link
      to={`/assets/${encodeURIComponent(ticker)}`}
      className={`${base} transition-colors hover:bg-slate-700 hover:text-emerald-300 ${className}`}
      aria-label={`Voir les détails de ${symbol}`}
    >
      {symbol}
    </Link>
  );
}

TickerBadge.propTypes = {
  ticker: PropTypes.string,
  linkTo: PropTypes.bool,
  className: PropTypes.string,
};
