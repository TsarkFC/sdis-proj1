package messages;

// <Version> CHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
//TODO Sera que ele devia extender? apesar de nao ser uma mensagem?
public class Chunk extends MsgWithChunk {
    /*private Double version;
    private Integer senderId;
    private String fileId;
    private Integer chunkNo;*/
    private byte[] body;

    public Chunk(Double version, Integer senderId, String fileId, Integer chunkNo, byte[] body) {
        super(version,senderId,fileId,chunkNo);
        this.body = body;
    }

    @Override
    public String getMsgType() {
        return null;
    }

    @Override
    protected String getChildString() {
        return null;
    }

    @Override
    public int getNumberArguments() {
        return 0;
    }

    @Override
    public byte[] getBytes() {
        return null;
    }
}
