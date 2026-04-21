import logging
import os
from typing import List, Optional
from uuid import UUID
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import uvicorn
import xgboost as xgb
import pandas as pd
import numpy as np

# Absolute import for the processor
from utils.feature_processor import FeatureProcessor

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ranking-service")

app = FastAPI(title="SyncIO Ranking Service", version="1.0.0")

class PostFeature(BaseModel):
    post_id: UUID
    author_id: UUID
    author_affinity: float = 0.0
    velocity_score: float = 0.0
    recency_hours: float = 0.0
    category: str
    media_count: int = 0

class RankingRequest(BaseModel):
    user_id: UUID
    candidates: List[PostFeature]

class RankedPost(BaseModel):
    post_id: UUID
    score: float

class RankingResponse(BaseModel):
    user_id: UUID
    ranked_candidates: List[RankedPost]

class ModelManager:
    def __init__(self, model_path: str = "models/ranking_v1.json"):
        self.model_path = model_path
        self.model = self._load_model()

    def _load_model(self):
        if os.path.exists(self.model_path):
            logger.info(f"Loading trained XGBoost model from {self.model_path}")
            return xgb.Booster(model_file=self.model_path)
        else:
            logger.warning(f"No model found at {self.model_path}. Initializing synthetic booster.")
            return self._create_synthetic_booster()

    def _create_synthetic_booster(self):
        """
        Creates an in-memory XGBoost booster with heuristic weights
        to ensure the service works before a real model is trained.
        """
      
        X = np.array([[0.5, 1.0, 1.0, 0, 1, 0.5, 0.5]])
        y = np.array([1.0])
        dtrain = xgb.DMatrix(X, label=y, feature_names=[
            "author_affinity", "velocity_score", "recency_hours", 
            "category_id", "media_count", "freshness_boost", "interaction_density"
        ])
        
        params = {'objective': 'reg:squarederror', 'eta': 1.0, 'max_depth': 3}
        return xgb.train(params, dtrain, num_boost_round=1)

    def score(self, df: pd.DataFrame) -> np.ndarray:
        dmatrix = xgb.DMatrix(df, feature_names=df.columns.tolist())
        return self.model.predict(dmatrix)

model_manager = ModelManager()

@app.get("/health")
async def health():
    return {"status": "healthy", "model_loaded": model_manager.model is not None}

@app.post("/rank", response_model=RankingResponse)
async def rank_posts(request: RankingRequest):
    if not request.candidates:
        return RankingResponse(user_id=request.user_id, ranked_candidates=[])

    logger.info(f"Ranking {len(request.candidates)} candidates for user {request.user_id}")
    
    try:
        features_df = FeatureProcessor.process_features(request.candidates)
        
        scores = model_manager.score(features_df)
        
        results = []
        for i, post in enumerate(request.candidates):
            results.append(RankedPost(post_id=post.post_id, score=float(scores[i])))
        
        # 4. Sort by score
        results.sort(key=lambda x: x.score, reverse=True)
        
        return RankingResponse(
            user_id=request.user_id,
            ranked_candidates=results
        )
    except Exception as e:
        logger.error(f"Ranking failed: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal Ranking Error")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
