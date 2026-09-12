package uk.sanshinkai.kickstosvg;

import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public final class DefinitionExtractor {
    private DefinitionExtractor() {}

    public static String extract(Document doc) throws Exception {
        var writer = new StringWriter();
        var transformer = TransformerFactory.newInstance().newTransformer();

        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        var expr = XPathFactory.newInstance().newXPath().compile("//defs/*");
        var nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        for (var i = 0; i < nodeList.getLength(); i++)
            transformer.transform(new DOMSource(nodeList.item(i)), new StreamResult(writer));

        return writer.toString();
    }
}
