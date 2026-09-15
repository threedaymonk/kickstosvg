package uk.sanshinkai.kickstosvg;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

final class TestHelper {
    private TestHelper() {}

    public static NodeList matchingNodes(Document doc, String xpath) throws Exception {
        var expr = XPathFactory.newInstance().newXPath().compile(xpath);
        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
    }

    public static Document parseXMLString(String svg) throws Exception {
        var src = new InputSource(new StringReader(svg));
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);
    }
}
