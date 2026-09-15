package uk.sanshinkai.kickstosvg;

import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public final class DefinitionExtractor {
    private DefinitionExtractor() {}

    public static String extract(InputStream stream) throws Exception {
        var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
        var writer = new StringWriter();
        var xslt = new StreamSource(
            DefinitionExtractor.class.getResourceAsStream("definitions.xslt"));
        var transformer = TransformerFactory.newInstance().newTransformer(xslt);

        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}
