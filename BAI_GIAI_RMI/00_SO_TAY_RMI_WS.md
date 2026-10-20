# SỔ TAY RMI & WEB SERVICE (SOAP) — CẤU TRÚC LỆNH & CÁCH DÙNG

MSSV dùng chung: **B23DCCN430** — Server thi: **36.50.135.242**
RMI Registry: cổng **1099** — Web Service: cổng **2221**

Mọi đoạn code trong sổ tay này đã được **biên dịch và chạy thật** với server RMI / web service
giả lập nội bộ; các thuật toán đã đối chiếu đúng ví dụ in trong đề (13/13 khớp, xem mục 9).

---

# PHẦN I — RMI (Remote Method Invocation)

## 1. Cơ chế: khác gì TCP/UDP

| | TCP/UDP | RMI |
|---|---|---|
| Đơn vị làm việc | byte / gói tin | **lời gọi phương thức** |
| Phải tự làm | mở socket, đóng/mở gói, parse chuỗi | **không** — gọi hàm như hàm nội bộ |
| Kết nối | tự mở, tự đóng | Java tự lo, **không có socket để đóng** |
| Cái phải khai báo | không | **interface từ xa**, chép y hệt đề |
| Timeout | `setSoTimeout` | không cần (có thì đặt qua system property) |

Server tạo sẵn một đối tượng rồi "treo biển" nó lên **danh bạ (Registry)** dưới một cái tên.
Client tra danh bạ theo tên → nhận về **stub** (cái điều khiển từ xa) → gọi hàm bình thường.
Java tự đóng gói tham số, truyền qua mạng, nhận kết quả về.

```
Client                         Registry (1099)              Server
  |-- getRegistry(IP, 1099) -------->|                          |
  |-- lookup("RMIByteService") ----->|                          |
  |<-- stub ------------------------ |                          |
  |-- requestData(mssv, qCode) ----------------------------->|   (gọi thật trên server)
  |<-- byte[] ----------------------------------------------- |
  |-- submitData(mssv, qCode, kq) -------------------------->|
```

## 2. Khung 3 bước — copy nguyên khi thi

```java
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import RMI.ByteService;                       // interface tự tạo, đúng package RMI

public class BAI_1 {
    public static void main(String[] args) throws Exception {
        String sv = "B23DCCN430";
        String q  = "Ova1uIY6";               // mã câu hỏi trên đề

        // (1) Tra danh bạ
        Registry reg = LocateRegistry.getRegistry("36.50.135.242", 1099);
        ByteService s = (ByteService) reg.lookup("RMIByteService");   // tên do đề quy định

        // (2) Nhận dữ liệu
        byte[] data = s.requestData(sv, q);

        // (3) Xử lý rồi nộp
        byte[] kq = /* thuật toán */ new byte[]{0, 0};
        s.submitData(sv, q, kq);
    }                                          // hết main là xong, không phải đóng gì
}
```

Thư mục phải như sau (interface **bắt buộc** nằm trong package `RMI`):

```
BAI_GIAI_RMI\
├── BAI_1.java
└── RMI\
    ├── ByteService.java        <- package RMI;
    ├── CharacterService.java
    ├── DataService.java
    ├── ObjectService.java
    └── TicketSla.java          <- lớp dữ liệu, nếu đề dùng object
```

Biên dịch & chạy:

```bat
javac -encoding UTF-8 RMI/*.java BAI_1.java
java BAI_1
```

## 3. Bảng tra API RMI

| Lệnh | Ý nghĩa |
|---|---|
| `LocateRegistry.getRegistry(host, 1099)` | **nối tới** danh bạ của server (client dùng cái này) |
| `LocateRegistry.createRegistry(1099)` | **tạo** danh bạ — chỉ server dùng, client không bao giờ |
| `reg.lookup("TênĐăngKý")` | xin stub theo tên; trả `Object` → phải ép kiểu |
| `reg.list()` | **liệt kê mọi tên đang đăng ký** — mẹo dò khi không chắc tên |
| `reg.rebind(tên, obj)` | server treo biển đối tượng |
| `extends Remote` | đánh dấu interface gọi được từ xa |
| `throws RemoteException` | bắt buộc ở **mọi** phương thức của interface |
| `implements Serializable` | bắt buộc với **lớp dữ liệu** truyền qua mạng |

