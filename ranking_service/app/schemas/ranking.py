from pydantic import BaseModel, Field
from typing import List
from uuid import UUID

class PostFeature(BaseModel):
    post_id: UUID
    author_id: UUID
    author_affinity: float = Field(default=0.0, ge=0.0, le=1.0)
    velocity_score: float = Field(default=0.0, ge=0.0)
    recency_hours: float = Field(default=0.0, ge=0.0)
    category: str
    media_count: int = Field(default=0, ge=0)

class RankingRequest(BaseModel):
    user_id: UUID
    candidates: List[PostFeature]

class RankedPost(BaseModel):
    post_id: UUID
    score: float

class RankingResponse(BaseModel):
    user_id: UUID
    ranked_candidates: List[RankedPost]
