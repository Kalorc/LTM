
// pkWPNAyA - RMI Character - registry 1099 - ma hoa URL theo quy tac phan path cua URI
import java.rmi.registry.*; // LocateRegistry, Registry: tra danh ba doi tuong tu xa
import java.nio.charset.StandardCharsets; // doi chuoi ra byte UTF-8 truoc khi ma hoa
import RMI.CharacterService; // Interface tu xa tu dat trong package RMI

public class BAI_2cha {
    public static void main(String[] args) throws Exception {
        String sv = "B23DCCN430"; // ma sinh vien
        String q = "pkWPNAyA"; // ma cau hoi tren de

        // === BUOC 1: KET NOI DANH BA VA LAY STUB ===
        Registry reg = LocateRegistry.getRegistry("36.50.135.242", 1099);
        // Ten "RMICharacterService" do de quy dinh; lookup tra Object nen phai ep kieu
        CharacterService s = (CharacterService) reg.lookup("RMICharacterService");

        // === BUOC 2: NHAN CHUOI (yeu cau a) ===
        String str = s.requestCharacter(sv, q);
        System.out.println("Nhan: [" + str + "]"); // dong ngoac de nhin ro dau cach dau/cuoi

        // === BUOC 3: MA HOA URL (yeu cau b) ===
        String maHoa = maHoaUrl(str);
        System.out.println("Gui : [" + maHoa + "]");

        // === BUOC 4: NOP (yeu cau c) ===
        s.submitCharacter(sv, q, maHoa);

        // === BUOC 5: KET THUC (yeu cau d) - RMI khong co socket phai dong ===
    }

    /*
     * Ma hoa theo dung luat de in ra (phan path cua URI):
     * GIU NGUYEN: chu cai, chu so va - _ . ~ ! * ' ( ) ; : @ & = + $ , /
     * THAY BANG %XX: dau cach, % " < > [ ] { } | \ ^ ? # va moi ky tu ngoai ASCII
     * (theo UTF-8)
     */
    static String maHoaUrl(String s) {
        String giuNguyen = "-_.~!*'();:@&=+$,/";
        StringBuilder sb = new StringBuilder();
        // Lay byte UTF-8: ky tu tieng Viet 1 chu cho ra 2-3 byte, moi byte thanh mot
        // %XX
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            int v = b & 0xFF; // doi byte co dau (-128..127) sang 0..255
            char c = (char) v;
            if ((v < 128 && Character.isLetterOrDigit(c)) || giuNguyen.indexOf(c) >= 0)
                sb.append(c); // ky tu hop le -> giu nguyen
            else
                sb.append('%').append(String.format("%02X", v)); // %XX chu HOA, du 2 chu so
        }
        return sb.toString();
    }

    /*
     * CACH NGAN HON de bai goi y - CHI dung khi chac chuoi KHONG co dau ':' truoc
     * dau '/':
     * String maHoa = new java.net.URI(null, null, str, null).toASCIIString();
     * Co dau ':' (vi du "abc: xyz") thi no nem URISyntaxException "Illegal
     * character in
     * scheme name". Muon dung van cho an toan thi them '/' o dau roi cat di:
     * String maHoa = new java.net.URI(null, null, "/" + str,
     * null).toASCIIString().substring(1);
     * Hai cach cho ket qua trung nhau tren bo ky tu de neu: ~=+%^*&/()[]{}!@#$?|
     */
}