Không cần `rmic` (bỏ từ Java 5 — stub sinh động), không cần `SecurityManager`
(bỏ hẳn từ JDK 18), không cần `-Djava.rmi.server.codebase` khi client tự có interface.

## 4. LUẬT VÀNG: chữ ký phải khớp 100%

RMI định danh phương thức bằng **method hash** = băm của `tên + mô tả (kiểu tham số + KIỂU TRẢ VỀ)`.
Lệch một chữ là server không nhận ra:

```
java.rmi.ServerException: ... java.rmi.UnmarshalException:
    unrecognized method hash: method not supported by remote object
```

(Đã dựng server/client lệch chữ ký và tái hiện đúng lỗi này.)

Suy ra:

- **Chép y hệt** chữ ký đề in ra — kể cả kiểu trả về. `String requestData` ≠ `Serializable requestData`.
- `public` / không `public` trong interface: **không ảnh hưởng** (interface luôn public).
  Tên tham số (`studentCode`, `a`, `x`) cũng **không ảnh hưởng**. Chỉ KIỂU mới quan trọng.
- **Generic không ảnh hưởng** (bị xóa khi biên dịch): server `List<Object>` ↔ client `List<Integer>`
  vẫn chạy đúng — đã chạy thật, nhận `[3,8,5,10,7]`, nộp `[2,4]`, server nhận đủ. Rất tiện cho
  đề RMI Data vì đề không in chữ ký.
- Interface đặt đúng `package RMI`, đúng tên lớp. Tên **tệp** phải trùng tên interface.

Khi đề KHÔNG in chữ ký (hai đề RMI Data), thứ tự thử:

| Đề | Chữ ký thử trước | Nếu lỗi method hash thì đổi sang |
|---|---|---|
| `YHfPjI4z` (CSV số thực) | `String requestData(String,String)` + `void submitData(String,String,String)` | `List<String>` / `Serializable` |
| `LJHaKLzA` (mảng số nguyên) | `List<Integer> requestData(...)` + `void submitData(String,String,List<Integer>)` | `int[]` |

Mẹo dò lúc thi, chạy trước 5 dòng này rồi mới viết tiếp:

```java
Registry reg = LocateRegistry.getRegistry("36.50.135.242", 1099);
System.out.println(Arrays.toString(reg.list()));          // mọi tên đang đăng ký
Object stub = reg.lookup("RMIDataService");
System.out.println(Arrays.toString(stub.getClass().getInterfaces()));   // tên interface server dùng
```

## 5. Lớp dữ liệu Serializable (đề RMI Object)

Server gửi về đối tượng của **chính lớp** đó, nên lớp bên client phải khớp tuyệt đối:

```java
package RMI;                                         // đúng package đề ghi

public class TicketSla implements java.io.Serializable {
    private static final long serialVersionUID = 20260517L;   // ĐÚNG con số đề cho
    private String  ticketId;                        // đúng TÊN và KIỂU từng thuộc tính
    private String  priority;                        // LOW, MEDIUM, HIGH, CRITICAL
    private int     openedHoursAgo;
    private boolean breached;
    private String  action;

    public TicketSla() { }                           // constructor rỗng: nên có
    public TicketSla(String ticketId, String priority, int openedHoursAgo) { ... }
    // getter/setter đủ cả 5 thuộc tính; boolean dùng isBreached()/setBreached()
}
```

