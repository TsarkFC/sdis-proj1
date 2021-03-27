package messages;

public class CoordMessage {
    private String msg;

    public CoordMessage(String str){
        this.msg=str;
    }
    public CoordMessage(){
        this.msg="";
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String str) {
        this.msg=str;
    }
}
