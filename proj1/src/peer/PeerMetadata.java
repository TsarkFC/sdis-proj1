package peer;

import java.io.*;
import java.util.HashMap;
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

    public void updateChunkInfo(String fileId, Integer chunkNo, Integer storedNo) throws IOException {
        String chunkId = fileId + "-" + chunkNo;
        if (!chunksInfo.containsKey(chunkId)) {
            chunksInfo.put(chunkId, storedNo);
        } else {
            Integer currentStoredNo = chunksInfo.get(chunkId);
            chunksInfo.put(chunkId, currentStoredNo + storedNo);
        }
        writeMetadata();
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
    public Map<String, Integer> getChunksInfo() {
        return chunksInfo;
    }

    public String getPath() {
        return path;
    }
}
