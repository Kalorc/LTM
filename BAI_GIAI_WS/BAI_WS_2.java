// sTiPG8KK - WS DataService - cong 2221 - tinh TONG cac phan tu
// Ban TU DO: doc WSDL + XSD de lay dung namespace, dung TEN THAM SO va dang qualified.
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class BAI_WS_2 {
    static final String SV  = "B23DCCN430";
    static final String Q   = "sTiPG8KK";
    static final String URL = "http://36.50.135.242:2221/DataService";

    static String NS = "http://soap.server.network.judge.dblab.ptit/";  // lay tu WSDL, tu ghi de ben duoi
    static boolean QUALIFIED = false;        // schema co elementFormDefault="qualified" khong
    static Map<String, List<String>> THAM_SO = new HashMap<>();   // tenHam -> [ten tham so theo thu tu]

    public static void main(String[] args) throws Exception {
        docWsdl();          // (0) doc WSDL + cac XSD duoc import

        // (1) getData: thu lan luot cac bo ten tham so cho den khi co <return>
        List<String> so = null;
        for (List<String> ten : ungVien("getData", 2)) {
            String req = tham(ten, SV, Q);
            String resp = call("getData", req);
            List<String> v = values(resp, "return");
            System.out.println("--- thu " + ten + " -> " + (v.isEmpty() ? "rong" : v.size() + " phan tu"));
            if (!v.isEmpty()) { so = v; THAM_SO.put("getData", ten); break; }
            if (resp.contains("Fault")) System.out.println("    " + resp);
        }
        if (so == null) {
            System.out.println("!!! Van rong. XML goi gan nhat va phan hoi:");
            System.out.println(call("getData", tham(Arrays.asList("arg0", "arg1"), SV, Q)));
            return;
        }
        System.out.println("Nhan: " + so);

        // (2) Tinh TONG tat ca phan tu (yeu cau b)
        int tong = 0;
        for (String v : so) tong += Integer.parseInt(v.trim());
        System.out.println("Gui : " + tong);

        // (3) Nop - dung cung kieu ten tham so da chay duoc o buoc 1
        for (List<String> ten : ungVien("submitDataInt", 3)) {
            // Tham so thu 3 la so nguyen -> gui thang, khong dat trong dau nhay
            String ack = call("submitDataInt", tham(ten, SV, Q, String.valueOf(tong)));
            List<String> kt = values(ack, "return");
            System.out.println("Server (" + ten + "): " + (kt.isEmpty() ? ack : kt.toString()));
            if (!kt.isEmpty()) break;
        }
    }

    /* ---------- doc WSDL + XSD, lay NS / qualified / ten tham so ---------- */
    static void docWsdl() throws Exception {
        String wsdl = get(URL + "?wsdl");
        List<String> ns = timTatCa(wsdl, "targetNamespace=\"([^\"]+)\"");
        if (!ns.isEmpty()) NS = ns.get(0);
        System.out.println("--- targetNamespace: " + NS);
        System.out.println("--- operation      : " + timTatCa(wsdl, "<(?:\\w+:)?operation name=\"([^\"]+)\""));

        List<String> schemas = new ArrayList<>();
        schemas.add(wsdl);
        for (String loc : timTatCa(wsdl, "schemaLocation=\"([^\"]+)\"")) {   // thuong la DataService?xsd=1
            try {
                String u = loc.startsWith("http") ? loc : URL.substring(0, URL.lastIndexOf('/') + 1) + loc;
                schemas.add(get(u));
                System.out.println("--- doc them schema: " + u);
            } catch (Exception e) { /* bo qua */ }
        }
        for (String s : schemas) {
            if (s.contains("elementFormDefault=\"qualified\"")) QUALIFIED = true;
            docThamSo(s);
        }
        System.out.println("--- qualified      : " + QUALIFIED);
        System.out.println("--- tham so        : " + THAM_SO);
    }

    /* Tim trong schema: element/complexType ten X -> danh sach ten the con theo thu tu */
    static void docThamSo(String xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            Document d = f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            for (String tag : new String[]{"complexType", "element"}) {
                NodeList nl = d.getElementsByTagNameNS("*", tag);
                for (int i = 0; i < nl.getLength(); i++) {
                    Element e = (Element) nl.item(i);
                    String ten = e.getAttribute("name");
                    if (ten.isEmpty() || e.getParentNode() == null) continue;
                    String cha = e.getParentNode().getLocalName();
                    if (!"schema".equals(cha)) continue;            // chi lay khai bao muc cao nhat
                    List<String> con = new ArrayList<>();
                    NodeList seq = e.getElementsByTagNameNS("*", "element");
                    for (int j = 0; j < seq.getLength(); j++) {
                        String t = ((Element) seq.item(j)).getAttribute("name");
                        if (!t.isEmpty()) con.add(t);
                    }
                    if (!con.isEmpty()) THAM_SO.putIfAbsent(ten, con);
                }
            }
        } catch (Exception ignore) { }
    }

    /* Cac bo ten tham so se thu: tu schema truoc, roi den 2 kieu pho bien */
    static List<List<String>> ungVien(String ham, int n) {
        List<List<String>> r = new ArrayList<>();
        List<String> tuSchema = THAM_SO.get(ham);
        if (tuSchema != null && tuSchema.size() == n) r.add(tuSchema);
        List<String> arg = new ArrayList<>();
        for (int i = 0; i < n; i++) arg.add("arg" + i);
        r.add(arg);
        r.add(n == 2 ? Arrays.asList("studentCode", "qCode")
                     : Arrays.asList("studentCode", "qCode", "data"));
        LinkedHashSet<List<String>> bo = new LinkedHashSet<>(r);      // bo trung
        return new ArrayList<>(bo);
    }

    static String tham(List<String> ten, String... giaTri) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ten.size() && i < giaTri.length; i++)
            sb.append("<").append(ten.get(i)).append(">").append(esc(giaTri[i]))
              .append("</").append(ten.get(i)).append(">");
        return sb.toString();
    }

    static String esc(String s) { return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    /* ---------- HTTP ---------- */
    static String call(String ham, String arg) throws Exception {
        // qualified: them xmlns mac dinh de cac the con cung thuoc namespace cua dich vu
        String mac = QUALIFIED ? " xmlns=\"" + NS + "\"" : "";
        String xml = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
                + "<n:" + ham + " xmlns:n=\"" + NS + "\"" + mac + ">" + arg + "</n:" + ham + ">"
                + "</S:Body></S:Envelope>";
        HttpURLConnection c = (HttpURLConnection) new URL(URL).openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        c.setRequestProperty("SOAPAction", "\"\"");
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        c.getOutputStream().write(xml.getBytes("UTF-8"));
        InputStream is = c.getResponseCode() >= 400 ? c.getErrorStream() : c.getInputStream();
        return doc(is);
    }

    static String get(String u) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        InputStream is = c.getResponseCode() >= 400 ? c.getErrorStream() : c.getInputStream();
        return doc(is);
    }

    static String doc(InputStream is) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[4096];
        int n;
        while ((n = is.read(b)) > 0) bos.write(b, 0, n);
        return bos.toString("UTF-8");
    }

    static List<String> values(String xml, String the) {
        List<String> r = new ArrayList<>();
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            Document d = f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            NodeList nl = d.getElementsByTagNameNS("*", the);
            for (int i = 0; i < nl.getLength(); i++) r.add(nl.item(i).getTextContent());
        } catch (Exception ignore) { }
        return r;
    }

    static List<String> timTatCa(String s, String re) {
        LinkedHashSet<String> r = new LinkedHashSet<>();
        Matcher m = Pattern.compile(re).matcher(s);
        while (m.find()) r.add(m.group(1));
        return new ArrayList<>(r);
    }
}
