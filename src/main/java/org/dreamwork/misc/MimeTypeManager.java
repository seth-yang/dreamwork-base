package org.dreamwork.misc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2010-4-26
 * Time: 14:20:35
 */
public class MimeTypeManager {
    private static Map<String, MimeType> types = new ConcurrentHashMap<String, MimeType> ();
    private static Map<String, MimeType> reverse = new ConcurrentHashMap<String, MimeType> ();

    static {
        URL url = MimeTypeManager.class.getResource ("MimeType.xml");
        if (url != null) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance ();
            try {
                DocumentBuilder builder = dbf.newDocumentBuilder ();
                Document doc = builder.parse (url.openStream ());
                NodeList list = doc.getElementsByTagName ("mime-mapping");
                for (int i = 0; i < list.getLength (); i++) {
                    Element mapping = (Element) list.item (i);
                    Node extensionNode = mapping.getElementsByTagName ("extension").item (0);
                    String extension = extensionNode.getTextContent ();
                    Node mimeTypeNode = mapping.getElementsByTagName ("mime-type").item (0);
                    String mimeType = mimeTypeNode.getTextContent ();
                    MimeType mt = new MimeType (extension, mimeType);
                    types.put (extension.toLowerCase (), mt);
                    reverse.put (mimeType.toLowerCase (), mt);
                }
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }

    public static MimeType getMimeType (String ext) {
        return types.get (ext.toLowerCase ());
    }

    public static MimeType getMimeTypeByName (String name) {
        return reverse.get (name.toLowerCase ());
    }

    public static String getType (String ext) {
        MimeType type = types.get (ext.toLowerCase ());
        return type == null ? null : type.getName ();
    }
}