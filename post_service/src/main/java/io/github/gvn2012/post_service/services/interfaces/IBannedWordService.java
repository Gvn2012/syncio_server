package io.github.gvn2012.post_service.services.interfaces;

import java.util.List;

public interface IBannedWordService {
    List<String> getAllBannedWords();
    void addWord(String word);
    void removeWord(String word);
    void refreshCache();
}