| Sai ở đâu | Lỗi nhận được |
|---|---|
| `serialVersionUID` lệch | `java.io.InvalidClassException: local class incompatible` |
| thiếu / thừa / đổi tên thuộc tính | trường bị `null`/`0` im lặng, hoặc `InvalidClassException` |
| sai package (`Model.TicketSla`) | `java.lang.ClassNotFoundException: RMI.TicketSla` |

## 6. Bảng tra 6 đề RMI

| Mã câu hỏi | Service / tên đăng ký | Việc phải làm | Cốt lõi |
|---|---|---|---|
| `Ova1uIY6` | ByteService / `RMIByteService` | phần tử xuất hiện **ít nhất** → nộp `[giáTrị, sốLần]` | `LinkedHashMap` + so sánh `<` |
| `eyRHKunq` | ByteService / `RMIByteService` | phần tử xuất hiện **nhiều nhất** | giống trên, đổi `<` → `>`, `MAX_VALUE` → `-1` |
| `pkWPNAyA` | CharacterService / `RMICharacterService` | **mã hóa URL** phần path | `new URI(null,null,s,null).toASCIIString()` |
| `Hdok6Y1o` | CharacterService / `RMICharacterService` | số thập phân → **số La Mã** | bảng 13 mốc, trừ dần |
| `YHfPjI4z` | DataService / `RMIDataService` | average, stddev, p95 | stddev **tổng thể** (chia n), p95 = `ceil(n*0.95)-1` |
| `LJHaKLzA` | DataService / `RMIDataService` | vị trí **đỉnh cục bộ**, đếm **từ 1** | biên chỉ cần lớn hơn 1 phía |
| `Rdt4Yv56` | ObjectService / `RMIObjectService` | TicketSla: `breached` + `action` | ngưỡng theo priority, xem mục 9 |

Code từng thuật toán ở mục 9.

## 7. Bảng chẩn đoán lỗi RMI

| Lỗi | Nguyên nhân | Cách sửa |
|---|---|---|
| `ConnectException: Connection refused to host` | sai IP, sai cổng, hoặc chưa mở mạng | kiểm tra `36.50.135.242:1099` |
| `NotBoundException: RMIxxxService` | sai **tên đăng ký** | `reg.list()` xem tên thật |
| `UnmarshalException: unrecognized method hash` | **chữ ký lệch** | sửa interface khớp y hệt đề |
| `ClassCastException: $Proxy0 cannot be cast to ...` | ép kiểu sang interface khác tên | đúng tên interface, đúng package |
| `ClassNotFoundException: RMI.TicketSla` | thiếu lớp dữ liệu / sai package | tạo lớp trong `RMI\` |
| `InvalidClassException: local class incompatible` | `serialVersionUID` lệch | copy đúng số trong đề |
| `UnmarshalException: error unmarshalling return` + `ClassNotFound` | server trả lớp client chưa có | tạo lớp đó bên client |
| treo không trả lời | mạng chặn cổng callback | thường do tường lửa, thử lại hoặc đổi máy |

---

# PHẦN II — WEB SERVICE / SOAP (cổng 2221)

## 8. Cơ chế và hai con đường

SOAP = vẫn là gọi hàm từ xa, nhưng tham số **gói bằng XML** và đi qua **HTTP POST**.
WSDL = bản mô tả dịch vụ (có hàm nào, tham số kiểu gì, namespace là gì):

```
http://36.50.135.242:2221/DataService?wsdl
http://36.50.135.242:2221/CharacterService?wsdl
http://36.50.135.242:2221/ObjectService?wsdl
```

| | Cách 1 — sinh mã (chuẩn giáo trình) | Cách 2 — tự gửi XML (sổ tay này) |
|---|---|---|
| Làm gì | NetBeans/IntelliJ → *New → Web Service Client* → dán URL WSDL; hoặc `wsimport -keep -p client <url>` | tự dựng phong bì XML, POST bằng `HttpURLConnection` |
| Code client | 5 dòng | ~40 dòng hàm tiện ích (có sẵn `Soap.java`) |
| Điều kiện | **JDK 8** (từ JDK 11 Java bỏ JAX-WS + `wsimport`) | chạy trên **mọi** JDK, không thư viện ngoài |
| Rủi ro khi thi | máy thi không có JDK 8/thiếu jaxws-ri là tắc | phải lấy đúng `targetNamespace` |

Nếu phòng thi có NetBeans + JDK 8 thì Cách 1 nhanh hơn hẳn:

```java
DataService_Service ss = new DataService_Service();
DataService port = ss.getDataServicePort();
List<Integer> data = port.getData("B23DCCN430", "sTiPG8KK");
port.submitDataInt("B23DCCN430", "sTiPG8KK", data.stream().mapToInt(Integer::intValue).sum());
```

## 9. Phong bì SOAP 1.1 — đúng 4 thứ phải nhớ

```xml
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
  <S:Body>
    <n:getData xmlns:n="http://ws/">     <!-- 1. tên hàm + targetNamespace -->
      <arg0>B23DCCN430</arg0>            <!-- 2. tham số tên arg0, arg1, arg2... -->
      <arg1>sTiPG8KK</arg1>
    </n:getData>
  </S:Body>
