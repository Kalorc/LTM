// YHfPjI4z - RMI Data - registry 1099 - average, stddev, p95 tu chuoi CSV so thuc
import java.rmi.registry.*;   // LocateRegistry, Registry
import java.util.*;           // Arrays, Locale
import RMI.DataService;       // Interface tu xa trong package RMI (dung chung voi LJHaKLzA)

public class BAI_5 {
    public static void main(String[] args) throws Exception {
        String sv = "B23DCCN430";   // ma sinh vien
        String q  = "YHfPjI4z";     // ma cau hoi tren de

        // === BUOC 1: KET NOI DANH BA VA LAY STUB ===
        Registry reg = LocateRegistry.getRegistry("36.50.135.242", 1099);
        DataService s = (DataService) reg.lookup("RMIDataService");

        // === BUOC 2: NHAN CHUOI CSV (yeu cau b) ===
        Object o = s.requestData(sv, q);
        System.out.println("Nhan (" + o.getClass().getSimpleName() + "): " + o);

        // Bo dau ngoac va khoang trang neu co, roi tach theo dau phay
        String[] p = String.valueOf(o).replaceAll("[\\[\\]\\s]", "").split(",");
        double[] a = new double[p.length];
        for (int i = 0; i < p.length; i++) a[i] = Double.parseDouble(p[i]);
        int n = a.length;

        // === BUOC 3: TINH TOAN (yeu cau c) ===
        // Trung binh cong
        double tong = 0;
        for (double v : a) tong += v;
        double avg = tong / n;

        // Do lech chuan TONG THE: chia cho n (KHONG phai n-1).
        // Kiem chung bang vi du cua de: [10,20,30,40] -> chia n ra 11.18 (dung),
        // chia n-1 ra 12.91 (sai). Do la cho de mat diem nhat cua bai nay.
        double tongBinhPhuong = 0;
        for (double v : a) tongBinhPhuong += (v - avg) * (v - avg);
        double sd = Math.sqrt(tongBinhPhuong / n);

        // Phan vi 95: sap xep tang dan roi lay phan tu o vi tri ceil(n * 0.95) - 1
        double[] b = a.clone();     // clone de khong lam xao tron mang goc
        Arrays.sort(b);
        int vt = (int) Math.ceil(n * 0.95) - 1;
        double p95 = b[vt];

        // === BUOC 4: NOP (yeu cau d) ===
        // Locale.US BAT BUOC: may dat ngon ngu Viet se in "25,00" (dau phay) -> Wrong Answer
        String kq = String.format(Locale.US, "average=%.2f;stddev=%.2f;p95=%.2f", avg, sd, p95);
        System.out.println("Gui : " + kq);
        s.submitData(sv, q, kq);

        // === BUOC 5: KET THUC (yeu cau e) ===
    }
}
