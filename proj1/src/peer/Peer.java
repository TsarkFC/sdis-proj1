package peer;

import channels.BackupChannel;
import channels.ChannelCoordinator;
import channels.ControlChannel;
import messages.PutChunk;
import utils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//java peer.Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
public class Peer implements RemoteObject {

    private PeerArgs peerArgs;
    private PeerMetadata peerMetadata;
    private String fileSystem;
    private ChannelCoordinator channelCoordinator;

    public static void main(String[] args) {
        if (args.length != 9) {
            System.out.println("Usage: java Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>");
            return;
        }

        try {
            // Peer creation
            Peer peer = new Peer();
            peer.peerArgs = new PeerArgs(args);
            peer.startFileSystem();
            peer.readMetadata();

            // RMI connection
            String remoteObjName = peer.peerArgs.getAccessPoint();
            RemoteObject stub = (RemoteObject) UnicastRemoteObject.exportObject(peer, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remoteObjName, stub);
            System.err.println("Peer with name: " + remoteObjName + " ready");
            peer.createChannels();

        } catch (Exception e) {
            System.out.println("Peer name already taken");
        }
    }

    // multicast channel, the control channel (MC), that is used for control messages.
    // All peers must subscribe the MC channel.
    // Some subprotocols use also one of two multicast data channels, MDB and MDR, which are used to backup and restore file chunk data.

    public void startMulticastThread(String mcast_addr, int mcast_port, List<byte[]> message) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Multicast multicastThread = new Multicast(mcast_port, mcast_addr, message);
        executor.schedule(multicastThread, 0, TimeUnit.SECONDS);
    }

    public void createChannels() {
        // Create the channels
        channelCoordinator = new ChannelCoordinator(
                this.createMDBChannel(this.peerArgs.getAddressList()),
                this.createMCChannel(this.peerArgs.getAddressList())
        );

        System.out.println("Created multicast channels");
    }

    public BackupChannel createMDBChannel(AddressList addressList) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        BackupChannel backupChannel = new BackupChannel(addressList, this);
        executor.schedule(backupChannel, 0, TimeUnit.SECONDS);
        return backupChannel;
    }

    public ControlChannel createMCChannel(AddressList addressList) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        ControlChannel controlChannel = new ControlChannel(addressList, this);
        executor.schedule(controlChannel, 0, TimeUnit.SECONDS);
        return controlChannel;
    }

    public void startFileSystem() throws IOException {
        fileSystem = "filesystem/" + peerArgs.getPeerId();
        Files.createDirectories(Paths.get(fileSystem));
    }

    public String getFileSystem() {
        return fileSystem;
    }

    public PeerMetadata getPeerMetadata() {
        return peerMetadata;
    }

    public PeerArgs getPeerArgs() {
        return peerArgs;
    }

    private void readMetadata() {
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(peerArgs.getMetadataPath()));
            peerMetadata = (PeerMetadata) is.readObject();
            Map<String,Integer> chunksInfo = peerMetadata.getChunksInfo();
            System.out.println("ZAS");
            for (String chunkId : chunksInfo.keySet()) {
                System.out.println("FILEID-CHUNK: CHUNKID" + chunkId + " : " + chunksInfo.get(chunkId));
            }
            is.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No data to read from peer " + peerArgs.getPeerId());
            System.out.println("Creating new one...");
            peerMetadata = new PeerMetadata(peerArgs.getMetadataPath());

        }
    }

    @Override
    public String backup(File file, int repDegree) throws IOException, InterruptedException {

        //Initiator peer recieved backup from client
        System.out.println("Initiator peer recieved backup from client");

        List<byte[]> messages = new ArrayList<>();
        FileHandler fileHandler = new FileHandler(file);
        List<byte[]> chunks = fileHandler.splitFile();
        String fileId = fileHandler.createFileId();

        int chunkNo = 0;
        for (byte[] chunk : chunks) {
            PutChunk backupMsg = new PutChunk(1.0, 0, fileId, chunkNo, repDegree, chunk);
            byte[] msg = backupMsg.getBytes();
            messages.add(msg);
            chunkNo++;
        }

        //Ele esta a enviar para todos os peers
        //Ele aqui teria que começar a contar os segundos
        //Podia por aqui, dentro desta funçao um
        //Thread.sleep(1000);
        //.notifyAll()
        int storedExpected = chunks.size() * repDegree;
        int limit = 5;
        int reps = 1;
        int timeWait =1;
        while (reps < limit) {
            ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMdbAddr().getAddress(),
                    peerArgs.getAddressList().getMdbAddr().getPort(), messages);
            TimeUnit.SECONDS.sleep(timeWait);
            timeWait *= 2;
            if (storedExpected <= peerMetadata.getFileStoredCount(fileId))
                break;
            reps++;
            //channelCoordinator.closeMcIn1Second();
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