package io.github.gvn2012.user_service.services.interfaces;

public interface IBloomFilterService {
    
    boolean mightContainUsername(String username);
    
    void addUsername(String username);
    
    boolean mightContainEmail(String email);
    
    void addEmail(String email);
    
    void initialize();
}
