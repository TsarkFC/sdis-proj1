package messages;

// <Version> REMOVED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class Removed extends Message{
    public Removed(Double version, Integer senderId, String fileId, Integer chunkNo) {
        super(version, "REMOVED", senderId, fileId, chunkNo, null, null);
    }

    @Override
    public byte[] getMsgBytes() {
        return null;
    }
}
