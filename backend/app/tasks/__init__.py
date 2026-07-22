"""Background task package."""
from app.tasks.scan_runner import run_scan

__all__ = ["run_scan"]
