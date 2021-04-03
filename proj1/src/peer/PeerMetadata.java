package peer;

import utils.FileHandler;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerMetadata implements Serializable {

    /**
     * Information about chunks saved by the peer.
     * String key identifies the chunk (<fileId>-<chunkNo>)
     * Integer value identifies the number of STORED messages associated to the chunk
     */
    Map<String, Integer> chunksInfo = new HashMap<>();
    String path;

    public PeerMetadata(String path) {
        this.path = path;
        System.out.println("METADATA PATH: " + path);
    }

    public String getChunkId(String fileId,Integer chunkNo){ return fileId + "-" + chunkNo; }

    public void updateChunkInfo(String fileId, Integer chunkNo, Integer storedNo) throws IOException {
        String chunkId = getChunkId(fileId,chunkNo);
        if (!chunksInfo.containsKey(chunkId)) {
            chunksInfo.put(chunkId, storedNo);
        } else {
            Integer currentStoredNo = chunksInfo.get(chunkId);
            chunksInfo.put(chunkId, currentStoredNo + storedNo);
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
        return chunksInfo.getOrDefault(chunkId, 0);
    }

    public Integer getFileStoredCount(String fileId) {
        Integer count = 0;
        for (String chunkId : chunksInfo.keySet()) {
            if (chunkId.split("-")[0].equals(fileId)) {
                count += chunksInfo.get(chunkId);
            }
        }
        return count;
    }

    private void writeMetadata() throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(path));
        os.writeObject(this);
        os.close();
    }

    public PeerMetadata readMetadata(){
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(path));
            PeerMetadata peerMetadata = (PeerMetadata) is.readObject();
            Map<String,Integer> chunksInfo = peerMetadata.getChunksInfo();
            for (String chunkId : chunksInfo.keySet()) {
                System.out.println("FILEID-CHUNK: CHUNKID" + chunkId + " : " + chunksInfo.get(chunkId));
            }
            is.close();
            return peerMetadata;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No data to read from peer");
            System.out.println("Creating new one...");
            return new PeerMetadata(path);

        }
    }

    public Map<String, Integer> getChunksInfo() {
        return chunksInfo;
    }

    public String getPath() {
        return path;
    }
}
