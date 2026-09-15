package uk.sanshinkai.kickstosvg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DefinitionExtractorTest {
    @Test
    public void testExtractsDefinitions() throws Exception {
        var stream = Page.class.getResourceAsStream("kunkunshi-all.svg");
        var fragment = DefinitionExtractor.extract(stream);
        var doc = TestHelper.parseXMLString("<x>" + fragment + "</x>");

        assertEquals(1, TestHelper.matchingNodes(doc, "//*[@id='note_ai']").getLength());
    }
}
