# Conviction Scoring Model

Smart Money assigns every asset a **conviction score from 0 to 100**. It is a
weighted blend of four independent analyst sub-scores, each itself bounded to
0–100.

## Formula (v2 — with geopolitics)

```
CONVICTION = 0.25 · FINANCIAL
           + 0.20 · BUSINESS
           + 0.20 · FUTURE
           + 0.15 · CONTRARIAN
           + 0.20 · GEOPOLITICS
```

The weights sum to exactly **1.00** (asserted at import time in
`app/services/scoring.py`). Each sub-score is clamped to `[0, 100]` before
weighting, so the final result is always a valid 0–100 value.

| Dimension    | Weight | Owner agent          | Inputs |
|--------------|:------:|----------------------|--------|
| Financial    | 25 %   | Quant Analyst        | valuation (P/E), revenue & earnings growth, free cash-flow, debt/equity, operating margins |
| Business     | 20 %   | Business Analyst     | moat (gross margin), market scale (market cap), management (ROE), competitiveness (profit margin) |
| Future       | 20 %   | Future Potential     | revenue runway, PEG, forward-vs-trailing P/E |
| Contrarian   | 15 %   | Contrarian Analyst   | media saturation (inverted), analyst crowding |
| Geopolitics  | 20 %   | Analyste Géopolitique | country stability, sector geostrategic exposure, live conflict/sanctions news tension |

## Fluctuation forecast (prévision)

Beyond the score, each thesis carries a forward read to anticipate future moves:

- **Perspective (`outlook`)**: `Haussière` / `Neutre` / `Prudente`, from
  `0.45·future + 0.25·contrarian + 0.30·geopolitics`.
- **Volatilité attendue (`volatility`)**: `Faible` / `Modérée` / `Élevée`, from
  the asset's beta, geopolitical score, and contrarian surprise.
- **Risque géopolitique (`geo_risk`)**: `Faible` / `Modéré` / `Élevé`.

## Recommendation bands

| Score      | Label        |
|------------|--------------|
| ≥ 80       | STRONG_BUY   |
| 65 – 79    | BUY          |
| 45 – 64    | HOLD         |
| 30 – 44    | REDUCE       |
| < 30       | AVOID        |

## Why contrarian is *inverted* coverage

The platform's edge is finding opportunities **before** they are mainstream. A
name with heavy recent media coverage is already crowded, so its contrarian
score is *lowered*; a quiet, high-quality name scores higher. Media saturation
is measured via the DuckDuckGo news signal (`app/services/search.py`).

## Determinism & testability

The numeric spine is fully deterministic and computed without any LLM, so it is
unit-tested (`tests/test_scoring.py`, `tests/test_pipeline.py`). When an
Anthropic API key is configured, the CrewAI Investment Committee adds narrative
synthesis (thesis / risks / catalysts) on top of these numbers — it never
changes the scores.
