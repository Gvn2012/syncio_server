import grpc
from concurrent import futures
import logging
from .ranking_pb2 import RankingResponse, RankedPost
from .ranking_pb2_grpc import RankingServiceServicer, add_RankingServiceServicer_to_server
from .services.processor import FeatureProcessor
from .services.engine import ModelManager

logger = logging.getLogger("ranking-service-grpc")

class RankingService(RankingServiceServicer):
    def __init__(self):
        self.engine = ModelManager()

    def RankPosts(self, request, context):
        if not request.candidates:
            return RankingResponse(user_id=request.user_id, ranked_candidates=[])

        logger.info(f"gRPC: Ranking {len(request.candidates)} posts for user {request.user_id}")

        try:
            candidates_data = [
                type('obj', (object,), {
                    'post_id': c.post_id,
                    'author_id': c.author_id,
                    'author_affinity': c.author_affinity,
                    'velocity_score': c.velocity_score,
                    'recency_hours': c.recency_hours,
                    'category': c.category,
                    'media_count': c.media_count
                }) for c in request.candidates
            ]

            features_df = FeatureProcessor.process_features(candidates_data)
            scores = self.engine.predict(features_df)

            ranked_posts = [
                RankedPost(post_id=request.candidates[i].post_id, score=float(scores[i]))
                for i in range(len(request.candidates))
            ]
            ranked_posts.sort(key=lambda x: x.score, reverse=True)

            return RankingResponse(
                user_id=request.user_id,
                ranked_candidates=ranked_posts
            )
        except Exception as e:
            logger.error(f"gRPC Inference pipeline failed: {str(e)}")
            context.abort(grpc.StatusCode.INTERNAL, "Ranking Engine Failure")

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    add_RankingServiceServicer_to_server(RankingService(), server)
    server.add_insecure_port('[::]:9090')
    logger.info("gRPC server starting on port 9090")
    server.start()
    return server
