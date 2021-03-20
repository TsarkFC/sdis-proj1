package messages;

// <Version> GETCHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class GetChunk extends Message{
    public GetChunk(Double version, Integer senderId, String fileId, Integer chunkNo) {
        super(version, "GETCHUNK", senderId, fileId, chunkNo, null, null);
    }

    @Override
    public String getMsgString() {
        return null;
    }
}
