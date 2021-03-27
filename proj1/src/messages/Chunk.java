package messages;

// <Version> CHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
public class Chunk implements Message {
    private Double version;
    private Integer senderId;
    private String fileId;
    private Integer chunkNo;
    private byte[] body;

    public Chunk(Double version, Integer senderId, String fileId, Integer chunkNo, byte[] body) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.body = body;
    }

    @Override
    public byte[] getBytes() {
        return null;
    }
}
