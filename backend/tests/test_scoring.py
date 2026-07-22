"""Unit tests for the conviction scoring engine."""
from __future__ import annotations

import pytest

from app.services.scoring import (
    SubScores,
    WEIGHTS,
    clamp,
    conviction_score,
    quality_gate,
    recommendation_for,
)


def test_weights_sum_to_one():
    assert abs(sum(WEIGHTS.values()) - 1.0) < 1e-9


def test_clamp_bounds():
    assert clamp(-10) == 0
    assert clamp(150) == 100
    assert clamp(55) == 55


def test_conviction_is_weighted_average():
    sub = SubScores(financial=80, business=60, future=40, contrarian=20, geopolitics=70)
    expected = 0.25 * 80 + 0.20 * 60 + 0.20 * 40 + 0.15 * 20 + 0.20 * 70
    assert conviction_score(sub) == pytest.approx(expected, abs=0.01)


def test_conviction_all_max_is_100():
    assert conviction_score(SubScores(100, 100, 100, 100, 100)) == 100.0


def test_conviction_all_min_is_0():
    assert conviction_score(SubScores(0, 0, 0, 0, 0)) == 0.0


def test_conviction_clamps_out_of_range_inputs():
    # Out-of-range sub-scores are clamped before weighting.
    assert conviction_score(SubScores(200, 200, 200, 200, 200)) == 100.0


@pytest.mark.parametrize(
    "score,expected",
    [(95, "STRONG_BUY"), (70, "BUY"), (50, "HOLD"), (35, "REDUCE"), (10, "AVOID")],
)
def test_recommendation_bands(score, expected):
    assert recommendation_for(score) == expected


def test_quality_gate_filters_small_caps():
    assert quality_gate(1e8, 20, min_market_cap=5e8, max_pe=120) is False
    assert quality_gate(1e10, 20, min_market_cap=5e8, max_pe=120) is True


def test_quality_gate_filters_extreme_pe():
    assert quality_gate(1e10, 500, min_market_cap=5e8, max_pe=120) is False
