import messages.PutChunk;
import utils.FileHandler;

import java.io.*;
import java.rmi.RemoteException;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

//java Server <remote_object_name>
public class Peer implements RemoteObject {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <remote_object_name>");
            return;
        }

        String remoteObjName = args[0];
        try {
            Peer obj = new Peer();
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
    public String backup(File file,int repDegree) throws RemoteException {

        FileHandler fileHandler = new FileHandler(file);
        List<byte[]> chunks = fileHandler.splitFile();
        for (int i = 0; i < chunks.size(); i++) {
            String fileId = fileHandler.createFileId();
            int chunkNo = i;
            PutChunk backupMsg = new PutChunk(1.0,0,fileId,chunkNo,repDegree,chunks.get(i));
            for (int j = 0; j < repDegree; j++) {
                //send messages
            }

        }



        //Send message


        return "";
    }

    @Override
    public String restore(File file) throws RemoteException {
        System.out.println("Restore");
        return null;
    }

    @Override
    public String delete(File file) throws RemoteException {
        System.out.println("Delete");
        return null;
    }

    @Override
    public String state(File file) throws RemoteException {
        System.out.println("State");
        return null;
    }
    @Override
    public String reclaim(File file) throws RemoteException {
        System.out.println("Reclaim");
        return null;
    }

}