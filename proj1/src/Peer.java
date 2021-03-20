import messages.PutChunk;
import utils.FileHandler;

import java.io.*;
import java.rmi.RemoteException;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//java Server <remote_object_name>
public class Peer implements RemoteObject {

    private final double protocol_version = 1.0;
    private final int peerId = 0;


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

        System.out.println("ZAS");
        //MC
        //MDB

        //MulticastMessage


        //MDB Processava

        /*FileHandler fileHandler = new FileHandler(file);
        List<byte[]> chunks = fileHandler.splitFile();
        String fileId = fileHandler.createFileId();
        for (int i = 0; i < chunks.size(); i++) {
            int chunkNo = i;
            PutChunk backupMsg = new PutChunk(1.0,0,fileId,chunkNo,repDegree,chunks.get(i));
            for (int j = 0; j < repDegree; j++) {
                //send messages
            }
        }*/




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


    public void startMulticastThread(String mcast_addr,int mcast_port,String message){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Multicast multicastThread = new Multicast(mcast_port,mcast_addr,message);
        executor.scheduleAtFixedRate(multicastThread,0,1, TimeUnit.SECONDS);
    }

}