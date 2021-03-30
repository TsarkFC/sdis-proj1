package messages;

// <Version> GETCHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class GetChunk extends MsgWithChunk {

    public GetChunk(Double version, Integer senderId, String fileId, Integer chunkNo) {
        super(version, senderId, fileId, chunkNo);
    }

    @Override
    public String getMsgType() {
        return "GETCHUNK";
    }

    @Override
    protected String getChildString() {
        return getDoubleCRLF();
    }

    @Override
    public int getNumberArguments() {
        return 6;
    }

    @Override
    public byte[] getBytes() {
        return getMsgString().getBytes();
    }
}
