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

    static String maHoaURL(String xaua){

        return xau
    }

}