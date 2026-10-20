// Rdt4Yv56 - RMI Object - registry 1099 - phan tang xu ly SLA ticket
import java.rmi.registry.*;   // LocateRegistry, Registry
import RMI.ObjectService;     // Interface tu xa trong package RMI
import RMI.TicketSla;         // Lop du lieu, phai khop y het voi server

public class BAI_6 {
    public static void main(String[] args) throws Exception {
        String sv = "B23DCCN430";   // ma sinh vien
        String q  = "Rdt4Yv56";     // ma cau hoi tren de

        // === BUOC 1: KET NOI DANH BA VA LAY STUB ===
        Registry reg = LocateRegistry.getRegistry("36.50.135.242", 1099);
        ObjectService s = (ObjectService) reg.lookup("RMIObjectService");

        // === BUOC 2: NHAN DOI TUONG (yeu cau b) ===
        // Kieu tra ve la Serializable nen phai ep kieu ve TicketSla
        TicketSla t = (TicketSla) s.requestObject(sv, q);
        System.out.println("Nhan: " + t);

        // === BUOC 3: XU LY (yeu cau c) ===
        int h = t.getOpenedHoursAgo();
        String p = t.getPriority();

        // Vi pham SLA: nguong gio khac nhau theo muc uu tien. Dung dau > (khong phai >=):
        // de ghi "CRITICAL: openedHoursAgo > 2" nen dung 2 gio la CHUA vi pham.
        boolean viPham =
               ("CRITICAL".equals(p) && h > 2)
            || ("HIGH".equals(p)     && h > 8)
            || ("MEDIUM".equals(p)   && h > 24)
            || ("LOW".equals(p)      && h > 72);
        // "CRITICAL".equals(p) chu khong phai p.equals("CRITICAL"):
        // viet nguoc lai se NullPointerException neu server gui priority = null

        t.setBreached(viPham);

        // Thu tu 3 nhanh phai dung: khong vi pham -> MONITOR;
        // vi pham + (CRITICAL hoac > 96 gio) -> L2; con lai -> L1
        if (!viPham)                                 t.setAction("MONITOR");
        else if ("CRITICAL".equals(p) || h > 96)     t.setAction("ESCALATE_L2");
        else                                         t.setAction("ESCALATE_L1");

        System.out.println("Gui : " + t);

        // === BUOC 4: NOP (yeu cau d) - gui lai CHINH doi tuong da sua ===
        // Khong tao doi tuong moi: giu nguyen ticketId va cac truong khac server gui xuong
        s.submitObject(sv, q, t);

        // === BUOC 5: KET THUC (yeu cau e) ===
    }
}
