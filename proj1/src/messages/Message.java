package messages;

/*
- there may be more than one space between fields;
- there may be zero or more spaces after the last field in a line;
- the header always terminates with an empty header line.
I.e. the <CRLF> of the last header line is followed immediately by another <CRLF> without any character,
white spaces included, in between.
*/

// <Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
abstract class Message {
    Double version;
    String messageType;
    Integer senderId;
    String fileId;
    Integer chunkNo;
    Integer replicationDeg;
    byte[] body;

    public Message(Double version, String messageType, Integer senderId, String fileId, Integer chunkNo, Integer replicationDeg, byte[] body) {
        this.version = version;
        this.messageType = messageType;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.body = body;
    }

    public String getMsgString(){
        //<Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
        return  String.format("%s %s %d %s %d %d %s",this.version,this.messageType,this.senderId,this.fileId,this.chunkNo,this.replicationDeg,this.body);
    }


    public Byte[] messageBytes() {
        return new Byte[10];
    }
}