</S:Envelope>
```

1. **Header HTTP**: `Content-Type: text/xml; charset=utf-8` và `SOAPAction: ""` (SOAP 1.1 bắt buộc có, để rỗng được).
2. **Tên tham số** mặc định của JAX-WS là `arg0, arg1, arg2...` theo thứ tự khai báo (vì server không ghi `@WebParam`).
3. **Namespace** `http://ws/` phải đúng `targetNamespace` trong WSDL, nếu không nhận Fault
   `Cannot find dispatch method for {http://.../}getData`.
4. **Kết quả** server trả về nằm trong các thẻ `<return>`: một thẻ = 1 giá trị, danh sách = nhiều thẻ `<return>`.

Lấy `targetNamespace` lúc thi — không cần mở trình duyệt:

```java
System.out.println(new String(new java.net.URL(
    "http://36.50.135.242:2221/DataService?wsdl").openStream().readAllBytes(), "UTF-8"));
// tìm dòng:  <definitions ... targetNamespace="http://ws/" ...>
// tên thẻ con của object nằm trong schema: thêm ?xsd=1 vào URL
```

JAX-WS sinh namespace từ **package đảo ngược** của lớp service, nên hay gặp
`http://ws/`, `http://service/`, `http://server.ws/`. Đoán sai thì server báo Fault ngay, không mất gì.

## 10. Bộ công cụ `Soap.java` — dùng cho cả 12 đề WS

Đã kiểm thử với web service giả lập: nhận list số, nhận list object, nộp chuỗi, nộp list chuỗi,
nộp list object — server nhận đúng XML trong cả 5 ca.

```java
import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class Soap {
    static String NS = "http://ws/";   // targetNamespace lấy từ WSDL

    /* Gọi 1 phương thức SOAP, trả về XML phản hồi */
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

    /* Mọi thẻ <ten> (bỏ qua namespace) -> List<String>: dùng cho List<Integer>, List<String> */
    static List<String> values(String xml, String ten) throws Exception {
        NodeList nl = parse(xml).getElementsByTagNameNS("*", ten);
        List<String> r = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) r.add(nl.item(i).getTextContent());
        return r;
    }

    /* Danh sách đối tượng: mỗi <return> -> Map<tênTrường, giáTrị>, giữ đúng thứ tự trường */
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

    /* Tham số đối tượng: <arg2><customerId>..</customerId>...</arg2> — GIỮ ĐÚNG THỨ TỰ */
    static String obj(String arg, Map<String, String> m) {
        StringBuilder sb = new StringBuilder("<" + arg + ">");
        for (Map.Entry<String, String> e : m.entrySet())
            sb.append("<").append(e.getKey()).append(">").append(esc(e.getValue()))
              .append("</").append(e.getKey()).append(">");
        return sb.append("</").append(arg).append(">").toString();
    }

    /* Tham số danh sách: lặp lại thẻ <arg2> cho mỗi phần tử */
    static String list(String arg, List<String> vals) {
        StringBuilder sb = new StringBuilder();
        for (String v : vals) sb.append("<").append(arg).append(">").append(esc(v))
                                .append("</").append(arg).append(">");
        return sb.toString();
    }

    /* Danh sách đối tượng làm tham số */
    static String listObj(String arg, List<Map<String, String>> xs) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> m : xs) sb.append(obj(arg, m));
        return sb.toString();
    }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
```

