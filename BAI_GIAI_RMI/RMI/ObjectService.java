package RMI;   // de ghi: tao interface trong package RMI cua project client

// Chep Y HET chu ky de in ra, ke ca kieu java.io.Serializable:
// lech kieu la server tra "unrecognized method hash".
public interface ObjectService extends java.rmi.Remote {
    java.io.Serializable requestObject(String studentCode, String qCode) throws java.rmi.RemoteException;
    void submitObject(String studentCode, String qCode, java.io.Serializable object) throws java.rmi.RemoteException;
}
