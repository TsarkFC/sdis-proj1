package peer.metadata;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StoredChunksMetadata implements Serializable {

    /**
     * Information about chunks saved by the peer.
     * String key identifies the chunk (<fileId>-<chunkNo>)
     * ChunkMetadata contains all chunk necessary information
     */
    final ConcurrentHashMap<String, ChunkMetadata> chunksInfo = new ConcurrentHashMap<>();

    public String getChunkId(String fileId, Integer chunkNo) {
        return fileId + "-" + chunkNo;
    }

    public String[] getFileChunkIds(String chunkId){
        return chunkId.split("-");
    }

    /**
     * Updating when received STORED messages
     */
    public void updateChunkInfo(String fileId, Integer chunkNo, Integer peerId) {
        String chunkId = getChunkId(fileId, chunkNo);
        ChunkMetadata chunk;

        if (chunksInfo.containsKey(chunkId)) {
            chunk = chunksInfo.get(chunkId);
        } else {
            chunk = new ChunkMetadata();
            chunksInfo.put(chunkId, chunk);
        }
        chunk.addPeer(peerId);
    }

    /**
     * Updating when received BACKUP messages and before sending STORED messages
     */
    public void updateChunkInfo(String fileId, Integer chunkNo, Integer repDgr, Integer chunkSize, Integer peerId) {
        String chunkId = getChunkId(fileId, chunkNo);
        if (!chunksInfo.containsKey(chunkId)) {
            Set<Integer> peerIds = new HashSet<>();
            peerIds.add(peerId);
            chunksInfo.put(chunkId, new ChunkMetadata(chunkSize, chunkId, repDgr, peerIds));
        } else {
            ChunkMetadata chunkMetadata = chunksInfo.get(chunkId);
            //Saving chunk after having received stored messages
            Set<Integer> peerIds = chunkMetadata.getPeerIds();
            chunksInfo.put(chunkId, new ChunkMetadata(chunkSize, chunkId, repDgr, peerIds));
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

    public void deleteChunksFromFile(String fileId) {
        Iterator<String> it = chunksInfo.keySet().iterator();
        while (it.hasNext()) {
            String chunkId = it.next();
            if (chunkId.split("-")[0].equals(fileId)) {
                it.remove();
            }
        }
    }

    public Integer getStoredCount(String fileId, Integer chunkNo) {
        String chunkId = getChunkId(fileId, chunkNo);
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

    public boolean chunkIsStored(String fileID, int chunkNo) {
        return chunksInfo.containsKey(getChunkId(fileID, chunkNo));
    }

    public ChunkMetadata getChunk(String fileId, Integer chunkNo) {
        String chunkId = fileId + "-" + chunkNo;
        return chunksInfo.getOrDefault(chunkId, null);
    }

    public String returnData() {
        String tabs = "   ";
        StringBuilder state = new StringBuilder();
        int chunkNum = 0;
        for (Map.Entry<String, ChunkMetadata> entry : chunksInfo.entrySet()) {
            state.append(tabs +"Chunk  " + chunkNum + "\n");
            chunkNum++;
            ChunkMetadata chunkMetadata = entry.getValue();
            String[] fileChunkIds = getFileChunkIds(entry.getKey());
            state.append(tabs +"  * File ID: ").append(fileChunkIds[0]).append("\n");
            state.append(tabs +"  * Chunk Id: ").append(fileChunkIds[1]).append("\n");
            state.append(String.format("%s  * Size (kb): %d\n%s  * Replication Degree: %d\n%s  * Perceived replication Degree: %d\n",tabs,
                    chunkMetadata.getSizeKb(),tabs, chunkMetadata.getRepDgr(),tabs, chunkMetadata.getPerceivedRepDgr()));
        }
        return state.toString();
    }


    public int getStoredSize() {
        int size = 0;
        for (ChunkMetadata chunkMetadata : chunksInfo.values()) {
            size += chunkMetadata.getSizeKb();
        }
        return size;
    }

    public Map<String, ChunkMetadata> getChunksInfo() {
        return chunksInfo;
    }
}