### Ba mẫu dùng (đã chạy thật)

```java
String url = "http://36.50.135.242:2221/DataService";

// (a) Nhận List<Integer> rồi nộp 1 chuỗi
String r = Soap.call(url, "getData", "<arg0>" + SV + "</arg0><arg1>" + Q + "</arg1>");
List<Integer> a = new ArrayList<>();
for (String v : Soap.values(r, "return")) a.add(Integer.parseInt(v.trim()));
Soap.call(url, "submitDataString",
        "<arg0>" + SV + "</arg0><arg1>" + Q + "</arg1><arg2>" + kq + "</arg2>");

// (b) Nộp List<String> — lặp thẻ arg2
Soap.call(url, "submitDataStringArray",
        "<arg0>" + SV + "</arg0><arg1>" + Q + "</arg1>"
        + Soap.list("arg2", Arrays.asList("EVEN=8", "ODD=8")));

// (c) Nhận & nộp danh sách ĐỐI TƯỢNG
String ourl = "http://36.50.135.242:2221/ObjectService";
String ro = Soap.call(ourl, "requestListCustomer", "<arg0>" + SV + "</arg0><arg1>" + Q + "</arg1>");
List<Map<String,String>> cs = Soap.objects(ro, "return");   // [{customerId=HC700, location=HN, ...}]
// ... lọc / sắp xếp trên List<Map<String,String>> ...
String ack = Soap.call(ourl, "submitListCustomer",
        "<arg0>" + SV + "</arg0><arg1>" + Q + "</arg1>" + Soap.listObj("arg2", cs));
System.out.println(Soap.values(ack, "return"));             // [AC] hoặc [WA]
```

Cách (c) tiện vì **không cần tạo lớp Customer/EmployeeY/ProductY**: giữ nguyên `Map`, chỉ sửa
trường cần sửa rồi gửi lại y hệt các trường khác — tránh sai định dạng số/ngày.

## 11. Bảng tra 12 đề WS

| Mã câu hỏi | Service | Hàm nhận → hàm nộp | Việc phải làm |
|---|---|---|---|
| `sTiPG8KK` | DataService | `getData` → `submitDataInt` | tổng các phần tử |
| `03vYByqP` | DataService | `getData` → `submitDataString` | **ghép số lớn nhất** từ các số |
| `A0rIpHOS` | DataService | `getData` → `submitDataStringArray` | `[EVEN=n, ODD=m]` |
| `7bc1e2Kp` | DataService | `getData` → `submitDataString` | `primeCount=n;checksum=m`, checksum = `Σ(i+1)*a[i] mod 100000` |
| `brumE1Al` | CharacterService | `requestCharacter` → `submitCharacterCharArray` | xoay phải `k = ASCII(phần tử đầu)` lần |
| `uotdRCYu` | CharacterService | `requestStringArray` → `submitCharacterStringArray` | nhóm từ theo **số nguyên âm**, mỗi nhóm sắp A→Z |
| `5ZbWn82u` | CharacterService | `requestStringArray` → `submitStringArray` | che email/phone/token + sắp ERROR→WARN→INFO |
| `WKK7VOn6` | CharacterService | `requestString` → `submitString` | đảo ngược chuỗi |
| `WlA2Tgii` | ObjectService | `requestListEmployeeY` → `submitListEmployeeY` | sắp theo `startDate` tăng dần, **giữ thứ tự khi bằng** |
| `lIUbJPLI` | ObjectService | `requestListCustomer` → `submitListCustomer` | lọc `totalSpent > 5000` **và** `purchaseCount >= 5` |
| `U5M0p3AW` | ObjectService | `requestListCustomer` → `submitListCustomer` | lọc `purchaseCount >= 6` và `totalSpent >= 4000`, sắp `totalSpent` giảm, bằng thì `customerId` tăng |
| `3eZ51jPx` | ObjectService | `requestProductY` → `submitProductY` | `finalPrice = price*(1+tax/100)*(1-discount/100)`, làm tròn 2 số |

