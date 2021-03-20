import java.io.*;
import java.rmi.RemoteException;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//java Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
public class Peer implements RemoteObject {

    private final double protocol_version = 1.0;
    private final int peerId = 0;
    private final static  String MC_HOSTNAME="228.25.25.25";
    private final static int  MC_PORT=4445;

    private final static  String MDB_HOSTNAME="228.25.25.25";
    private final static int  MDB_PORT=4446;

    private final static  String MDC_HOSTNAME="228.25.25.25";
    private final static int  MDC_PORT=4447;



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
            System.out.println("Hosting peer already associated to rmi");
        }


    }

    // multicast channel, the control channel (MC), that is used for control messages.
    // All peers must subscribe the MC channel.
    // Some subprotocols use also one of two multicast data channels, MDB and MDR, which are used to backup and restore file chunk data.

    public void startMulticastThread(String mcast_addr, int mcast_port, String message){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        BackupChannel multicastThread = new BackupChannel(mcast_port, mcast_addr, message);
        executor.scheduleAtFixedRate(multicastThread,0,1, TimeUnit.SECONDS);
    }

    @Override
    public String backup(File file,int repDegree) throws RemoteException {

        System.out.println("ZAS");
        //startMulticastThread(MC_HOSTNAME,MC_PORT,"oi");
        startMulticastThread(MDB_HOSTNAME,MDB_PORT,"Multicast message mdb");
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




}