package messages;

// <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
public class PutChunk extends Message{
    public PutChunk(Double version, Integer senderId, String fileId, Integer chunkNo, Integer replicationDeg, byte[] body) {
        super(version, "PUTCHUNK", senderId, fileId, chunkNo, replicationDeg, body);
    }
}
