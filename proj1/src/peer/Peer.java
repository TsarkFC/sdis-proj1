package peer;

import channels.ChannelCoordinator;
import peer.metadata.StateMetadata;
import peer.metadata.StoredChunksMetadata;
import protocol.*;
import utils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//java peer.Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
public class Peer implements RemoteObject {

    private PeerArgs peerArgs;
    private StoredChunksMetadata storedChunksMetadata;
    private StateMetadata stateMetadata;
    private String fileSystem;
    private ChannelCoordinator channelCoordinator;
    private Protocol protocol;
    private String restoreDir;
    private String filesDir;

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
            peer.createMetadata();

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

    public void startMulticastThread(String mcastAddr, int mcastPort, List<byte[]> message) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Multicast multicastThread = new Multicast(mcastPort, mcastAddr, message);
        executor.schedule(multicastThread, 0, TimeUnit.SECONDS);
    }

    public void createMetadata(){
        StoredChunksMetadata metadata = new StoredChunksMetadata(this.getPeerArgs().getMetadataPath());
        this.setPeerMetadata(metadata.readMetadata());
        StateMetadata stateMetadata = new StateMetadata(this.getPeerArgs().getStateMetadataPath());
        //stateMetadata.readState();
    }

    public void createChannels() {
        channelCoordinator = new ChannelCoordinator(this);
    }

    public void startFileSystem() throws IOException {
        fileSystem = "filesystem/" + peerArgs.getPeerId();
        filesDir = fileSystem + "/files";
        restoreDir = fileSystem + "/restored";
        Files.createDirectories(Paths.get(filesDir));
        Files.createDirectories(Paths.get(restoreDir));
    }

    public String getFileSystem() {
        return fileSystem;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public StoredChunksMetadata getPeerStoredMetadata() {
        return storedChunksMetadata;
    }

    public StateMetadata getPeerStateMetadata() {
        return stateMetadata;
    }

    public void setPeerMetadata(StateMetadata stateMetadata) {
        this.stateMetadata = stateMetadata;
    }

    public void setPeerMetadata(StoredChunksMetadata storedChunksMetadata) {
        this.storedChunksMetadata = storedChunksMetadata;
    }

    public PeerArgs getPeerArgs() {
        return peerArgs;
    }

    public String getRestoreDir() {
        return restoreDir;
    }

    public String getFilesDir() {
        return filesDir;
    }

    @Override
    public String backup(File file, int repDegree) throws IOException {
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
        System.out.println("Initiator peer received Delete");
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
        System.out.println("Initiator peer received Reclaim");
        this.protocol = new ReclaimProtocol(maxDiskSpace, this);
        this.protocol.initialize();
        return null;
    }
}