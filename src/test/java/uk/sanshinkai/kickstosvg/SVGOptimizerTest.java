package uk.sanshinkai.kickstosvg;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

        var doc = parse(SVGOptimizer.optimize(svg));

        assertEquals(0, matchingNodes(doc, "//*[@id='alpha']").getLength());
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

        var doc = parse(SVGOptimizer.optimize(svg));

        assertEquals(1, matchingNodes(doc, "//*[@id='alpha']").getLength());
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

        var doc = parse(SVGOptimizer.optimize(svg));

        assertEquals(1, matchingNodes(doc, "//*[@id='alpha']").getLength());
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

        var doc = parse(SVGOptimizer.optimize(svg));

        assertEquals(0, matchingNodes(doc, "//*[@id='alpha']").getLength());
        assertEquals(0, matchingNodes(doc, "//*[@id='bravo']").getLength());
        assertEquals(0, matchingNodes(doc, "//*[@id='charlie']").getLength());
        assertEquals(1, matchingNodes(doc, "//*[@id='delta']").getLength());
        assertEquals(1, matchingNodes(doc, "//*[@id='echo']").getLength());
        assertEquals(1, matchingNodes(doc, "//*[@id='foxtrot']").getLength());
    }

    private NodeList matchingNodes(Document doc, String xpath) throws Exception {
        var expr = XPathFactory.newInstance().newXPath().compile(xpath);
        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
    }

    private Document parse(String svg) throws Exception {
        var src = new InputSource(new StringReader(svg));
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);
    }
}

