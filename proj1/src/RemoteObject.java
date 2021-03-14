import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObject extends Remote {
    String backup() throws RemoteException;
    String restore() throws RemoteException;
    String delete() throws RemoteException;
    String state() throws RemoteException;
    String reclaim() throws RemoteException;

}