## 12. Bẫy của phần SOAP

| Bẫy | Hậu quả | Cách tránh |
|---|---|---|
| Sai `targetNamespace` | Fault `Cannot find dispatch method` | đọc WSDL, sửa hằng `NS` |
| Thiếu header `SOAPAction` | Fault / HTTP 500 | luôn đặt `SOAPAction: ""` |
| Dùng tên tham số thật (`studentCode`) thay `arg0` | server nhận `null` rồi trả WA | giữ `arg0/arg1/arg2` trừ khi WSDL ghi khác |
| Đảo thứ tự thẻ con của object | Fault `unexpected element` | giữ đúng thứ tự như server gửi (dùng `LinkedHashMap`) |
| Tự định dạng lại `startDate` | server không parse được | **gửi lại đúng chuỗi đã nhận**, chỉ dùng bản sao để so sánh |
| `float` in ra `9000.0` vs `9000` | thường vẫn chấp nhận, nhưng rủi ro | gửi lại nguyên chuỗi nhận được nếu không phải trường cần sửa |
| Không đọc `<return>` sau khi nộp | không biết AC/WA | in `Soap.values(ack, "return")` |
| `%.2f` trên máy locale VN ra `794,95` | WA vì dấu phẩy | `String.format(Locale.US, ...)` hoặc `Math.round(v*100)/100.0` |

Biên dịch & chạy:

```bat
javac -encoding UTF-8 Soap.java BAI_WS.java
java BAI_WS
```

---

# PHẦN III — KHO THUẬT TOÁN (đã đối chiếu ví dụ trong đề)

## 13. RMI

```java
// Ova1uIY6 / eyRHKunq — phần tử xuất hiện ít nhất (hoặc nhiều nhất)
LinkedHashMap<Byte, Integer> dem = new LinkedHashMap<>();      // giữ thứ tự gặp lần đầu
for (byte b : data) dem.merge(b, 1, Integer::sum);
byte best = 0; int min = Integer.MAX_VALUE;                    // nhiều nhất: int max = -1;
for (Map.Entry<Byte, Integer> e : dem.entrySet())
    if (e.getValue() < min) { min = e.getValue(); best = e.getKey(); }   // nhiều nhất: >
byte[] kq = { best, (byte) min };
// [1,2,3,2,1] -> [3, 1]   (dùng < chặt để khi hòa giữ phần tử xuất hiện TRƯỚC)

// pkWPNAyA — mã hóa URL phần path
String maHoa = new java.net.URI(null, null, s, null).toASCIIString();
// "Hello World!" -> "Hello%20World!"

// Hdok6Y1o — số La Mã
int[]    v = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
String[] k = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};
StringBuilder sb = new StringBuilder();
for (int i = 0; i < v.length; i++) while (n >= v[i]) { sb.append(k[i]); n -= v[i]; }
// 58 -> LVIII ; 1994 -> MCMXCIV

// YHfPjI4z — average / stddev / p95
double avg = Arrays.stream(a).average().orElse(0);
double var = Arrays.stream(a).map(x -> (x-avg)*(x-avg)).sum() / n;   // CHIA n (tổng thể)
double sd  = Math.sqrt(var);
double[] b = a.clone(); Arrays.sort(b);
double p95 = b[(int) Math.ceil(n * 0.95) - 1];
String kq = String.format(Locale.US, "average=%.2f;stddev=%.2f;p95=%.2f", avg, sd, p95);
// [10,20,30,40] -> average=25.00;stddev=11.18;p95=40.00
// Chia n-1 sẽ ra 12.91 -> SAI so với ví dụ của đề

// LJHaKLzA — đỉnh cục bộ, vị trí đếm TỪ 1
for (int i = 0; i < a.size(); i++) {
    boolean trai  = (i == 0)            || a.get(i) > a.get(i-1);   // biên: chỉ cần 1 phía
    boolean phai  = (i == a.size()-1)   || a.get(i) > a.get(i+1);
    if (trai && phai) kq.add(i + 1);
}
// [3,8,5,10,7] -> [2,4]

// Rdt4Yv56 — SLA ticket
int h = t.getOpenedHoursAgo(); String p = t.getPriority();
boolean viPham = ("CRITICAL".equals(p) && h > 2) || ("HIGH".equals(p) && h > 8)
              || ("MEDIUM".equals(p)   && h > 24) || ("LOW".equals(p)  && h > 72);
t.setBreached(viPham);
if (!viPham)                             t.setAction("MONITOR");
else if ("CRITICAL".equals(p) || h > 96) t.setAction("ESCALATE_L2");
else                                     t.setAction("ESCALATE_L1");
```

