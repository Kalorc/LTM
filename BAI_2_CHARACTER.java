import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import RMI.CharacterService;

public class BAI_2_CHARACTER {
    public static void main(String[] args) throws Exception {
        String sv = "B23DCCN430";
        String q = "pkWPNAyA";

        Registry reg = LocateRegistry.getRegistry("36.50.135.242", 1099);
        CharacterService s = (CharacterService) reg.lookup("RMICharacterService");

        String xau = s.requestCharacter(sv, q);
        String outp = maHoaURL(xau);

        s.submitCharacter(sv, q, outp);

    }

    static String maHoaURL(String xaua) {
        String giuNguyen = "-_.~!*'();:@&=+$,/";
        String kq = "";
        for (byte x : xaua.getBytes(StandardCharsets.UTF_8)) {
            int v = x & 0xFF;
            char c = (char) x;
            if (giuNguyen.indexOf(c) >= 0 || (v < 128 && Character.isLetterOrDigit(c))) {

            }
        }

        return kq;
    }

}