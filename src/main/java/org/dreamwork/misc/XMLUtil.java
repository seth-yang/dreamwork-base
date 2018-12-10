package org.dreamwork.misc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 13-8-12
 * Time: 下午5:33
 */
public class XMLUtil {
    private static DocumentBuilder builder;
    private static SchemaFactory schemaFactory;

    private synchronized static DocumentBuilder getDocumentBuilder () throws ParserConfigurationException {
        if (builder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
            factory.setNamespaceAware (true);
            builder = factory.newDocumentBuilder ();
        }
        return builder;
    }

    public static Document parse (String string) throws IOException, ParserConfigurationException, SAXException {
        return parse (new ByteArrayInputStream (string.getBytes ("utf-8")));
    }

    public static Document parse (File file) throws ParserConfigurationException, IOException, SAXException {
        return getDocumentBuilder ().parse (file);
    }

    public static Document parse (URL url) throws IOException, ParserConfigurationException, SAXException {
        InputStream in = url.openStream ();
        try {
            return parse (in);
        } finally {
            in.close ();
        }
    }

    public static Document parse (InputStream in) throws ParserConfigurationException, IOException, SAXException {
        return getDocumentBuilder ().parse (in);
    }

    public static void print (Element e, OutputStream out) throws TransformerException, IOException {
        Source source = new DOMSource (e);
        Result result = new StreamResult (out);
        TransformerFactory tf = TransformerFactory.newInstance ();
        Transformer transformer = tf.newTransformer ();
        transformer.transform (source, result);
        out.flush ();
    }

    public static Document createDocument () throws ParserConfigurationException {
        return getDocumentBuilder ().newDocument ();
    }

    public static void validDocumentByXSD (Source xsd, Source doc) throws SAXException, IOException {
        synchronized (XMLUtil.class) {
            if (schemaFactory == null) {
                schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            }
        }

        Schema schema = schemaFactory.newSchema (xsd);
        schema.newValidator ().validate (doc);
    }
}