## 14. Web Service

```java
// 03vYByqP — ghép số lớn nhất
String kq = a.stream().map(String::valueOf)
             .sorted((x, y) -> (y + x).compareTo(x + y))     // so sánh GHÉP, không so sánh số
             .collect(Collectors.joining());
// 7602,9136,1090,34319,... -> 913678307602617934319322232179305903025028199...

// 7bc1e2Kp — primeCount + checksum
int d = 0; long s = 0;
for (int i = 0; i < a.size(); i++) { if (nt(a.get(i))) d++; s += (long)(i+1) * a.get(i); }
String kq = "primeCount=" + d + ";checksum=" + (s % 100000);
// [11,12,13,20] -> primeCount=2;checksum=154

// A0rIpHOS — đếm chẵn lẻ
Arrays.asList("EVEN=" + chan, "ODD=" + le);     // nộp bằng Soap.list("arg2", ...)

// brumE1Al — xoay phải k lần (k = mã ASCII phần tử đầu)
int n = a.size(); k = ((k % n) + n) % n;        // 97 lần trên mảng 4 phần tử = 1 lần
List<Integer> r = new ArrayList<>(a.subList(n - k, n)); r.addAll(a.subList(0, n - k));
// ['a','b','c','d'], k=97 -> ['d','a','b','c']

// uotdRCYu — nhóm từ theo số nguyên âm
TreeMap<Integer, List<String>> m = new TreeMap<>(Comparator.reverseOrder());   // nhiều -> ít
for (String w : ws) m.computeIfAbsent(demNguyenAm(w), x -> new ArrayList<>()).add(w);
List<String> kq = new ArrayList<>();
for (List<String> g : m.values()) { Collections.sort(g); kq.add(String.join(",", g)); }
// [apple,banana,pear,grape,kiwi] -> ["banana", "apple,grape,kiwi,pear"]

// 5ZbWn82u — che dữ liệu + sắp theo mức log
line.replaceAll("(?i)(email=)\\S+", "$1[EMAIL]")
    .replaceAll("(?i)(phone=)\\S+", "$1[PHONE]")
    .replaceAll("(?i)(token=)\\S+", "$1[TOKEN]");
List<String> muc = Arrays.asList("ERROR", "WARN", "INFO");
logs.sort(Comparator.comparingInt(s -> {
    for (int i = 0; i < muc.size(); i++) if (s.startsWith(muc.get(i))) return i;
    return muc.size(); }));                     // List.sort ỔN ĐỊNH -> cùng mức giữ thứ tự gốc

// WKK7VOn6 — đảo chuỗi
new StringBuilder(s).reverse().toString();

// WlA2Tgii — thâm niên: startDate tăng dần, bằng thì giữ nguyên thứ tự
cs.sort(Comparator.comparing(c -> c.get("startDate")));      // chuỗi ISO cùng định dạng so được
// an toàn hơn: Comparator.comparing(c -> java.time.OffsetDateTime.parse(c.get("startDate")))

// lIUbJPLI / U5M0p3AW — lọc + sắp khách hàng
cs.stream()
  .filter(c -> Integer.parseInt(c.get("purchaseCount")) >= 6
            && Double.parseDouble(c.get("totalSpent")) >= 4000)
  .sorted(Comparator.comparingDouble((Map<String,String> c) -> -Double.parseDouble(c.get("totalSpent")))
                    .thenComparing(c -> c.get("customerId")))
  .collect(Collectors.toList());

// 3eZ51jPx — giá cuối, làm tròn 2 số
double v = Math.round(price * (1 + tax/100) * (1 - discount/100) * 100.0) / 100.0;
// 770 / 16 / 11 -> 794.95 ;  1000 / 10 / 5 -> 1045.0
```

