import pandas as pd
from typing import List
from ..schemas.ranking import PostFeature

class FeatureProcessor:
    CATEGORY_MAPPING = {
        "NORMAL": 0,
        "POLL": 1,
        "EVENT": 2,
        "TASK": 3,
        "ANNOUNCEMENT": 4
    }

    @classmethod
    def process_features(cls, candidates: List[PostFeature]) -> pd.DataFrame:
        """
        Converts a list of PostFeature objects into a pandas DataFrame ready for XGBoost.
        """
        data = []
        for post in candidates:
            data.append({
                "author_affinity": post.author_affinity,
                "velocity_score": post.velocity_score,
                "recency_hours": post.recency_hours,
                "category_id": cls.CATEGORY_MAPPING.get(post.category.upper(), 0),
                "media_count": post.media_count,
                "freshness_boost": 1.0 / (1.0 + post.recency_hours),
                "interaction_density": post.velocity_score / (1.0 + post.recency_hours)
            })
            
        df = pd.DataFrame(data)
        
        column_order = [
            "author_affinity", 
            "velocity_score", 
            "recency_hours", 
            "category_id", 
            "media_count",
            "freshness_boost",
            "interaction_density"
        ]
        
        for col in column_order:
            if col not in df.columns:
                df[col] = 0.0
                
        return df[column_order]
