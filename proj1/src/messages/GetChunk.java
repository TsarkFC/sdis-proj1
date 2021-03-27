package messages;

// <Version> GETCHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class GetChunk implements Message {
    private Double version;
    private Integer senderId;
    private String fileId;
    private Integer chunkNo;

    public GetChunk(Double version, Integer senderId, String fileId, Integer chunkNo) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public byte[] getBytes() {
        return null;
    }
}
