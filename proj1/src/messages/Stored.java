package messages;

// <Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class Stored implements Message {
    final int VERSION_IDX = 0;
    final int MSG_TYPE_IDX = 1;
    final int SENDER_ID_IDX = 2;
    final int FILE_ID_IDX = 3;
    final int CHUNK_NO_IDX = 4;
    final int CR = 0xD;
    final int LF = 0xA;

    private Double version;
    private Integer senderId;
    private String fileId;
    private Integer chunkNo;
    private final String messageType = "STORED";

    public Stored(Double version, Integer senderId, String fileId, Integer chunkNo) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    public Stored(String message) {
        String[] tokens = message.split("\\s+", 7);
        //String[] tokens = message.split(" ", 7);

        if (!tokens[MSG_TYPE_IDX].equals("STORED")) {
            System.out.println("ERROR: building " + tokens[MSG_TYPE_IDX] + " message with STORED constructor!");
        }
        this.version = Double.parseDouble(tokens[VERSION_IDX]);
        this.senderId = Integer.parseInt(tokens[SENDER_ID_IDX]);
        this.fileId = tokens[FILE_ID_IDX];
        this.chunkNo = Integer.parseInt(tokens[CHUNK_NO_IDX]);
    }

    @Override
    public byte[] getBytes() {
        //Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        //TODO Adicionar CRLF
        return String.format("%s %s %d %s %d",
                this.version, this.messageType, this.senderId, this.fileId, this.chunkNo).getBytes();
    }
}
