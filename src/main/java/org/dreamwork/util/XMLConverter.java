package org.dreamwork.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 11-8-4
 * Time: 上午11:29
 */
@Deprecated
public class XMLConverter implements IConverter {
    public <T> T cast (Class<T> type, String expression) {
        try {
            Reader reader = new StringReader (expression);
            InputSource source = new InputSource (reader);
//            InputStream in = new ByteArrayInputStream (expression.getBytes ());
            DocumentBuilder builder = DocumentBuilderFactory.newInstance ().newDocumentBuilder ();
            Document doc = builder.parse (source);
            return NodeConverter.parseNode (type, doc.getDocumentElement ());
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    @SuppressWarnings ("unchecked")
    public String cast (Object value) {
        if (value == null) return "";
        try {
            Class type = value.getClass ();
            String className = type.getName ();
            int pos = className.lastIndexOf ('.');
            String rootName = pos != -1 ? className.substring (pos + 1) : className;
            DocumentBuilder builder = DocumentBuilderFactory.newInstance ().newDocumentBuilder ();
            Document doc = builder.newDocument ();

//            boolean parsed = false;
            ConverterInfo ci = ReferenceUtil.getAnnotation (type, ConverterInfo.class);
            if (ci != null) {
                Class<? extends IConverter> cc = ci.converter ();
                if (cc != DefaultConverter.class) {
                    IConverter c = cc.newInstance ();
                    return c.cast (value);
                }
            }

//            if (!parsed) {
                if (type.isArray () || value instanceof java.util.Collection) rootName = "list";
                else if (value instanceof java.util.Map) rootName = "map";

                Element root = doc.createElement (rootName);
                doc.appendChild (root);
                NodeConverter.parseObject (type, value, root);
//            }

            TransformerFactory tf = TransformerFactory.newInstance ();
            Transformer transformer = tf.newTransformer ();
            StringWriter writer = new StringWriter ();
            Source source = new DOMSource (doc);
            Result result = new StreamResult (writer);
            transformer.transform (source, result);
            writer.flush ();
            return writer.getBuffer ().toString ();
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    public String cast (Object value, String format) {
        return cast (value);
    }

    public byte[] castToByteArray (Object value) {
        return new byte[0];
    }
}