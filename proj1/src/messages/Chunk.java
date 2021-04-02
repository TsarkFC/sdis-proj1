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
        //<Version> <MessageType> <SenderId> <FileId> <ChunkNo> <CRLF>
        String header = String.format("%s %s %d %s %d %s", this.version, getMsgType(), this.senderId,
                this.fileId, this.chunkNo, getDoubleCRLF());
        byte[] headerBytes = header.getBytes();

        // create a destination array that is the size of the two arrays
        byte[] msgBytes = new byte[headerBytes.length + this.body.length];

        // copy headerBytes into start of msgBytes (from pos 0, copy headerBytes.length bytes)
        System.arraycopy(headerBytes, 0, msgBytes, 0, headerBytes.length);

        // copy this.body into end of msgBytes (from pos headerBytes.length, copy this.body.length bytes)
        System.arraycopy(this.body, 0, msgBytes, headerBytes.length, this.body.length);

        return msgBytes;
    }

    public byte[] getBody() {
        return body;
    }
}
