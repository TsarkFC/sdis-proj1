package messages;

// <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
public class PutChunk extends Message{
    public PutChunk(Double version, Integer senderId, Integer fileId, Integer chunkNo, Integer replicationDeg, String body) {
        super(version, "PUTCHUNK", senderId, fileId, chunkNo, replicationDeg, body);
    }
}
