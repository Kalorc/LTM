// uotdRCYu - WS CharacterService - cong 2221 - nhom tu theo so nguyen am
// Ban TU DO: doc WSDL + XSD de lay dung namespace, dung TEN THAM SO va dang qualified.
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class BAI_WS_4 {
    static final String SV  = "B23DCCN430";
    static final String Q   = "uotdRCYu";
    static final String URL = "http://36.50.135.242:2221/CharacterService";

    static String NS = "http://soap.server.network.judge.dblab.ptit/";  // lay tu WSDL, tu ghi de ben duoi
    // === CACH NOP - doi so nay neu WA ===
    // 1 = danh sach PHANG: moi TU la mot phan tu (dung vi du cua de: [apple, banana, grape, kiwi, pear])
    // 2 = moi NHOM la mot phan tu: "apple,banana" / "grape,kiwi,pear"  (dung phan mo ta cua de)
    // 3 = GOP het thanh MOT chuoi duy nhat
    // Da thu 2 -> WA du chuoi hien thi trung khop => ranh gioi nhom moi la cho lech -> dung 1.
    static final int CHE_DO = 1;
    static final String NGAN_CACH = ",";     // dung cho che do 2 va 3: phay KHONG dau cach (da xac nhan)
    static final boolean NHOM_GIAM = false;  // nhom IT nguyen am dung TRUOC (da xac nhan)

    static boolean QUALIFIED = false;        // schema co elementFormDefault="qualified" khong
    static Map<String, List<String>> THAM_SO = new HashMap<>();   // tenHam -> [ten tham so theo thu tu]

    public static void main(String[] args) throws Exception {
        docWsdl();          // (0) doc WSDL + cac XSD duoc import

        // (1) getData: thu lan luot cac bo ten tham so cho den khi co <return>
        List<String> so = null;
        for (List<String> ten : ungVien("requestStringArray", 2)) {
            String req = tham(ten, SV, Q);
            String resp = call("requestStringArray", req);
            List<String> v = values(resp, "return");
            System.out.println("--- thu " + ten + " -> " + (v.isEmpty() ? "rong" : v.size() + " phan tu"));
            if (!v.isEmpty()) { so = v; THAM_SO.put("getData", ten); break; }
            if (resp.contains("Fault")) System.out.println("    " + resp);
        }
        if (so == null) {
            System.out.println("!!! Van rong. XML goi gan nhat va phan hoi:");
            System.out.println(call("requestStringArray", tham(Arrays.asList("arg0", "arg1"), SV, Q)));
            return;
        }
        System.out.println("Nhan: " + so);

        // (2) Nhom cac tu theo SO NGUYEN AM (yeu cau b)
        // TreeMap tu sap xep khoa; reverseOrder() -> nhom nhieu nguyen am dung truoc
        Comparator<Integer> thuTu = NHOM_GIAM ? Comparator.<Integer>reverseOrder()
                                              : Comparator.<Integer>naturalOrder();
        TreeMap<Integer, List<String>> nhom = new TreeMap<>(thuTu);
        for (String w : so)
            nhom.computeIfAbsent(demNguyenAm(w), x -> new ArrayList<>()).add(w);

        List<String> kq = new ArrayList<>();
        List<String> phang = new ArrayList<>();          // tat ca cac tu theo dung thu tu da sap
        for (Map.Entry<Integer, List<String>> e : nhom.entrySet()) {
            Collections.sort(e.getValue());              // sap tu dien trong tung nhom
            phang.addAll(e.getValue());
            if (CHE_DO == 2) kq.add(String.join(NGAN_CACH, e.getValue()));   // moi nhom mot chuoi
            System.out.println("   nhom " + e.getKey() + " nguyen am: " + e.getValue());
        }
        if (CHE_DO == 1) kq = phang;                                   // moi tu mot phan tu
        if (CHE_DO == 3) kq = new ArrayList<>(Arrays.asList(String.join(NGAN_CACH, phang)));

        System.out.println("So phan tu nop: " + kq.size());
        System.out.println("Noi lai de doi chieu voi dong Expected: " + String.join(",", kq));
        System.out.println("Gui : " + kq);

        // (3) Nop: tham so thu 3 la DANH SACH -> lap lai the cho tung phan tu (yeu cau c)
        for (List<String> ten : ungVien("submitCharacterStringArray", 3)) {
            String req = tham(ten.subList(0, 2), SV, Q) + lap(ten.get(2), kq);
            String ack = call("submitCharacterStringArray", req);
            List<String> kt = values(ack, "return");
            System.out.println("Server (" + ten + "): " + (kt.isEmpty() ? ack : kt.toString()));
            if (!kt.isEmpty()) break;
        }
    }

    /* Lap lai the <ten> cho moi phan tu: <arg2>100</arg2><arg2>97</arg2>... */
    static String lap(String ten, List<String> vals) {
        StringBuilder sb = new StringBuilder();
        for (String v : vals) sb.append("<").append(ten).append(">").append(esc(v))
                                .append("</").append(ten).append(">");
        return sb.toString();
    }

    /* Dem nguyen am trong mot tu: chi a e i o u, khong phan biet HOA/thuong.
       Neu server cham ke ca 'y' la nguyen am thi them 'y' vao chuoi duoi day. */
    static int demNguyenAm(String w) {
        int d = 0;
        for (char c : w.toLowerCase().toCharArray())
            if ("aeiou".indexOf(c) >= 0) d++;
        return d;
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
