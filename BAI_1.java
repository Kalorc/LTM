
// Ova1uIY6 - RMI Byte - registry 1099 - tim phan tu xuat hien IT nhat
import java.rmi.registry.*; // LocateRegistry, Registry: tra danh ba doi tuong tu xa
import java.util.*; // LinkedHashMap, Map, Arrays
import RMI.ByteService; // Interface tu xa tu dat trong package RMI

public class BAI_1 {
    public static void main(String[] args) throws Exception {
        String sv = "B23DCCN430"; // ma sinh vien
        String q = "eyRHKunq"; // ma cau hoi in tren de

        // === BUOC 1: KET NOI DANH BA VA LAY STUB ===
        // Danh ba (RMI Registry) cua server, 1099 la cong mac dinh cua RMI
        Registry reg = LocateRegistry.getRegistry("36.50.135.242", 1099);
        // lookup = xin doi tuong dang ky duoi ten "RMIByteService" (ten do de quy dinh)
        // Tra ve Object nen phai ep kieu ve ByteService
        ByteService s = (ByteService) reg.lookup("RMIByteService");

        // === BUOC 2: GOI requestData DE NHAN MANG BYTE (yeu cau a) ===
        // Goi nhu ham binh thuong, Java tu dong goi tham so va truyen qua mang
        byte[] data = s.requestData(sv, q);
        System.out.println("Nhan: " + Arrays.toString(data));

        // === BUOC 3: XU LY - DEM TAN SUAT (yeu cau b) ===
        // LinkedHashMap giu dung THU TU GAP LAN DAU -> hoa thi lay phan tu xuat hien
        // truoc
        LinkedHashMap<Byte, Integer> dem = new LinkedHashMap<>();
        // merge(b, 1, Integer::sum): chua co thi dat 1, da co thi cong them 1
        for (byte b : data)
            dem.merge(b, 1, Integer::sum);

        byte best = 0; // phan tu xuat hien it nhat
        int min = Integer.MIN_VALUE; // so lan it nhat, khoi tao lon nhat roi ha dan
        // Duyet map theo thu tu gap lan dau; dung dau < (KHONG dung <=)
        // de khi bang nhau thi giu phan tu gap truoc - dung yeu cau cua de
        for (Map.Entry<Byte, Integer> e : dem.entrySet())
            if (e.getValue() > min) {
                min = e.getValue();
                best = e.getKey();
            }
        // Neu de hoi phan tu xuat hien NHIEU nhat: doi < thanh > va MAX_VALUE thanh -1

        // === BUOC 4: GOI submitData DE GUI KET QUA (yeu cau c) ===
        // De yeu cau mang gom [phan tu, so lan xuat hien]; (byte) min vi min dang la
        // int
        byte[] kq = { best, (byte) min };
        System.out.println("Gui : " + Arrays.toString(kq));
        s.submitData(sv, q, kq);

        // === BUOC 5: KET THUC (yeu cau d) ===
        // RMI khong co socket phai dong - goi xong la xong chuong trinh
    }
}
