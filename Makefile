.PHONY: help install backend frontend test migrate seed up down

help:
	@echo "Smart Money — make targets"
	@echo "  make install    Install backend + frontend deps"
	@echo "  make backend    Run FastAPI dev server (:8000)"
	@echo "  make frontend   Run Vite dev server (:5173)"
	@echo "  make test       Run backend test suite"
	@echo "  make migrate    Apply Alembic migrations"
	@echo "  make seed       Run a demo scan to populate data"
	@echo "  make up / down  Docker compose up / down"

install:
	cd backend && pip install -r requirements.txt
	cd frontend && npm install

backend:
	cd backend && uvicorn app.main:app --reload --port 8000

frontend:
	cd frontend && npm run dev

test:
	cd backend && pytest

migrate:
	cd backend && alembic upgrade head

seed:
	cd backend && python -m app.seed

up:
	cd deployment && docker compose up --build

down:
	cd deployment && docker compose down
