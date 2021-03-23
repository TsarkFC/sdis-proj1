package peer;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObject extends Remote {
    String backup(File file,int repDegree) throws RemoteException;
    String restore(File file) throws RemoteException;
    String delete(File file) throws RemoteException;
    String state(File file) throws RemoteException;
    String reclaim(File file) throws RemoteException;

}
