package io.github.gvn2012.post_service.utils.diff;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DiffStrategyTest {

    private final MyersDiffStrategy myers = new MyersDiffStrategy();
    private final PatienceDiffStrategy patience = new PatienceDiffStrategy(myers);
    private final HistogramDiffStrategy histogram = new HistogramDiffStrategy(myers);

    @Test
    public void testMyersBasic() {
        testStrategy(myers, "Hello World", "Hello Synchronized World");
    }

    @Test
    public void testPatienceBasic() {
        testStrategy(patience, "line1\nline2\nline3", "line1\nline2.5\nline3");
    }

    @Test
    public void testHistogramBasic() {
        testStrategy(histogram, "A\nB\nC\nD", "A\nC\nB\nD");
    }

    @Test
    public void testHtmlContent() {
        String original = "<p>Hello @user</p>\n<div>Some content</div>";
        String modified = "<p>Hello <b>@user</b></p>\n<div>Updated content</div>";
        
        testStrategy(myers, original, modified);
        testStrategy(patience, original, modified);
        testStrategy(histogram, original, modified);
    }

    @Test
    public void testEmptyAndNull() {
        testStrategy(myers, "", "something");
        testStrategy(patience, "something", "");
        testStrategy(histogram, null, "new");
    }

    private void testStrategy(IDiffStrategy strategy, String original, String modified) {
        if (original == null) original = "";
        if (modified == null) modified = "";

        byte[] diff = strategy.computeDiff(original, modified);
        assertNotNull(diff);
        
        String result = strategy.applyDiff(original, diff);
        assertEquals(result, modified, "Strategy " + strategy.getClass().getSimpleName() + " failed to reconstruct modified text");
    }
}
