# --- Smart Money backend image ---
FROM python:3.11-slim AS base

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

# System deps needed by some scientific wheels (yfinance/pandas).
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential curl && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --upgrade pip && pip install -r requirements.txt

COPY . .

EXPOSE 8000

# Apply migrations then launch. Falls back to create_all via app lifespan.
CMD ["sh", "-c", "alembic upgrade head || true && uvicorn app.main:app --host 0.0.0.0 --port 8000"]
