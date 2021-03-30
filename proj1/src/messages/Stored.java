package messages;

// <Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class Stored extends MsgWithChunk {

    public Stored(Double version, Integer senderId, String fileId, Integer chunkNo) {
        super(version,senderId,fileId,chunkNo);
    }

    public Stored(String message) {
        super(message);
    }

    @Override
    public String getMsgType() {
        return "STORED";
    }

    @Override
    protected String getChildString() {
        return "";
    }

    //TODO, Estava 7 aqui entao nao mudei, é mm 7?
    @Override
    public int getNumberArguments() {
        return 7;
    }

    @Override
    public byte[] getBytes() {
        //Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        //TODO Adicionar CRLF
        return getMsgString().getBytes();
    }

    public void printMsg() {
        super.printMsg();
    }
}
