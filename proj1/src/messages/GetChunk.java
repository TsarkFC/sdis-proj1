package messages;

// <Version> GETCHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class GetChunk extends Message{
    public GetChunk(Double version, Integer senderId, Integer fileId, Integer chunkNo) {
        super(version, "GETCHUNK", senderId, fileId, chunkNo, null, null);
    }
}
