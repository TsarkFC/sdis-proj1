package messages;

// <Version> DELETE <SenderId> <FileId> <CRLF><CRLF>
public class Delete extends Message{
    public Delete(Double version, Integer senderId, String fileId) {
        super(version, "DELETE", senderId, fileId, null, null, null);
    }

    @Override
    public byte[] getMsgBytes() {
        return null;
    }
}
