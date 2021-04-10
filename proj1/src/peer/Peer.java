package peer;

import channels.ChannelCoordinator;
import filehandler.FileHandler;
import peer.metadata.Metadata;
import protocol.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

//java peer.Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
public class Peer implements RemoteObject {

    private ChannelCoordinator channelCoordinator;
    private PeerArgs peerArgs;
    private Metadata metadata;
    private String fileSystem;
    private Protocol protocol;
    private String restoreDir;
    private String filesDir;

    private List<String> chunksReceived = new ArrayList<>();
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, byte[]>> activeRestores = new ConcurrentHashMap<>();

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
            StartProtocol startProtocol = new StartProtocol(peer);
            startProtocol.sendStartingMessage();

        } catch (Exception e) {
            System.out.println("Error creating peer and connecting to RMI: " + e);
        }
    }

    public void createMetadata() {
        Metadata metadata = new Metadata(this.getArgs().getMetadataPath());
        this.setMetadata(metadata.readMetadata());
    }

    public void createChannels() {
        channelCoordinator = new ChannelCoordinator(this);
    }

    public void startFileSystem() throws IOException {
        fileSystem = "../filesystem/" + peerArgs.getPeerId();
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
    public String restore(String path) throws IOException {
        System.out.println("Initiator peer received Restore");
        this.protocol = new RestoreProtocol(path, this);
        this.protocol.initialize();
        return null;
    }

    @Override
    public String delete(String path) throws IOException, InterruptedException {
        System.out.println("Initiator peer received Delete");
        this.protocol = new DeleteProtocol(path, this);
        this.protocol.initialize();
        return null;
    }

    @Override
    public String state() throws RemoteException {
        return metadata.returnState();
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

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public PeerArgs getArgs() {
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
    public boolean isVanillaVersion() {
        return peerArgs.getVersion() == 1.0;
    }

    public boolean hasReceivedChunk(String chunkId) {
        return chunksReceived.contains(chunkId);
    }

    public void addChunkReceived(String chunkId) {
        this.chunksReceived.add(chunkId);
    }

    public void resetChunksReceived() {
        this.chunksReceived = new ArrayList<>();
    }

    public void addRestoreEntry(String fileId) {
        activeRestores.put(fileId, new ConcurrentHashMap<>());
    }

    public void addChunk(String fileId, Integer chunkNo, byte[] chunk) {
        ConcurrentHashMap<Integer, byte[]> restore = activeRestores.get(fileId);
        if (restore == null) {
            System.out.println("[RESTORE] could not find file entry in restore data!");
            return;
        }
        restore.put(chunkNo, chunk);

        System.out.println("NUMBER OF CHUNKS: " + FileHandler.getNumberOfChunks(metadata.getFileSize(fileId)));
        if (restore.size() >= FileHandler.getNumberOfChunks(metadata.getFileSize(fileId))) {
            Path restoreFilePath = Paths.get(metadata.getFileMetadata(fileId).getPathname());
            String filename = getRestoreDir() + "/" + restoreFilePath.getFileName();
            FileHandler.restoreFile(filename, restore);

            activeRestores.remove(fileId);
            System.out.println("[RESTORE] completed");
        }
    }

    public boolean hasRestoreEntry(String fileId) {
        return activeRestores.get(fileId) != null;
    }
}