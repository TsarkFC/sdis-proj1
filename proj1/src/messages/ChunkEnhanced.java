package messages;

public class ChunkEnhanced extends MsgWithChunk {
    private int portNumber;
    private static int PORTNUM_IDX = 5;

    public ChunkEnhanced(Double version, Integer senderId, String fileId, Integer chunkNo, int portNumber) {
        super(version, senderId, fileId, chunkNo);
        this.portNumber = portNumber;
    }

    public ChunkEnhanced(String header) {
        super(header);
        this.portNumber = Integer.parseInt(tokens[PORTNUM_IDX]);
    }

    @Override
    public String getMsgType() {
        return "CHUNK";
    }

    @Override
    protected String getChildString() {
        return "";
    }

    @Override
    public int getNumberArguments() {
        return 6;
    }

    @Override
    public byte[] getBytes() {
        //<Version> <MessageType> <SenderId> <FileId> <ChunkNo> <PortNumber> <CRLF>
        String header = String.format("%s %s %d %s %d %d", this.version, getMsgType(), this.senderId,
                this.fileId, this.chunkNo, this.portNumber);
        return addCRLF(header.getBytes());
    }

    public int getPortNumber() {
        return portNumber;
    }
}
