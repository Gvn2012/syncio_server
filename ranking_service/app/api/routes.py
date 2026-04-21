import logging
from fastapi import APIRouter, HTTPException
from ..schemas.ranking import RankingRequest, RankingResponse, RankedPost
from ..services.processor import FeatureProcessor
from ..services.engine import ModelManager

logger = logging.getLogger("ranking-service")
router = APIRouter()

engine = ModelManager()

@router.get("/health")
async def health():
    return {
        "status": "healthy", 
        "model_loaded": engine.model is not None,
        "engine": "xgboost"
    }

@router.post("/rank", response_model=RankingResponse)
async def rank_candidates(request: RankingRequest):
    if not request.candidates:
        return RankingResponse(user_id=request.user_id, ranked_candidates=[])

    logger.info(f"Ranking {len(request.candidates)} posts for user {request.user_id}")
    
    try:
        features_df = FeatureProcessor.process_features(request.candidates)
        
        scores = engine.predict(features_df)
        
        ranked_posts = [
            RankedPost(post_id=post.post_id, score=float(scores[i]))
            for i, post in enumerate(request.candidates)
        ]
        ranked_posts.sort(key=lambda x: x.score, reverse=True)
        
        return RankingResponse(
            user_id=request.user_id,
            ranked_candidates=ranked_posts
        )
    except Exception as e:
        logger.error(f"Inference pipeline failed: {str(e)}")
        raise HTTPException(status_code=500, detail="Ranking Engine Failure")
