package messages;

// <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
public class PutChunk extends MsgWithChunk {

    final int REP_DGR_IDX = 5;
    final int BODY_IDX = 6;
    private final Integer replicationDeg;
    private final byte[] body;

    public PutChunk(Double version, Integer senderId, String fileId, Integer chunkNo,
                    Integer replicationDeg, byte[] body) {
        super(version, senderId,fileId,chunkNo);
        this.replicationDeg = replicationDeg;
        this.body = body;
    }

    public PutChunk(String message) {
        super(message);
        this.replicationDeg = Integer.parseInt(tokens[REP_DGR_IDX]);
        //Verificar se esta o CRLF
        this.body = tokens[BODY_IDX].substring(4).getBytes();
    }

    @Override
    public String getMsgType() {
        return "PUTCHUNK";
    }

    @Override
    protected String getChildString() {
        return String.format("%d %s",this.replicationDeg, getDoubleCRLF());
    }

    @Override
    public int getNumberArguments() {
        return 7;
    }

    public void printMsg() {
        super.printMsg();
        System.out.println("Rep dgr: " + this.replicationDeg);
        System.out.println("Body: " + new String(this.body));
    }

    //TODO Há alguma razao para nao converter a String toda para bytes?
    @Override
    public byte[] getBytes() {
        //<Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
        String header = String.format("%s %s %d %s %d %d %s", this.version, getMsgType(), this.senderId,
                this.fileId, this.chunkNo, this.replicationDeg, getDoubleCRLF());
        byte[] headerBytes = header.getBytes();

        // create a destination array that is the size of the two arrays
        byte[] msgBytes = new byte[headerBytes.length + this.body.length];

        // copy headerBytes into start of msgBytes (from pos 0, copy headerBytes.length bytes)
        System.arraycopy(headerBytes, 0, msgBytes, 0, headerBytes.length);

        // copy this.body into end of msgBytes (from pos headerBytes.length, copy this.body.length bytes)
        System.arraycopy(this.body, 0, msgBytes, headerBytes.length, this.body.length);

        return msgBytes;
    }


    public Integer getReplicationDeg() {
        return replicationDeg;
    }

    public byte[] getBody() {
        return body;
    }

}