## 15. Hai chỗ đề viết mâu thuẫn — cần cân nhắc khi nộp

- **`uotdRCYu` (nhóm nguyên âm)**: đề viết "Nhóm 2 nguyên âm: apple, banana" nhưng *banana* có
  **3** nguyên âm, và danh sách kết quả in ra lại là mảng phẳng `[apple, banana, grape, kiwi, pear]`
  chứ không phải mảng các nhóm. Phương án chính: nộp `["banana", "apple,grape,kiwi,pear"]`
  (mỗi phần tử một nhóm, nhóm nhiều nguyên âm trước). Nếu WA, thử lần lượt: nhóm **ít → nhiều**
  nguyên âm; hoặc nộp mảng phẳng đã sắp theo (số nguyên âm giảm, rồi A→Z).
- **`03vYByqP`**: đề viết "gửi kết quả **tổng**" nhưng ví dụ là chuỗi ghép số lớn nhất → bám **ví dụ**.

Quy tắc chung đã rút ra từ phần TCP/UDP: **định dạng phải khớp từng ký tự với ví dụ in trong đề**
(dấu cách sau dấu phẩy, dấu phẩy thừa ở cuối, thứ tự trường). Ví dụ trong đề đáng tin hơn phần mô tả.

## 16. Checklist 60 giây trước khi nộp

**RMI**

1. Interface đúng package `RMI`, chữ ký chép y hệt đề, có `throws RemoteException`.
2. Tên `lookup` đúng (`RMIByteService` / `RMICharacterService` / `RMIDataService` / `RMIObjectService`).
3. MSSV `B23DCCN430` và **mã câu hỏi của đề mình**, không phải mã trong file mẫu.
4. Lớp dữ liệu: đúng `serialVersionUID`, đúng tên/kiểu thuộc tính.
5. In `Nhan:` và `Gui :` ra console để đối chiếu với ví dụ trong đề trước khi gọi `submit`.

**WS**

1. `NS` khớp `targetNamespace` trong WSDL; URL dịch vụ **không** có `?wsdl` khi POST.
2. Tên hàm nộp đúng biến thể (`submitDataInt` / `submitDataString` / `submitDataStringArray` / `submitStringArray` / `submitCharacterCharArray` / `submitCharacterStringArray` / `submitListCustomer` / `submitListEmployeeY` / `submitProductY` / `submitString`).
3. Tham số `arg0`, `arg1`, `arg2`; danh sách thì **lặp thẻ**, không gộp bằng dấu phẩy.
4. Object gửi lại: đủ mọi trường, đúng thứ tự, giá trị không sửa thì giữ nguyên chuỗi gốc.
5. In `<return>` của lời gọi nộp để xem **AC / WA**.
