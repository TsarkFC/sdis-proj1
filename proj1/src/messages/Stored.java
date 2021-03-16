package messages;

// <Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class Stored extends Message {
    public Stored(Double version, Integer senderId, Integer fileId, Integer chunkNo) {
        super(version, "STORED", senderId, fileId, chunkNo, null, null);
    }
}
