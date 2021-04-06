package peer.metadata;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoredChunksMetadata implements Serializable {

    /**
     * Information about chunks saved by the peer.
     * String key identifies the chunk (<fileId>-<chunkNo>)
     * List<Integer> identifies the peers who stored the message
     */
    Map<String, List<Integer>> chunksInfo = new HashMap<>();
    String path;

    public StoredChunksMetadata(String path) {
        this.path = path;
        System.out.println("METADATA PATH: " + path);
    }

    public String getChunkId(String fileId,Integer chunkNo){ return fileId + "-" + chunkNo; }

    public void updateChunkInfo(String fileId, Integer chunkNo, Integer storedNo) throws IOException {
        String chunkId = getChunkId(fileId,chunkNo);
        if (!chunksInfo.containsKey(chunkId)) {
            List<Integer> stored = new ArrayList<>();
            stored.add(storedNo);
            chunksInfo.put(chunkId, stored);
        } else {
            List<Integer> peerIds = chunksInfo.get(chunkId);
            if (!peerIds.contains(storedNo))
                peerIds.add(storedNo);
        }
        writeMetadata();
    }

    public void deleteChunk(String fileId, Integer chunkNo) throws IOException {
        String chunkId = fileId + "-" + chunkNo;
        if (!chunksInfo.containsKey(chunkId)) {
            System.out.println("Cannot delete Chunk from Metadata");
        }else{
            chunksInfo.remove(chunkId);
        }
        writeMetadata();
    }

    public void deleteChunksFile(List<Integer> chunksNums,String fileID){
        for (Integer chunkNo : chunksNums){
            String chunkId = getChunkId(fileID,chunkNo);
            if (chunksInfo.containsKey(chunkId)) {
                chunksInfo.remove(chunkId);
            }
        }
        try {
            writeMetadata();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public Integer getStoredCount(String fileId, Integer chunkNo) {
        String chunkId = fileId + "-" + chunkNo;
        if (!chunksInfo.containsKey(chunkId)) {
            return 0;
        } else {
            return chunksInfo.get(chunkId).size();
        }
    }

    public Integer getFileStoredCount(String fileId) {
        Integer count = 0;
        for (String chunkId : chunksInfo.keySet()) {
            if (chunkId.split("-")[0].equals(fileId)) {
                count += chunksInfo.get(chunkId).size();
            }
        }
        return count;
    }

    private void writeMetadata() throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(path));
        os.writeObject(this);
        os.close();
    }

    public StoredChunksMetadata readMetadata(){
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(path));
            StoredChunksMetadata storedChunksMetadata = (StoredChunksMetadata) is.readObject();
            Map<String, List<Integer>> chunksInfo = storedChunksMetadata.getChunksInfo();
            for (String chunkId : chunksInfo.keySet()) {
                System.out.println("FILEID-CHUNK: CHUNKID" + chunkId + " : " + chunksInfo.get(chunkId));
            }
            is.close();
            return storedChunksMetadata;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No data to read from peer");
            System.out.println("Creating new one...");
            return new StoredChunksMetadata(path);

        }
    }

    public Map<String, List<Integer>> getChunksInfo() {
        return chunksInfo;
    }

    public String getPath() {
        return path;
    }
}
