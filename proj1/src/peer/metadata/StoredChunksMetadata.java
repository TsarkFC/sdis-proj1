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
     * ChunkMetadata contains all chunk necessary information
     */
    Map<String, ChunkMetadata> chunksInfo = new HashMap<>();

    public String getChunkId(String fileId, Integer chunkNo) {
        return fileId + "-" + chunkNo;
    }

    /**
     * Updating when received STORED messages
     */
    public void updateChunkInfo(String fileId, Integer chunkNo, Integer peerId) {
        String chunkId = getChunkId(fileId, chunkNo);
        if (chunksInfo.containsKey(chunkId))  {
            ChunkMetadata chunk = chunksInfo.get(chunkId);
            chunk.addPeer(peerId);
        }
    }

    /**
     * Updating when received BACKUP messages and before sending STORED messages
     */
    public void updateChunkInfo(String fileId, Integer chunkNo, Integer repDgr, Integer chunkSize, Integer peerId) {
        String chunkId = getChunkId(fileId, chunkNo);
        if (!chunksInfo.containsKey(chunkId)) {
            List<Integer> peerIds = new ArrayList<>();
            peerIds.add(peerId);
            chunksInfo.put(chunkId, new ChunkMetadata(chunkSize, chunkId, repDgr, peerIds));
        } else {
            ChunkMetadata chunk = chunksInfo.get(chunkId);
            chunk.addPeer(peerId);
        }
    }

    public void deleteChunk(String fileId, Integer chunkNo) {
        String chunkId = fileId + "-" + chunkNo;
        if (!chunksInfo.containsKey(chunkId)) {
            System.out.println("Cannot delete Chunk from Metadata");
        } else {
            chunksInfo.remove(chunkId);
        }
    }

    public void deleteChunksFile(List<Integer> chunksNums, String fileID) {
        for (Integer chunkNo : chunksNums) {
            String chunkId = getChunkId(fileID, chunkNo);
            chunksInfo.remove(chunkId);
        }
    }

    public Integer getStoredCount(String fileId, Integer chunkNo) {
        String chunkId = fileId + "-" + chunkNo;
        if (!chunksInfo.containsKey(chunkId)) {
            return 0;
        } else {
            return chunksInfo.get(chunkId).getPerceivedRepDgr();
        }
    }

    public Integer getFileStoredCount(String fileId) {
        int count = 0;
        for (String chunkId : chunksInfo.keySet()) {
            if (chunkId.split("-")[0].equals(fileId)) {
                count += chunksInfo.get(chunkId).getPerceivedRepDgr();
            }
        }
        return count;
    }

    public boolean chunkIsStored(String fileID,int chunkNo){
        return !chunksInfo.containsKey(getChunkId(fileID,chunkNo));
    }

    public ChunkMetadata getChunk(String fileId,Integer chunkNo){
        String chunkId = fileId + "-" + chunkNo;
        if (!chunksInfo.containsKey(chunkId)) {
            return null;
        } else {
            return chunksInfo.get(chunkId);
        }
    }

    public String returnData() {
        StringBuilder state = new StringBuilder();

        for (Map.Entry<String, ChunkMetadata> entry : chunksInfo.entrySet()) {
            ChunkMetadata chunkMetadata = entry.getValue();
            state.append("[Stored chunk ").append(entry.getKey()).append("]\n");
            state.append(String.format("Size (kb): %d\nReplication Degree: %d\nPerceived replication Degree: %d\n",
                    chunkMetadata.getSizeKb(), chunkMetadata.getRepDgr(), chunkMetadata.getPerceivedRepDgr()));
        }
        return state.toString();
    }
}
