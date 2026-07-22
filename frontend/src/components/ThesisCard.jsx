import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import ConvictionGauge from './ConvictionGauge';
import ScoreBar from './ScoreBar';
import RecommendationBadge from './RecommendationBadge';
import TickerBadge from './TickerBadge';
import SourceBadge from './SourceBadge';
import ForecastBadge from './ForecastBadge';
import { SUBSCORE_META, timeAgo } from '../lib/format';

/**
 * Ranked thesis card: gauge + sub-score mini-bars + recommendation.
 * @param {{ thesis: object, rank?: number }} props
 */
export default function ThesisCard({ thesis, rank }) {
  if (!thesis) return null;
  const {
    ticker,
    name,
    conviction_score,
    recommendation,
    summary,
    created_at,
    data_source,
    data_provider,
    outlook,
    volatility,
    geo_risk,
    geopolitical_score,
  } = thesis;

  return (
    <article className="card card-hover animate-fade-in flex flex-col gap-4 p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            {typeof rank === 'number' ? (
              <span className="font-mono text-xs text-slate-500">#{rank}</span>
            ) : null}
            <TickerBadge ticker={ticker} />
          </div>
          <Link
            to={`/assets/${encodeURIComponent(ticker || '')}`}
            className="mt-2 block truncate text-sm font-semibold text-slate-100 hover:text-emerald-300"
          >
            {name || ticker || 'Inconnu'}
          </Link>
          <p className="mt-0.5 text-[11px] text-slate-500">{timeAgo(created_at)}</p>
        </div>
        <ConvictionGauge score={conviction_score} size="sm" label={false} />
      </div>

      <SourceBadge source={data_source} provider={data_provider} className="self-start" />

      {summary ? <p className="line-clamp-3 text-xs leading-relaxed text-slate-400">{summary}</p> : null}

      <div className="grid grid-cols-2 gap-x-4 gap-y-2">
        {SUBSCORE_META.map((m) => (
          <ScoreBar key={m.key} label={m.label} value={thesis[m.key]} weight={m.weight} compact />
        ))}
      </div>

      <ForecastBadge
        outlook={outlook}
        volatility={volatility}
        geoRisk={geo_risk}
        geoScore={geopolitical_score}
        compact
        className="border-t border-terminal-border pt-3"
      />

      <div className="mt-auto flex items-center justify-between gap-2 pt-1">
        <RecommendationBadge recommendation={recommendation} />
        <Link
          to={`/assets/${encodeURIComponent(ticker || '')}`}
          className="text-xs font-medium text-emerald-400 hover:text-emerald-300"
        >
          Détails →
        </Link>
      </div>
    </article>
  );
}

ThesisCard.propTypes = {
  thesis: PropTypes.object.isRequired,
  rank: PropTypes.number,
};
