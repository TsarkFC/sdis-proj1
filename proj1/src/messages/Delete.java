package messages;

// <Version> DELETE <SenderId> <FileId> <CRLF><CRLF>
public class Delete extends Message {

    public Delete(Double version, Integer senderId, String fileId) {
        super(version,senderId,fileId);
    }

    @Override
    public String getMsgType() {
        return "DELETE";
    }

    @Override
    protected String getExtraString() {
        return getDoubleCRLF();
    }

    @Override
    public int getNumberArguments() {
        return 5;
    }

    @Override
    public byte[] getBytes() {
        return getMsgString().getBytes();
    }
}
