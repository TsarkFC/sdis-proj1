package peer.metadata;

import java.io.*;
import java.util.*;

public class StateMetadata implements Serializable {

    /**
     * Maps fileId to FileMetadata
     */
    Map<String, FileMetadata> hostingFileInfo = new HashMap<>();

    /**
     * Contains information about stored chunks
     */
    StoredChunksMetadata storedChunksMetadata;

    /**
     * Path where metadata will be saved
     */
    String path;

    /**
     * Max space the peer can store
     */
    double maxSpace = -1;

    public StateMetadata(String path) {
        this.path = path;
        storedChunksMetadata = new StoredChunksMetadata();
    }

    /**
     * Updating information on initiator peer data
     */
    public void addHostingEntry(FileMetadata fileMetadata) throws IOException {
        hostingFileInfo.put(fileMetadata.getId(), fileMetadata);
        writeMetadata();
    }

    public void updateHostingInfo(FileMetadata hostingMetadata, Integer chunkNo, Integer peerId) throws IOException {
        hostingMetadata.addChunk(chunkNo, peerId);
        writeMetadata();
    }

    public boolean hasFile(String fileId) {
        return hostingFileInfo.containsKey(fileId);
    }

    public boolean hasChunk(String fileId,int chunkNo) {
        String chunkID = storedChunksMetadata.getChunkId(fileId,chunkNo);
        return storedChunksMetadata.getChunksInfo().containsKey(chunkID);
    }



    public String getFileIdFromPath(String pathName) {
        for (Map.Entry<String, FileMetadata> entry : hostingFileInfo.entrySet()) {
            if (entry.getValue().getPathname().equals(pathName)) return entry.getKey();
        }
        return null;
    }

    public void deleteFile(String fileId) throws IOException {
        hostingFileInfo.remove(fileId);
        storedChunksMetadata.deleteChunksFromFile(fileId);
        writeMetadata();
    }

    /**
     * Updating information on stored chunks data
     */
    public void updateStoredInfo(String fileId, Integer chunkNo, Integer peerId) throws IOException {
        FileMetadata hostingMetadata = hostingFileInfo.get(fileId);
        if (hostingMetadata != null) {
            updateHostingInfo(hostingMetadata, chunkNo, peerId);
        } else {
            storedChunksMetadata.updateChunkInfo(fileId, chunkNo, peerId);
        }
        writeMetadata();
    }

    public void updateStoredInfo(String fileId, Integer chunkNo, Integer repDgr, Double chunkSize, Integer peerId) throws IOException {
        int chunkSizeKb = (int) Math.round(chunkSize);
        storedChunksMetadata.updateChunkInfo(fileId, chunkNo, repDgr, chunkSizeKb, peerId);
        writeMetadata();
    }

    public boolean verifyRepDgr(String fileId, Integer repDgr, Integer numOfChunks) {
        Map<Integer, List<Integer>> chunkData = hostingFileInfo.get(fileId).getChunksData();
        int chunksCount = 0;
        for (Map.Entry<Integer, List<Integer>> entry : chunkData.entrySet()) {
            chunksCount++;
            if (entry.getValue().size() < repDgr) return false;
        }
        return chunksCount == numOfChunks;
    }

    private void writeMetadata() throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(path));
        os.writeObject(this);
        os.close();
    }

    public StateMetadata readMetadata() {
        try {
            System.out.println("path = " + path);
            File f = new File(path);
            System.out.println("Name: " + f.getName());
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(path));
            StateMetadata stateMetadata = (StateMetadata) is.readObject();
            is.close();
            return stateMetadata;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error: " + e);
            System.out.println("No data to read from peer");
            System.out.println("Creating new one...");
            return new StateMetadata(path);
        }
    }

    public String returnState() {
        StringBuilder state = new StringBuilder();

        // hosting data
        for (String fileId : hostingFileInfo.keySet()) {
            FileMetadata fileMetadata = hostingFileInfo.get(fileId);
            state.append("[Hosting]\n");
            state.append(String.format("Pathname: %s\nID: %s\nReplication Degree: %d\n",
                    fileMetadata.getPathname(), fileMetadata.getId(), fileMetadata.getRepDgr()));
            state.append("[Chunks]\n");
            for (Map.Entry<Integer, List<Integer>> entry : fileMetadata.getChunksData().entrySet()) {
                state.append("[").append(entry.getKey()).append("]");
                state.append(" Perceived replication degree = ").append(entry.getValue().size()).append("\n");
            }
        }

        // stored chunks data
        state.append(storedChunksMetadata.returnData());

        return state.toString();
    }

    public void setMaxSpace(double maxSpace){
        this.maxSpace= maxSpace;
    }
    public double getMaxSpace(){return maxSpace;}

    public boolean hasSpace(double newFileSizeKb){
        int storedSize = storedChunksMetadata.getStoredSize();
        double finalSpace = storedSize+newFileSizeKb;
        if(maxSpace==-1) return true;
        return maxSpace > finalSpace;
    }

    public void printState(){
        System.out.println("\n********************************************");
        System.out.println("************* State Metadata  **************");
        System.out.println("Max space: " + maxSpace);
        for (String fileId : hostingFileInfo.keySet()) {
            FileMetadata fileMeta = hostingFileInfo.get(fileId);
            System.out.println("File:");
            System.out.println(String.format("\tPathname: %s\n\tID: %s\n\tReplication Degree: %d\n",
                    fileMeta.getPathname(),fileMeta.getId(),fileMeta.getRepDgr()));
        }
        System.out.println("\tSaved Chunks:");
        for (ChunkMetadata chunkMetadata : storedChunksMetadata.getChunksInfo().values()){
            System.out.println("\t\tID: " + chunkMetadata.getId());
            System.out.println("\t\tSize: " + chunkMetadata.getSizeKb());
            System.out.println("\t\tDesired Rep: " + chunkMetadata.getRepDgr());
            System.out.println("\t\tPerceived Rep: " + chunkMetadata.getPerceivedRepDgr());
            System.out.println();
        }
    }



    public String getPath() {
        return path;
    }

    public Map<String, FileMetadata> getHostingFileInfo() {
        return hostingFileInfo;
    }

    public StoredChunksMetadata getStoredChunksMetadata() {
        return storedChunksMetadata;
    }

}
