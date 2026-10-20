import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import RMI.ByteService;

public class BAI_2 {
    public static void main(String[] args) throws Exception {
        String sv = "B23DCCN430";
        String q = "eyRHKunq";

        Registry reg = LocateRegistry.getRegistry("36.50.135.242", 1099);
        ByteService s = (ByteService) reg.lookup("RMIByteService");

        byte[] data = s.requestData(sv, q);

        LinkedHashMap<Byte, Integer> dem = new LinkedHashMap<>();
        for (byte x : data) {
            if (dem.containsKey(x))
                dem.put(x, dem.get(x) + 1);
            else
                dem.put(x, 1);
        }

        byte xhnn = 0;
        int slxh = Integer.MIN_VALUE;

        for (Map.Entry<Byte, Integer> b : dem.entrySet()) {
            if (b.getValue() > slxh) {
                slxh = b.getValue();
                xhnn = b.getKey();
            }
        }

        byte[] kq = { xhnn, (byte) slxh };
        s.submitData(sv, q, kq);
    }
}