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

    private ChannelCoordinator channelCoordinator;
    private PeerArgs peerArgs;
    private StateMetadata stateMetadata;
    private String fileSystem;
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

    public void createMetadata() {
        StateMetadata metadata = new StateMetadata(this.getPeerArgs().getMetadataPath());
        this.setPeerMetadata(metadata.readMetadata());
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

    @Override
    public String backup(File file, int repDegree) throws IOException {
        System.out.println("Initiator peer received Backup");
        this.protocol = new BackupProtocol(file, this, repDegree);
        this.protocol.initialize();
        return null;
    }

    @Override
    public String restore(File file) throws IOException {
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
    public String state() throws RemoteException {
        return stateMetadata.returnState();
    }

    @Override
    public String reclaim(double maxDiskSpace) throws IOException {
        System.out.println("Initiator peer received Reclaim");
        this.protocol = new ReclaimProtocol(maxDiskSpace, this);
        this.protocol.initialize();
        return null;
    }

    public String getFileSystem() {
        return fileSystem;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public StateMetadata getPeerMetadata() {
        return stateMetadata;
    }

    public void setPeerMetadata(StateMetadata stateMetadata) {
        this.stateMetadata = stateMetadata;
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

    public ChannelCoordinator getChannelCoordinator() {
        return channelCoordinator;
    }

    public void setChannelCoordinator(ChannelCoordinator channelCoordinator) {
        this.channelCoordinator = channelCoordinator;
    }
}