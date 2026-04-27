package io.github.gvn2012.user_service.services.interfaces;

public interface IBloomFilterService {
    
    boolean mightContainUsername(String username);
    void addUsername(String username);
    
    boolean mightContainEmail(String email);
    void addEmail(String email);

    boolean mightContainPhone(String phoneNumber);
    void addPhone(String phoneNumber);
    
    void initialize();
    void repopulate();
}
