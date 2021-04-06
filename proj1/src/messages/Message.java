package messages;

public abstract class Message {
    protected static final int VERSION_IDX = 0;
    protected static final int MSG_TYPE_IDX = 1;
    protected static final int SENDER_ID_IDX = 2;
    protected static final int FILE_ID_IDX = 3;
    protected final Double version;
    protected final Integer senderId;
    protected final String fileId;
    protected final int CR = 0xD;
    protected final int LF = 0xA;
    protected String[] tokens;

    public Message(Double version, Integer senderId, String fileId) {
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
    }

    public Message(String message) {
        tokens = message.split("\\s+", getNumberArguments());

        if (!tokens[MSG_TYPE_IDX].equals(getMsgType())) {
            System.out.println("ERROR: building " + tokens[MSG_TYPE_IDX] + " message with "+ getMsgType() + " constructor!");
        }

        this.version = Double.parseDouble(tokens[VERSION_IDX]);
        this.senderId = Integer.parseInt(tokens[SENDER_ID_IDX]);
        this.fileId = tokens[FILE_ID_IDX];
    }

    public String getMsgString(){
        return String.format("%s %s %d %s %s", this.version, getMsgType(), this.senderId,
                this.fileId, getExtraString());
    }

    public abstract String getMsgType();

    protected abstract String getExtraString();


    public abstract int getNumberArguments();

    public void printMsg(){
        System.out.println(getMsgType());
        System.out.println("Version: " + this.version);
        System.out.println("Sender ID: " + this.senderId);
        System.out.println("File ID: " + this.fileId);
    }

    public abstract byte[] getBytes();

    public Double getVersion() {
        return version;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getCRLF() {
        return Integer.toHexString(CR) + Integer.toHexString(LF);
    }

    public String getDoubleCRLF() {
        return getCRLF() + getCRLF();
    }

    public static String getTypeStatic(String msg){
        String[] stringArr = msg.split("\\s+", 4);
        return stringArr[MSG_TYPE_IDX];
    }

}

