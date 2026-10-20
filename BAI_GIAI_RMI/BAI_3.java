// Hdok6Y1o - RMI Character - registry 1099 - doi so thap phan sang so La Ma
import java.rmi.registry.*;   // LocateRegistry, Registry: tra danh ba doi tuong tu xa
import RMI.CharacterService;  // Interface tu xa tu dat trong package RMI (dung chung voi pkWPNAyA)

public class BAI_3 {
    public static void main(String[] args) throws Exception {
        String sv = "B23DCCN430";   // ma sinh vien
        String q  = "Hdok6Y1o";     // ma cau hoi tren de

        // === BUOC 1: KET NOI DANH BA VA LAY STUB ===
        Registry reg = LocateRegistry.getRegistry("36.50.135.242", 1099);
        CharacterService s = (CharacterService) reg.lookup("RMICharacterService");

        // === BUOC 2: NHAN CHUOI (yeu cau a) ===
        String str = s.requestCharacter(sv, q);
        System.out.println("Nhan: [" + str + "]");

        // === BUOC 3: DOI SANG SO LA MA (yeu cau b) ===
        // replaceAll("[^0-9]", ""): bo moi ky tu khong phai chu so (dau cach, chu, xuong dong)
        // -> an toan du server gui "58", " 58 " hay "So thap phan dau vao: 58"
        int n = Integer.parseInt(str.replaceAll("[^0-9]", ""));
        String kq = laMa(n);
        System.out.println("Gui : [" + n + " -> " + kq + "]");

        // === BUOC 4: NOP (yeu cau c) ===
        s.submitCharacter(sv, q, kq);

        // === BUOC 5: KET THUC (yeu cau d) - RMI khong co socket phai dong ===
    }

    /* Doi so nguyen sang so La Ma.
       Meo: xep 13 moc tu LON den BE, trong do co ca 4 moc "tru" (CM=900, CD=400, XC=90, IX=9,
       va XL=40, IV=4). Nho 4 moc nay moi viet duoc 4=IV, 9=IX, 40=XL, 90=XC, 400=CD, 900=CM
       ma khong can xu ly rieng truong hop nao.
       Cach chay: lay moc lon nhat con <= n, tru dan, moi lan tru thi noi them ky hieu. */
    static String laMa(int n) {
        int[]    v = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] k = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v.length; i++)
            while (n >= v[i]) {       // while chu khong phai if: 3000 can ba chu M
                sb.append(k[i]);
                n -= v[i];
            }
        return sb.toString();
    }
}
