package messages;

// <Version> PUTCHUNK <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
public class PutChunk extends Message {
    public PutChunk(Double version, Integer senderId, String fileId, Integer chunkNo, Integer replicationDeg, byte[] body) {
        super(version, "PUTCHUNK", senderId, fileId, chunkNo, replicationDeg, body);
    }

    public PutChunk(String message) {
        super(message);
    }

    @Override
    public byte[] getMsgBytes() {
        //<Version> <MessageType> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
        String header = String.format("%s %s %d %s %d %d %s ", this.version, this.messageType, this.senderId, this.fileId,
                this.chunkNo, this.replicationDeg, getDoubleCRLF());
        byte[] headerBytes = header.getBytes();

        // create a destination array that is the size of the two arrays
        byte[] msgBytes = new byte[headerBytes.length + this.body.length];

        // copy headerBytes into start of msgBytes (from pos 0, copy headerBytes.length bytes)
        System.arraycopy(headerBytes, 0, msgBytes, 0, headerBytes.length);

        // copy this.body into end of msgBytes (from pos headerBytes.length, copy this.body.length bytes)
        System.arraycopy(this.body, 0, msgBytes, headerBytes.length, this.body.length);

        return msgBytes;
    }
}
