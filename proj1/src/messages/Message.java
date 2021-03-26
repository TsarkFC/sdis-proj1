package messages;

/*
- there may be more than one space between fields;
- there may be zero or more spaces after the last field in a line;
- the header always terminates with an empty header line.
I.e. the <CRLF> of the last header line is followed immediately by another <CRLF> without any character,
white spaces included, in between.
*/

import java.nio.charset.StandardCharsets;

// <Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
//Abstract classes nao podem ter construtores entao mudei
public abstract class Message {

    Double version;
    String messageType;
    Integer senderId;
    String fileId;
    Integer chunkNo;
    Integer replicationDeg;
    byte[] body;

    final int VERSION_IDX = 0;
    final int MSG_TYPE_IDX = 1;
    final int SENDER_ID_IDX = 2;
    final int FILE_ID_IDX = 3;
    final int CHUNK_NO_IDX = 4;
    final int REP_DGR_IDX = 5;
    final int BODY_IDX = 6;
    final int CR = 0xD;
    final int LF = 0xA;

    public Message(Double version, String messageType, Integer senderId, String fileId, Integer chunkNo, Integer replicationDeg, byte[] body) {
        this.version = version;
        this.messageType = messageType;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.body = body;
    }

    public Message(String message) {
        String[] tokens = message.split(" ");
        this.version = Double.parseDouble(tokens[VERSION_IDX]);
        this.messageType = tokens[MSG_TYPE_IDX];
        this.senderId = Integer.parseInt(tokens[SENDER_ID_IDX]);
        this.fileId = tokens[FILE_ID_IDX];
        this.chunkNo = Integer.parseInt(tokens[CHUNK_NO_IDX]);
        this.replicationDeg = Integer.parseInt(tokens[REP_DGR_IDX]);
        //TODO CONFIRMAR ISTO
        this.body = tokens[BODY_IDX].getBytes();
    }

    public void printMsg() {
        System.out.println();
        System.out.println();
        System.out.println("Version:" + this.version);
        System.out.println("MSG TYPE: " + this.messageType);
        System.out.println("Sender ID: " + this.senderId);
        System.out.println("File ID: " + this.fileId);
        System.out.println("Chunk No: " + this.chunkNo);
        System.out.println("Rep dgr: " + this.replicationDeg);
    }

    public abstract String getMsgString();

    public Byte[] messageBytes() {
        return new Byte[10];
    }

    public Double getVersion() {
        return version;
    }

    public String getMessageType() {
        return messageType;
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

    public String getCRLF() {
        return Integer.toHexString(CR) + Integer.toHexString(LF);
    }

    public String getDoubleCRLF() {
        return getCRLF() + getCRLF();
    }
}
