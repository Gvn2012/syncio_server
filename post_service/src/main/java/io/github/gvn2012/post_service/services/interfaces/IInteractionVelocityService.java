package io.github.gvn2012.post_service.services.interfaces;

import java.util.List;
import java.util.UUID;

public interface IInteractionVelocityService {

    void recordInteraction(UUID postId, InteractionType type);

    List<UUID> getTrendingPosts(int limit);

    double getVelocityScore(UUID postId);

    enum InteractionType {
        LIKE(1.0), COMMENT(3.0), SHARE(5.0);

        private final double weight;

        InteractionType(double weight) {
            this.weight = weight;
        }

        public double getWeight() {
            return weight;
        }
    }
}
