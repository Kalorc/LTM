
// LJHaKLzA - RMI Data - registry 1099 - tim vi tri dinh cuc bo (dem tu 1)
import java.rmi.registry.*; // LocateRegistry, Registry
import java.util.*; // List, ArrayList
import RMI.DataService; // Interface tu xa trong package RMI

public class BAI_4 {
    public static void main(String[] args) throws Exception {
        String sv = "B23DCCN430"; // ma sinh vien
        String q = "LJHaKLzA"; // ma cau hoi tren de

        // === BUOC 1: KET NOI DANH BA VA LAY STUB ===
        Registry reg = LocateRegistry.getRegistry("36.50.135.242", 1099);
        DataService s = (DataService) reg.lookup("RMIDataService");

        // === BUOC 2: NHAN DU LIEU (yeu cau b) ===
        // Kieu tra ve la Object nen chua biet server goi int[], Integer[] hay List
        Object o = s.requestData(sv, q);
        List<Integer> a = doiSangList(o);
        System.out.println("Nhan (" + o.getClass().getSimpleName() + "): " + a);

        // === BUOC 3: TIM DINH CUC BO (yeu cau c) ===
        List<Integer> kq = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            // Phan tu dau khong co ben trai -> coi nhu thoa dieu kien ben trai (chi can lon
            // hon ben phai)
            boolean trai = (i == 0) || a.get(i) > a.get(i - 1);
            // Phan tu cuoi khong co ben phai -> coi nhu thoa dieu kien ben phai
            boolean phai = (i == a.size() - 1) || a.get(i) > a.get(i + 1);
            if (trai && phai)
                kq.add(i + 1); // + 1 vi de yeu cau dem TU 1
        }
        System.out.println("Gui : " + kq);

        // === BUOC 4: NOP (yeu cau d) - gui List<Integer> ===
        s.submitData(sv, q, kq);

        // === BUOC 5: KET THUC (yeu cau e) ===
    }

    /*
     * Doi Object nhan duoc ve List<Integer>, chap nhan moi kieu server co the gui
     */
    @SuppressWarnings("unchecked")
    static List<Integer> doiSangList(Object o) {
        List<Integer> r = new ArrayList<>();
        if (o instanceof int[]) { // mang so nguyen nguyen thuy
            for (int v : (int[]) o)
                r.add(v);
        } else if (o instanceof Object[]) { // Integer[] hoac String[]
            for (Object v : (Object[]) o)
                r.add(Integer.parseInt(String.valueOf(v).trim()));
        } else if (o instanceof List) { // List<Integer>
            for (Object v : (List<Object>) o)
                r.add(Integer.parseInt(String.valueOf(v).trim()));
        } else { // chuoi CSV "3,8,5,10,7" hoac "[3, 8, 5]"
            for (String p : String.valueOf(o).replaceAll("[\\[\\]\\s]", "").split(","))
                if (!p.isEmpty())
                    r.add(Integer.parseInt(p));
        }
        return r;
    }
}
