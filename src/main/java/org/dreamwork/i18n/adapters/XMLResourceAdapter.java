package org.dreamwork.i18n.adapters;

import org.dreamwork.i18n.*;
import org.dreamwork.util.FileInfo;
import org.dreamwork.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-19
 * Time: 下午3:30
 */
public class XMLResourceAdapter extends AbstractResourceAdapter {
    private static DocumentBuilder builder;

    private static final Logger logger = LoggerFactory.getLogger (XMLResourceAdapter.class);
    private AbstractResourceManager manager;
    private Set<XMLResourceAdapter> importedAdapters;
    private Locale defaultLocale;

    public XMLResourceAdapter (AbstractResourceManager manager, Locale defaultLocale, URL... resources) throws ParserConfigurationException, IOException, SAXException {
        this.manager = manager;
        this.defaultLocale = defaultLocale;
        initDocumentBuilder ();
        for (URL url : resources) {
            if (logger.isTraceEnabled ())
                logger.trace ("Trying to load resources from {}", url);
            Element root = parse (url);

            NodeList list = root.getElementsByTagName ("import");
            if (list != null && list.getLength () > 0) {
                importedAdapters = new HashSet<XMLResourceAdapter> ();
                for (int i = 0; i < list.getLength (); i ++) {
                    Element e = (Element) list.item (i);
                    String location = e.getAttribute ("file");
                    if (StringUtil.isEmpty (location)) continue;

                    processImport (url, location);
                }
            }

            list = root.getElementsByTagName ("bundle");
            for (int i = 0; i < list.getLength (); i ++) {
                Element bundle = (Element) list.item (i);
                String language = bundle.getAttribute ("language");
                if (isEmpty (language))
                    throw new MissingResourceException ("Attribute \"language\" of element <bundle> is need!");

                language = language.trim ();
                Locale locale = LocaleUtil.parseLocale (language);
                XMLResourceBundle rb = new XMLResourceBundle (bundle);
                softCache.put (locale, rb);
            }
        }
    }

    @Override
    public String getString (Locale locale, String name, String defaultValue) {
        String value = super.getString (locale, name, (String) null);
        if (value != null)
            return value;

        if (importedAdapters != null) for (XMLResourceAdapter adapter : importedAdapters) {
            value = adapter.getString (locale, name, (String) null);
            if (value != null)
                return value;
        }
        return defaultValue;
    }

    @Override
    public void loadResources (String baseName, Locale defaultLocale) throws MissingResourceException {
        this.resourceName = baseName;
        if (!isLocaleSupport (defaultLocale))
            throw new MissingResourceException ("Default locale [" + defaultLocale.getDisplayName () + "] missing.");
        defaultResourceBundle = softCache.get (defaultLocale);
    }

    /**
     * 装载指定名称和区域设置的资源绑定器
     *
     * @param locale   区域设置
     * @return 资源绑定器
     */
    @Override
    protected IResourceBundle loadResourceByLocale (Locale locale) {
        return softCache.get (locale);
    }

    protected Map<Locale, IResourceBundle> getAllResourceBundle () {
        return softCache;
    }

    private Element parse (URL url) throws IOException, SAXException {
        Document doc = builder.parse (url.openStream ());
        return doc.getDocumentElement ();
    }

    private void initDocumentBuilder () throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
        builder = factory.newDocumentBuilder ();
    }

    private void processImport (URL base, String location) throws IOException, SAXException, ParserConfigurationException {
        String baseName = FileInfo.getFileNameWithoutExtension (location);
        XMLResourceAdapter adapter = (XMLResourceAdapter) manager.getResourceAdapter (baseName);
        if (adapter != null) {
            importedAdapters.add (adapter);
        } else {
            String basedir = base.toString ();
            String path = FileInfo.getFolder (basedir);
            path = FileInfo.getAbsolutePath (path, location);
            if (!path.endsWith (".xml"))
                path += ".xml";
//            String ext = FileInfo.getExtension (location);
            URL url = new URL (path);
            adapter = new XMLResourceAdapter (manager, defaultLocale, url);
            manager.mergeAdapter (baseName, adapter);
            importedAdapters.add (adapter);
        }
    }
}