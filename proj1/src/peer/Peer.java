package peer;

import channels.BackupChannel;
import channels.ChannelCoordinator;
import channels.ControlChannel;
import channels.RestoreChannel;
import messages.Delete;
import messages.GetChunk;
import messages.PutChunk;
import protocol.*;
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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//java peer.Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
public class Peer implements RemoteObject {

    private PeerArgs peerArgs;
    private PeerMetadata peerMetadata;
    private String fileSystem;
    private ChannelCoordinator channelCoordinator;
    private Protocol protocol;


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
            PeerMetadata metadata = new PeerMetadata(peer.getPeerArgs().getMetadataPath());
            peer.setPeerMetadata(metadata.readMetadata());


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
        channelCoordinator = new ChannelCoordinator(this);
    }


    public void startFileSystem() throws IOException {
        fileSystem = "filesystem/" + peerArgs.getPeerId();
        Files.createDirectories(Paths.get(fileSystem));
    }

    public String getFileSystem() {
        return fileSystem;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public PeerMetadata getPeerMetadata() {
        return peerMetadata;
    }

    public void setPeerMetadata(PeerMetadata peerMetadata) {
        this.peerMetadata = peerMetadata;
    }

    public PeerArgs getPeerArgs() {
        return peerArgs;
    }

    private void readMetadata() {
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(peerArgs.getMetadataPath()));
            peerMetadata = (PeerMetadata) is.readObject();
            Map<String, List<Integer>> chunksInfo = peerMetadata.getChunksInfo();
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
        /*List<byte[]> messages = new ArrayList<>();
        FileHandler fileHandler = new FileHandler(file);
        List<byte[]> chunks = fileHandler.splitFile();
        String fileId = fileHandler.createFileId();

        int chunkNo = 0;
        for (byte[] chunk : chunks) {
            PutChunk backupMsg = new PutChunk(peerArgs.getVersion(), peerArgs.getPeerId(), fileId, chunkNo, repDegree, chunk);
            byte[] msg = backupMsg.getBytes();
            messages.add(msg);
            chunkNo++;
        }

        int storedExpected = chunks.size() * repDegree;
        int limit = 5;
        int reps = 1;
        int timeWait = 1;
        while (reps <= limit) {
            ThreadHandler.startMulticastThread(peerArgs.getAddressList().getMdbAddr().getAddress(),
                    peerArgs.getAddressList().getMdbAddr().getPort(), messages);
            TimeUnit.SECONDS.sleep(timeWait);
            timeWait *= 2;
            if (storedExpected <= peerMetadata.getFileStoredCount(fileId))
                break;
            reps++;
        }*/

        System.out.println("Initiator peer received Backup");
        this.protocol = new BackupProtocol(file, this, repDegree);
        this.protocol.initialize();
        return null;
    }

    @Override
    public String restore(File file) throws IOException, InterruptedException {
        System.out.println("Initiator peer received Restore");
        this.protocol = new RestoreProtocol(file, this);
        this.protocol.initialize();
        return null;
    }

    @Override
    public String delete(File file) throws IOException, InterruptedException {
        this.protocol = new DeleteProtocol(file, this);
        this.protocol.initialize();
        return null;
    }

    @Override
    public String state(File file) throws RemoteException {
        System.out.println("State");
        return null;
    }

    @Override
    public String reclaim(double maxDiskSpace) throws IOException {
        this.protocol = new ReclaimProtocol(maxDiskSpace, this);
        this.protocol.initialize();
        return null;
    }
}