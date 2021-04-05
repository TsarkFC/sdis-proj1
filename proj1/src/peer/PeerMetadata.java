package peer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerMetadata implements Serializable {

    /**
     * Information about chunks saved by the peer.
     * String key identifies the chunk (<fileId>-<chunkNo>)
     * List<Integer> identifies the peers who stored the message
     */
    Map<String, List<Integer>> chunksInfo = new HashMap<>();
    String path;

    public PeerMetadata(String path) {
        this.path = path;
        System.out.println("METADATA PATH: " + path);
    }

    public void updateChunkInfo(String fileId, Integer chunkNo, Integer peerId) throws IOException {
        String chunkId = fileId + "-" + chunkNo;
        if (!chunksInfo.containsKey(chunkId)) {
            List<Integer> stored = new ArrayList<>();
            stored.add(peerId);
            chunksInfo.put(chunkId, stored);
        } else {
            List<Integer> peerIds = chunksInfo.get(chunkId);
            if (!peerIds.contains(peerId))
                peerIds.add(peerId);
        }
        writeMetadata();
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
    public Map<String, List<Integer>> getChunksInfo() {
        return chunksInfo;
    }

    public String getPath() {
        return path;
    }
}
