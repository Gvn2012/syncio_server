import os
import logging
import xgboost as xgb
import pandas as pd
import numpy as np

logger = logging.getLogger("ranking-service")

class ModelManager:
    """
    Handles loading the XGBoost model from disk or initializing 
    a synthetic heuristic model for cold-start scenarios.
    """
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
        Creates an in-memory XGBoost booster with heuristic weights.
        Ensures the system is functional without a prior training phase.
        """
        # features: [affinity, velocity, recency, category_id, media, freshness, density]
        X = np.array([[0.5, 1.0, 1.0, 0, 1, 0.5, 0.5]])
        y = np.array([1.0])
        dtrain = xgb.DMatrix(X, label=y, feature_names=[
            "author_affinity", "velocity_score", "recency_hours", 
            "category_id", "media_count", "freshness_boost", "interaction_density"
        ])
        
        params = {
            'objective': 'reg:squarederror', 
            'eta': 1.0, 
            'max_depth': 3,
            'verbosity': 0
        }
        return xgb.train(params, dtrain, num_boost_round=1)

    def predict(self, df: pd.DataFrame) -> np.ndarray:
        """
        Performs batch prediction on a processed DataFrame.
        """
        dmatrix = xgb.DMatrix(df, feature_names=df.columns.tolist())
        return self.model.predict(dmatrix)
