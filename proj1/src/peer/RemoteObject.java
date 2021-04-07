package peer;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObject extends Remote {
    String backup(File file,int repDegree) throws IOException, InterruptedException;
    String restore(File file) throws IOException, InterruptedException;
    String delete(File file) throws IOException, InterruptedException;
    String state() throws RemoteException;
    String reclaim(double maxDiskSpace) throws IOException;
}
