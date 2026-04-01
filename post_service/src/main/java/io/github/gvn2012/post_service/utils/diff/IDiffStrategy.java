package io.github.gvn2012.post_service.utils.diff;

public interface IDiffStrategy {
    byte[] computeDiff(String originalText, String modifiedText);
    String applyDiff(String originalText, byte[] diff);
}
