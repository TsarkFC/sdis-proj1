package peer;

import channels.BackupChannel;
import channels.Channel;
import channels.ControlChannel;
import messages.PutChunk;
import utils.*;

import java.io.*;
import java.rmi.RemoteException;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//java peer.Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
public class Peer implements RemoteObject {

    private final double protocol_version = 1.0;
    private final int peerId = 0;
    private PeerArgs peerArgs;

    public static void main(String[] args) {
        if (args.length != 9) {
            System.out.println("Usage: java Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>");
            return;
        }

        try {
            Peer obj = new Peer();
            obj.peerArgs = new PeerArgs(args);
            String remoteObjName = obj.peerArgs.getAccessPoint();

            RemoteObject stub = (RemoteObject) UnicastRemoteObject.exportObject(obj, 0);
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remoteObjName, stub);
            System.err.println("Peer with name: " + remoteObjName + " ready");

            //Create the channels
            createMDBChannel(obj.peerArgs.getAddressList());
            //É suposto ele so estar a aparecer no initiator peer as mensagens de stored?
            createMCChannel(obj.peerArgs.getAddressList());
            System.out.println("Created multicast channels");
        } catch (Exception e) {
            System.out.println("Peer name already taken");
        }
    }

    // multicast channel, the control channel (MC), that is used for control messages.
    // All peers must subscribe the MC channel.
    // Some subprotocols use also one of two multicast data channels, MDB and MDR, which are used to backup and restore file chunk data.

    public void startMulticastThread(String mcast_addr, int mcast_port, List<String> message) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Multicast multicastThread = new Multicast(mcast_port, mcast_addr, message);
        executor.schedule(multicastThread, 0, TimeUnit.SECONDS);
    }

    public static void createMDBChannel(AddressList addressList) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        BackupChannel backupChannel = new BackupChannel(addressList);
        executor.schedule(backupChannel, 0, TimeUnit.SECONDS);
    }

    public static void createMCChannel(AddressList addressList) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        ControlChannel controlChannel = new ControlChannel(addressList);
        executor.schedule(controlChannel, 0, TimeUnit.SECONDS);
    }

    @Override
    public String backup(File file, int repDegree) throws RemoteException {

        //Initiator peer recieved backup from client
        System.out.println("Initiator peer recieved backup from client");

        List<String> messages = new ArrayList<>();

        //A quem e que ele deve enviar os chunks?
        //Nao vai enviar de broadcast a toda gente
        //É suposto escolher randomly
        FileHandler fileHandler = new FileHandler(file);
        List<byte[]> chunks = fileHandler.splitFile();
        String fileId = fileHandler.createFileId();
        for (int chunkno = 0; chunkno < chunks.size(); chunkno++) {
            PutChunk backupMsg = new PutChunk(1.0, 0, fileId, chunkno, repDegree, chunks.get(chunkno));
            String msg = backupMsg.getMsgString();
            messages.add(msg);
            /*for (int j = 0; j < repDegree; j++) {
                //send messages
            }*/
        }
        //Ele esta a enviar para todos os peers
        //Ele aqui teria que começar a contar os segundos
        //Podia por aqui, dentro desta funçao um
        //Thread.sleep(1000);
        //.notifyAll()
        ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMdbAddr().getAddress(), peerArgs.getAddressList().getMdbAddr().getPort(), messages);

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