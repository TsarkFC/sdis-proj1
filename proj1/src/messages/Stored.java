package messages;

// <Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class Stored extends Message {
    public Stored(Double version, Integer senderId, String fileId, Integer chunkNo) {
        super(version, "STORED", senderId, fileId, chunkNo, null, null);
    }

    public Stored(String message){
        super(message);
    }

    @Override
    public String getMsgString() {
        //Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        //TODO Adicionar CRLF
        return  String.format("%s %s %d %s %d",this.version,this.messageType,this.senderId,this.fileId,this.chunkNo);
    }
}
