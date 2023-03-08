package com.lazywhatsapreader.pogo;

public class Message {
    private int slNo;
    private String msg1;
    private String msg2;
    private String msg3;
    private String msg4;
    private String time;
    private int open;

    public Message() {
        // TODO Auto-generated constructor stub
    }

    public Message(String msg1, String msg2, String msg3, String msg4, String time, int open) {
        this.msg1 = msg1;
        this.msg2 = msg2;
        this.msg3 = msg3;
        this.msg4 = msg4;
        this.time = time;
        this.open = open;

    }


    public String getMsg4() {
        return msg4;
    }

    public void setMsg4(String msg4) {
        this.msg4 = msg4;
    }

    public int getSlNo() {
        return slNo;
    }

    public void setSlNo(int slNo) {
        this.slNo = slNo;
    }

    public String getMsg1() {
        return msg1;
    }

    public void setMsg1(String msg1) {
        this.msg1 = msg1;
    }

    public String getMsg2() {
        return msg2;
    }

    public void setMsg2(String msg2) {
        this.msg2 = msg2;
    }

    public String getMsg3() {
        return msg3;
    }

    public void setMsg3(String msg3) {
        this.msg3 = msg3;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }


}
