import java.io.*;
import java.rmi.RemoteException;
import java.util.HashMap;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

//java Server <remote_object_name>
public class Server implements RemoteObject {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <remote_object_name>");
            return;
        }

        String remoteObjName = args[0];
        try {
            Server obj = new Server();
            RemoteObject stub = (RemoteObject) UnicastRemoteObject.exportObject(obj, 0);
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remoteObjName, stub);
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

    }

    @Override
    public String backup() throws RemoteException {
        System.out.println("Backup");
        return null;
    }

    @Override
    public String restore() throws RemoteException {
        System.out.println("Restore");
        return null;
    }

    @Override
    public String delete() throws RemoteException {
        System.out.println("Delete");
        return null;
    }

    @Override
    public String state() throws RemoteException {
        System.out.println("State");
        return null;
    }
    @Override
    public String reclaim() throws RemoteException {
        System.out.println("Reclaim");
        return null;
    }

}