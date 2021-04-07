package peer.metadata;

import java.io.*;
import java.util.*;

public class StateMetadata implements Serializable {

    Map<String, FileMetadata> hostingFileInfo = new HashMap<>();
    StoredChunksMetadata storedChunksMetadata;
    String path;

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

    public void deleteFile(String fileId) throws IOException {
        if (!hostingFileInfo.containsKey(fileId)) {
            System.out.println("Cannot delete File from Metadata");
        } else {
            hostingFileInfo.remove(fileId);
        }
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

    public void updateStoredInfo(String fileId, Integer chunkNo, Integer repDgr, Integer chunkSize, Integer peerId) throws IOException {
        storedChunksMetadata.updateChunkInfo(fileId, chunkNo, repDgr, chunkSize, peerId);
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

    public void deleteChunksFile(List<Integer> chunksNums, String fileID) {
        storedChunksMetadata.deleteChunksFile(chunksNums, fileID);
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
            System.out.println("test1");
            StateMetadata stateMetadata = (StateMetadata) is.readObject();
            System.out.println("test2");
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


    public String getPath() {
        return path;
    }

    public Map<String, FileMetadata> getHostingFileInfo() {
        return hostingFileInfo;
    }
}
