import pandas as pd
import numpy as np
from typing import List
from ..main import PostFeature

class FeatureProcessor:
    CATEGORY_MAPPING = {
        "GENERAL": 0,
        "ANNOUNCEMENT": 1,
        "EVENT": 2,
        "POLL": 3,
        "QUESTION": 4
    }

    @classmethod
    def process_features(cls, candidates: List[PostFeature]) -> pd.DataFrame:
        """
        Converts a list of PostFeature objects into a pandas DataFrame ready for XGBoost DMatrix.
        """
        data = []
        for post in candidates:
            data.append({
                "author_affinity": post.author_affinity,
                "velocity_score": post.velocity_score,
                "recency_hours": post.recency_hours,
                "category_id": cls.CATEGORY_MAPPING.get(post.category.upper(), 0),
                "media_count": post.media_count,
                # Derived features
                "freshness_boost": 1.0 / (1.0 + post.recency_hours),
                "interaction_density": post.velocity_score / (1.0 + post.recency_hours)
            })
            
        df = pd.DataFrame(data)
        
        # Ensure correct column order for model consistency
        column_order = [
            "author_affinity", 
            "velocity_score", 
            "recency_hours", 
            "category_id", 
            "media_count",
            "freshness_boost",
            "interaction_density"
        ]
        
        return df[column_order]
