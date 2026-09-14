package uk.sanshinkai.kickstosvg;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public final class SVGOptimizer {
    private SVGOptimizer() {}

    public static String optimize(String svg) throws Exception {
        var src = new InputSource(new StringReader(svg));
        var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);
        var writer = new StringWriter();
        var xslt = new StreamSource(SVGOptimizer.class.getResourceAsStream("strip.xslt"));
        var transformer = TransformerFactory.newInstance().newTransformer(xslt);

        for (var i = 0; i < 10; i++) { // Don't loop forever
            var changed = removeUnusedDefinitions(doc);
            if (!changed) break;
        }

        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    private static NodeList matchingNodes(Document doc, String xpath) throws Exception {
        var expr = XPathFactory.newInstance().newXPath().compile(xpath);
        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
    }

    private static boolean removeUnusedDefinitions(Document doc) throws Exception {
        boolean removed = false;
        var definitions = matchingNodes(doc, "//defs/*[@id]");
        for (var i = 0; i < definitions.getLength(); i++) {
            var definition = (Element) definitions.item(i);
            var id = definition.getAttribute("id");
            var xpath = String.format(Locale.ROOT,
                "//*[@href='#%s']|//*[contains(@style,'url(#%s)')]", id, id);
            var references = matchingNodes(doc, xpath);
            if (references.getLength() == 0) {
                definition.getParentNode().removeChild(definition);
                removed = true;
            }
        }
        return removed;
    }
}
