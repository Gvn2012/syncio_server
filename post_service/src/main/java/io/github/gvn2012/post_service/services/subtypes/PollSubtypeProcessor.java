package io.github.gvn2012.post_service.services.subtypes;

import io.github.gvn2012.post_service.dtos.mappers.PostPollMapper;
import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.entities.PollOption;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostPoll;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.repositories.PollOptionRepository;
import io.github.gvn2012.post_service.repositories.PostPollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PollSubtypeProcessor implements PostSubtypeProcessor {

    private final PostPollMapper postPollMapper;
    private final PostPollRepository postPollRepository;
    private final PollOptionRepository pollOptionRepository;

    @Override
    public PostCategory supportedCategory() {
        return PostCategory.POLL;
    }

    @Override
    public void process(Post post, PostCreateRequest request) {
        if (request.getPoll() == null) return;
        PostPoll poll = postPollMapper.toEntity(request.getPoll());
        poll.setPost(post);
        PostPoll saved = postPollRepository.saveAndFlush(poll);
        if (request.getPoll().getOptions() != null) {
            List<PollOption> options = request.getPoll().getOptions().stream()
                    .map(optReq -> {
                        PollOption opt = postPollMapper.toOptionEntity(optReq);
                        opt.setPoll(saved);
                        return opt;
                    }).toList();
            if (!options.isEmpty()) {
                pollOptionRepository.saveAll(options);
                saved.getOptions().addAll(options);
            }
        }
        post.setPoll(saved);
    }
}
