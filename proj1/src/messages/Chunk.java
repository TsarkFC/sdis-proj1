package messages;

// <Version> CHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
public class Chunk extends Message{
    public Chunk(Double version, Integer senderId, String fileId, Integer chunkNo, byte[] body) {
        super(version, "CHUNK", senderId, fileId, chunkNo, null, body);
    }
}
