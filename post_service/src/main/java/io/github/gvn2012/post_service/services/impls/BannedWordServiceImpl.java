package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.BannedWord;
import io.github.gvn2012.post_service.repositories.BannedWordRepository;
import io.github.gvn2012.post_service.services.interfaces.IBannedWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannedWordServiceImpl implements IBannedWordService {

    private final BannedWordRepository bannedWordRepository;

    @Override
    @Cacheable(value = "banned_words", key = "'all'")
    public Set<String> getAllBannedWords() {
        log.info("Fetching banned words from database");
        return bannedWordRepository.findAll().stream()
                .map(BannedWord::getWord)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    @CacheEvict(value = "banned_words", key = "'all'")
    public void addWord(String word) {
        String normalizedWord = word.toLowerCase().trim();
        if (!bannedWordRepository.existsByWord(normalizedWord)) {
            BannedWord bannedWord = new BannedWord();
            bannedWord.setWord(normalizedWord);
            bannedWordRepository.save(bannedWord);
            log.info("Added new banned word: {}", normalizedWord);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "banned_words", key = "'all'")
    public void removeWord(String word) {
        String normalizedWord = word.toLowerCase().trim();
        bannedWordRepository.findByWord(normalizedWord)
                .ifPresent(bw -> {
                    bannedWordRepository.delete(bw);
                    log.info("Removed banned word: {}", normalizedWord);
                });
    }

    @Override
    @CacheEvict(value = "banned_words", key = "'all'")
    public void refreshCache() {
        log.info("Manual refresh of banned words cache triggered");
    }
}
