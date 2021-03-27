package messages;

// <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
public class PutChunk implements Message {
    final int VERSION_IDX = 0;
    final int MSG_TYPE_IDX = 1;
    final int SENDER_ID_IDX = 2;
    final int FILE_ID_IDX = 3;
    final int CHUNK_NO_IDX = 4;
    final int REP_DGR_IDX = 5;
    final int BODY_IDX = 6;
    final int CR = 0xD;
    final int LF = 0xA;

    private final Double version;
    private final Integer senderId;
    private final String fileId;
    private final Integer chunkNo;
    private final Integer replicationDeg;
    private final byte[] body;
    private final String messageType = "PUTCHUNK";

    public PutChunk(Double version, Integer senderId, String fileId, Integer chunkNo,
                    Integer replicationDeg, byte[] body) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.body = body;
    }

    public PutChunk(String message) {
        String[] tokens = message.split("\\s+", 7);
        //String[] tokens = message.split(" ", 7);

        if (!tokens[MSG_TYPE_IDX].equals("PUTCHUNK")) {
            System.out.println("ERROR: building " + tokens[MSG_TYPE_IDX] + " message with PUTCHUNK constructor!");
        }
        this.version = Double.parseDouble(tokens[VERSION_IDX]);
        this.senderId = Integer.parseInt(tokens[SENDER_ID_IDX]);
        this.fileId = tokens[FILE_ID_IDX];
        this.chunkNo = Integer.parseInt(tokens[CHUNK_NO_IDX]);
        this.replicationDeg = Integer.parseInt(tokens[REP_DGR_IDX]);

        //Verificar se esta o CRLF
        this.body = tokens[BODY_IDX].substring(4).getBytes();
        //this.body = tokens[BODY_IDX].getBytes();
        //printMsg();
    }

    public void printMsg() {
        System.out.println("PUTCHUNK");
        System.out.println("Version: " + this.version);
        System.out.println("Sender ID: " + this.senderId);
        System.out.println("File ID: " + this.fileId);
        System.out.println("Chunk No: " + this.chunkNo);
        System.out.println("Rep dgr: " + this.replicationDeg);
        System.out.println("Body: " + new String(this.body));
    }

    @Override
    public byte[] getBytes() {
        //<Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
        String header = String.format("%s %s %d %s %d %d %s", this.version, this.messageType, this.senderId,
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

    public String getCRLF() {
        return Integer.toHexString(CR) + Integer.toHexString(LF);
    }

    public String getDoubleCRLF() {
        return getCRLF() + getCRLF();
    }

    public Double getVersion() {
        return version;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public Integer getChunkNo() {
        return chunkNo;
    }

    public Integer getReplicationDeg() {
        return replicationDeg;
    }

    public byte[] getBody() {
        return body;
    }

}
