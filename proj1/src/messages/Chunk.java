package messages;

// <Version> CHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>

public class Chunk extends MsgWithChunk {
    private byte[] body;
    private static int BODY_IDX = 5;

    public Chunk(Double version, Integer senderId, String fileId, Integer chunkNo, byte[] body) {
        super(version,senderId,fileId,chunkNo);
        this.body = body;
    }
    public Chunk(String msg){
        super(msg);
        this.body = tokens[BODY_IDX].substring(4).getBytes();
    }

    @Override
    public String getMsgType() {
        return "CHUNK";
    }

    @Override
    protected String getChildString() {
        //TODO ADD MsgBody class
        return String.format("%s",getDoubleCRLF());
    }

    @Override
    public int getNumberArguments() {
        return 6;
    }

    @Override
    public byte[] getBytes() {
        //Vai ser igual ao putchunk
        return null;
    }
}
