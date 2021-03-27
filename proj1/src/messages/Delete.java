package messages;

// <Version> DELETE <SenderId> <FileId> <CRLF><CRLF>
public class Delete implements Message {
    private Double version;
    private Integer senderId;
    private String fileId;

    public Delete(Double version, Integer senderId, String fileId) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
    }

    @Override
    public byte[] getBytes() {
        return null;
    }
}
