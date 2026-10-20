package RMI;   // de ghi: giao dien DataService trong package RMI

import java.rmi.Remote;
import java.rmi.RemoteException;

// Chep Y HET chu ky de in ra (kieu Object, khong doi thanh int[] hay List):
// RMI bam ten + kieu tham so + KIEU TRA VE thanh method hash, lech la loi
// "unrecognized method hash: method not supported by remote object".
public interface DataService extends Remote {
    public Object requestData(String studentCode, String qCode) throws RemoteException;
    public void submitData(String studentCode, String qCode, Object data) throws RemoteException;
}
