import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/* Bo cong cu SOAP 1.1 tu gui XML - chay tren MOI JDK, khong can wsimport. */
public class Soap {

    static String NS = "http://ws/";   // lay tu targetNamespace trong WSDL

    /* Goi 1 phuong thuc SOAP, tra ve XML phan hoi (dang chuoi) */
    static String call(String url, String ham, String args) throws Exception {
        String xml = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
                + "<n:" + ham + " xmlns:n=\"" + NS + "\">" + args + "</n:" + ham + ">"
                + "</S:Body></S:Envelope>";
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        c.setRequestProperty("SOAPAction", "\"\"");
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        c.getOutputStream().write(xml.getBytes("UTF-8"));
        InputStream is = c.getResponseCode() >= 400 ? c.getErrorStream() : c.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[4096]; int n;
        while ((n = is.read(b)) > 0) bos.write(b, 0, n);
        return bos.toString("UTF-8");
    }

    static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    /* Danh sach gia tri cua moi the <ten> (bo qua namespace) - dung cho List<Integer>/List<String> */
    static List<String> values(String xml, String ten) throws Exception {
        NodeList nl = parse(xml).getElementsByTagNameNS("*", ten);
        List<String> r = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) r.add(nl.item(i).getTextContent());
        return r;
    }

    /* Danh sach doi tuong: moi the <return> -> Map<tenTruong, giaTri> (giu thu tu truong) */
    static List<Map<String, String>> objects(String xml, String ten) throws Exception {
        NodeList nl = parse(xml).getElementsByTagNameNS("*", ten);
        List<Map<String, String>> r = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Map<String, String> m = new LinkedHashMap<>();
            NodeList ch = nl.item(i).getChildNodes();
            for (int j = 0; j < ch.getLength(); j++)
                if (ch.item(j).getNodeType() == Node.ELEMENT_NODE)
                    m.put(ch.item(j).getLocalName(), ch.item(j).getTextContent());
            r.add(m);
        }
        return r;
    }

    /* Dung tham so doi tuong: <arg2><ten>gt</ten>...</arg2> - GIU DUNG THU TU nhu WSDL */
    static String obj(String arg, Map<String, String> m) {
        StringBuilder sb = new StringBuilder("<" + arg + ">");
        for (Map.Entry<String, String> e : m.entrySet())
            sb.append("<").append(e.getKey()).append(">").append(esc(e.getValue()))
              .append("</").append(e.getKey()).append(">");
        return sb.append("</").append(arg).append(">").toString();
    }

    /* Dung tham so danh sach: lap lai the <arg2> moi phan tu */
    static String list(String arg, List<String> vals) {
        StringBuilder sb = new StringBuilder();
        for (String v : vals) sb.append("<").append(arg).append(">").append(esc(v))
                                .append("</").append(arg).append(">");
        return sb.toString();
    }

    /* Danh sach doi tuong lam tham so */
    static String listObj(String arg, List<Map<String, String>> xs) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> m : xs) sb.append(obj(arg, m));
        return sb.toString();
    }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
