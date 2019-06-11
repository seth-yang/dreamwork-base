package org.dreamwork.config;

import org.dreamwork.util.DefaultConverter;
import org.dreamwork.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-3-29
 * Time: 下午3:19
 */
public class ApplicationConfigParser implements FileChangeListener {
    private Document doc;
    private FileMonitor monitor;
    private String version;
    private boolean realtime = false;
    private Map<String, Class<? extends IXMLConfigParser>> parserClasses = new HashMap<String, Class<? extends IXMLConfigParser>> ();
    private Map<String, Object> values = new HashMap<String, Object> ();
    private WeakHashMap<String, Object> indices = new WeakHashMap<String, Object> ();

    private static final DocumentBuilder builder;
    private static Logger logger = LoggerFactory.getLogger (ApplicationConfigParser.class);
    private static final Object locker = new Object ();
    private static final Pattern p = Pattern.compile ("[\\./]");
    private static final Pattern TOKEN = Pattern.compile ("\\$\\{(.*?)\\}");

    static {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
            builder = factory.newDocumentBuilder ();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException (e);
        }
    }

    /**
     * 构造函数.
     * 使用一个输入流来构建解析器
     * @param in 输入流
     * @throws IOException
     * @throws SAXException
     */
    public ApplicationConfigParser (InputStream in) throws IOException, SAXException {
        try {
            doc = builder.parse (in);
        } finally {
            in.close ();
        }
    }

    /**
     * 构造函数.
     * 使用一个输入流来构建解析器
     * @param url URL
     * @throws IOException
     * @throws SAXException
     */
    public ApplicationConfigParser (URL url) throws IOException, SAXException {
        this (url.openStream ());
        try {
            File file = getFile (url);
            monitor = new FileMonitor (file);
        } catch (Exception ex) {
            //
        }
    }

    /**
     * 构造函数.
     * 使用一个文件来构建解析器
     * @param file 配置文件
     * @throws IOException
     * @throws SAXException
     */
    public ApplicationConfigParser (File file) throws IOException, SAXException {
        doc = builder.parse (file);
        monitor = new FileMonitor (file);
    }

    public ApplicationConfigParser (InputSource source) throws IOException, SAXException {
        doc = builder.parse (source);
    }

    /**
     * 添加一个自定义解析器
     * @param section 解析器名称
     * @param parser 解析器类
     */
    public void addParserClass (String section, Class<? extends IXMLConfigParser> parser) {
        parserClasses.put (section, parser);
    }

    /**
     * 获取自定义块
     * @return 自定义块迭代器
     */
    public Iterator<String> getUserDefineSections () {
        return parserClasses.keySet ().iterator ();
    }

    /**
     * 获取所有自定义块解析器
     * @return 自定义块解析器类的迭代器
     */
    public Iterator<Class<? extends IXMLConfigParser>> getSectionParserType () {
        return parserClasses.values ().iterator ();
    }

    public String getVersion () {
        return version;
    }

    public boolean isRealtime () {
        return realtime;
    }

    public void stop () {
        if (monitor != null)
            monitor.terminate ();
    }

    /**
     * 使用 XSD 文档对当前配置文件进行验证
     * @param xsd W3C Schema 文档
     * @return 验证结果，若符合 Schema 规范，返回 true, 否则 false
     * @throws SAXException
     * @throws IOException
     */
    public boolean validateByXSD (InputStream xsd) throws SAXException, IOException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source source = new StreamSource (xsd);
        Schema schema = factory.newSchema (source);
        source = new DOMSource (doc);
        schema.newValidator ().validate (source);
        return true;
    }

    public Document getDocument () {
        return doc;
    }

    /**
     * 获取配置文件中的值
     * @param expression 表达式. 使用.分隔的路径表达式
     * @return 值
     * @throws ConfigParseException
     */
    public Object getValue (String expression) throws ConfigParseException{
        if (StringUtil.isEmpty (expression)) return null;

        if (indices.containsKey (expression))
            return indices.get (expression);

        String[] a = p.split (expression.trim ());
        if (a.length == 1) {
            Object o = values.get (expression);
            if (o != null)
                indices.put (expression, o);
            return o;
        }

        Object container = values.get (a [0]);
        if (!(container instanceof ConfigEntry))
            throw new ConfigParseException (a[0] + " is not a ConfigEntry!");
        for (int i = 1; i < a.length - 1; i ++) {
            ConfigEntry entry = (ConfigEntry) container;
            String name = a[i];
            Object o = entry.getValue (name);
            if (!(o instanceof ConfigEntry)) {
                String part = StringUtil.join (a, ".", 0, i);
                throw new ConfigParseException (part + " is not a ConfigEntry!");
            }
            container = o;
        }
        Object o = ((ConfigEntry) container).getValue (a [a.length - 1]);

        if (o instanceof String) {
            String text = (String) o;
            Matcher m = TOKEN.matcher (text);
            StringBuffer buff = new StringBuffer ();
            while (m.find ()) {
                String g = m.group (1);
                m.appendReplacement (buff, (String) getValue (g));
            }
            m.appendTail (buff);
            text = buff.toString ();
            indices.put (expression, text);
            return text;
        }

        indices.put (expression, o);
        return o;
    }

    public String getString (String expression) {
        Object value = getValue (expression);
        if (value instanceof CharSequence)
            return value.toString ();
        return String.valueOf (value);
    }

    public int getInteger (String expression) {
        Number number = getNumber (expression);
        if (number != null)
            return number.intValue ();

        String text = getString (expression);
        return Integer.parseInt (text);
    }

    public long getLong (String expression) {
        Number number = getNumber (expression);
        if (number != null)
            return number.longValue ();

        String text = getString (expression);
        return Long.parseLong (text);
    }

    public double getDouble (String expression) {
        Number number = getNumber (expression);
        if (number != null)
            return number.doubleValue ();

        return Double.parseDouble (getString (expression));
    }

    public float getFloat (String expression) {
        Number number = getNumber (expression);
        if (number != null)
            return number.floatValue ();

        return Float.parseFloat (getString (expression));
    }

    public boolean getBoolean (String expression) {
        Object value = getValue (expression);
        if (value instanceof Boolean)
            return (Boolean) value;

        return Boolean.valueOf (getString (expression));
    }

    public Date getDate (String expression) {
        Object value = getValue (expression);
        if (value instanceof Date)
            return (Date) value;

        if (value instanceof Number)
            return new Date (((Number) value).longValue ());

        return new DefaultConverter ().cast (Date.class, getString (expression));
    }

    public Number getNumber (String expression) {
        Object value = getValue (expression);
        if (value instanceof Number)
            return (Number) value;

        return null;
    }

    public boolean contains (String expression) {
        try {
            return getValue (expression) != null;
        } catch (Exception ex) {
            return false;
        }
    }

    public void addFileChangeListener (FileChangeListener listener) {
        if (monitor != null)
            monitor.addFileChangedListener (listener);
    }

    public void parse () {
        synchronized (locker) {
            indices.clear ();
            Element root = doc.getDocumentElement ();
            parseMetaData (root);
            // 提取解析器
            parseSections (root);
            // 解析正文
            parseContent (root);
            if (realtime && monitor != null && monitor.getState () == Thread.State.NEW) {
                monitor.addFileChangedListener (this);
                monitor.start ();
            }

            doc = null;
        }
    }

    private void parseMetaData (Element root) {
        version = null;
        realtime = false;
/*
        if (root.hasAttribute ("version"))
            version = root.getAttribute ("version").trim ();
        if (root.hasAttribute ("realtime")) {
            String rt = root.getAttribute ("realtime").trim ();
            realtime = "true".equalsIgnoreCase (rt);
        }
*/
        SimpleElementParser p = new SimpleElementParser ();
        ConfigEntry e = p.parse (root);
        for (String name : e.values.keySet ()) {
            if ("version".equals (name))
                version = (String) e.values.get (name);
            else if ("realtime".equals (name)) {
                String rt = root.getAttribute ("realtime").trim ();
                realtime = "true".equalsIgnoreCase (rt);
            } else {
                values.put (name, e.values.get (name));
            }
        }

    }

    @SuppressWarnings ("unchecked")
    private void parseSections (Element root) throws ConfigParseException {
        NodeList list = root.getElementsByTagName ("section");
        for (int i = 0; i < list.getLength (); i ++) {
            Element section = (Element) list.item (i);
            if (!section.hasAttribute ("name"))
                throw new ConfigParseException ("Attribute 'name' of element 'section' must be specified !");
            String name = section.getAttribute ("name").trim ();
            if (!section.hasAttribute ("parser"))
                throw new ConfigParseException ("Attribute 'parser' of element 'section' must be specified!");
            String parser = section.getAttribute ("parser").trim ();

            try {
                Class parserClass = Class.forName (parser);
                if (!IXMLConfigParser.class.isAssignableFrom (parserClass))
                    throw new ConfigParseException ("parser must implement org.dreamwork.config.IXMLConfigParser");
                parserClasses.put (name, parserClass);
            } catch (ClassNotFoundException ex) {
                logger.error (ex.getMessage (), ex);
                throw new ConfigParseException (ex);
            }
        }
    }

    private void parseContent (Element root) throws ConfigParseException {
        NodeList list = root.getChildNodes ();
        PropertyConfigParser pcp = new PropertyConfigParser ();
        SimpleElementParser sep = new SimpleElementParser ();
        ListParser listParser = new ListParser ();
        for (int i = 0; i < list.getLength (); i ++) {
            Node node = list.item (i);
            if (node.getNodeType () != Node.ELEMENT_NODE) continue;

            String name = node.getNodeName ();
            if ("section".equals (name)) continue;

            if ("property".equals (name)) {
                KeyValuePair<String> pair = pcp.parse ((Element) node);
                values.put (pair.getName (), pair.getValue ());
                continue;
            }

            if ("list".equalsIgnoreCase (name)) {
                String key = ((Element) node).getAttribute ("name");
                values.put (key, listParser.parse ((Element) node));
                continue;
            }

            Class<? extends IXMLConfigParser> type = parserClasses.get (name);
            if (type != null) {
                try {
                    IXMLConfigParser parser = type.newInstance ();
                    values.put (name, parser.parse ((Element) node));
                } catch (Exception ex) {
                    logger.error (ex.getMessage (), ex);
                    throw new ConfigParseException (ex);
                }
            } else {
                Element e = (Element) node;
                ConfigType ct = ConfigType.getElementType (e);
                ConfigEntry entry;
                switch (ct) {
                    case EmptyElement:
                        entry = sep.parse (e);
                        if (entry != null)
                            values.put (e.getNodeName (), entry);
                        break;
                    case ContentElement:
                        values.put (e.getNodeName (), e.getTextContent ());
                        break;
                    case ComplexElement:
                        entry = new ConfigEntry ();
                        new DefaultXMLConfigParser (entry).parse (e);
                        values.put (e.getNodeName (), entry);
                        break;
                }
            }
        }
    }

    private File getFile (URL url) throws IOException {
        String path = url.getFile ();
        return new File (path).getCanonicalFile ();
    }

    public void fileChanged (File file) throws IOException {
        try {
            changeResource (new FileInputStream (file));
        } catch (SAXException ex) {
            throw new IOException (ex.getMessage ());
        }
    }

    private void changeResource (InputStream in) throws IOException, SAXException {
        try {
            doc = builder.parse (in);
        } finally {
            in.close ();
        }
        parse ();
    }
}