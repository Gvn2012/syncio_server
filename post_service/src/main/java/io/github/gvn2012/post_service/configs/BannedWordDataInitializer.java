package io.github.gvn2012.post_service.configs;

import io.github.gvn2012.post_service.services.interfaces.IBannedWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BannedWordDataInitializer implements ApplicationRunner {

    private final IBannedWordService bannedWordService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Bootstrapping initial banned words...");
        List<String> initialWords = List.of("spam", "scam", "badword", "offensive", "testbadword");
        
        for (String word : initialWords) {
            bannedWordService.addWord(word);
        }
        log.info("Finished bootstrapping {} initial banned words.", initialWords.size());
    }
}
