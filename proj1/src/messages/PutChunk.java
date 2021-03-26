package messages;

// <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
public class PutChunk extends Message{
    public PutChunk(Double version, Integer senderId, String fileId, Integer chunkNo, Integer replicationDeg, byte[] body) {
        super(version, "PUTCHUNK", senderId, fileId, chunkNo, replicationDeg, body);
    }
    public PutChunk(String message){
        super(message);
        System.out.println("after putchunk");
    }

    @Override
    public String getMsgString() {
        //<Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
        return  String.format("%s %s %d %s %d %d %s %s",this.version,this.messageType,this.senderId,this.fileId,this.chunkNo,this.replicationDeg,getDoubleCRLF(),this.body);
    }
}
