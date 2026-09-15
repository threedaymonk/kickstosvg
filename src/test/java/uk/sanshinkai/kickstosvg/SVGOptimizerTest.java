package uk.sanshinkai.kickstosvg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SVGOptimizerTest {
    @Test
    public void testRemovesUnusedDefinitions() throws Exception {
        var svg =
            """
            <svg xmlns="http://www.w3.org/2000/svg">
                <defs>
                    <path d="M 0,0 h 4" id="alpha"/>
                </defs>
            </svg>
            """;

        var doc = TestHelper.parseXMLString(SVGOptimizer.optimize(svg));

        assertEquals(0, TestHelper.matchingNodes(doc, "//*[@id='alpha']").getLength());
    }

    @Test
    public void testRetainsUseDefinitions() throws Exception {
        var svg =
            """
            <svg xmlns="http://www.w3.org/2000/svg">
                <defs>
                    <path d="M 0,0 h 4" id="alpha"/>
                </defs>
                <use x="0" y="0" href="#alpha"/>
            </svg>
            """;

        var doc = TestHelper.parseXMLString(SVGOptimizer.optimize(svg));

        assertEquals(1, TestHelper.matchingNodes(doc, "//*[@id='alpha']").getLength());
    }

    @Test
    public void testRetainsStyleDefinitions() throws Exception {
        var svg =
            """
            <svg xmlns="http://www.w3.org/2000/svg">
                <defs>
                    <path d="M 0,0 h 4" id="alpha"/>
                </defs>
                <path d="M 0,0 v 1" style="marker-end: url(#alpha)"/>
            </svg>
            """;

        var doc = TestHelper.parseXMLString(SVGOptimizer.optimize(svg));

        assertEquals(1, TestHelper.matchingNodes(doc, "//*[@id='alpha']").getLength());
    }

    @Test
    public void testRecursivelyRemovesUnusedDefinitions() throws Exception {
        var svg =
            """
            <svg xmlns="http://www.w3.org/2000/svg">
                <defs>
                    <path d="M 0,0 h 4" id="alpha"/>
                    <use id="bravo" href="#alpha"/>
                    <use id="charlie" href="#bravo"/>
                    <path d="M 0,0 v 4" id="delta"/>
                    <use id="echo" href="#delta"/>
                    <use id="foxtrot" href="#echo"/>
                </defs>
                <use x="0" y="0" href="#foxtrot"/>
            </svg>
            """;

        var doc = TestHelper.parseXMLString(SVGOptimizer.optimize(svg));

        assertEquals(0, TestHelper.matchingNodes(doc, "//*[@id='alpha']").getLength());
        assertEquals(0, TestHelper.matchingNodes(doc, "//*[@id='bravo']").getLength());
        assertEquals(0, TestHelper.matchingNodes(doc, "//*[@id='charlie']").getLength());
        assertEquals(1, TestHelper.matchingNodes(doc, "//*[@id='delta']").getLength());
        assertEquals(1, TestHelper.matchingNodes(doc, "//*[@id='echo']").getLength());
        assertEquals(1, TestHelper.matchingNodes(doc, "//*[@id='foxtrot']").getLength());
    }
}

