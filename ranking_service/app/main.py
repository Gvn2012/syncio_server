import logging
from fastapi import FastAPI
from .api.routes import router as api_router

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

app = FastAPI(
    title="SyncIO Ranking Service",
    description="Python sidecar for high-performance ML-based feed ranking using XGBoost.",
    version="2.0.0"
)

app.include_router(api_router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
