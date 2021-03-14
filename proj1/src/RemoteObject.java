import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObject extends Remote {
    String processRequest(String request) throws RemoteException;
}
