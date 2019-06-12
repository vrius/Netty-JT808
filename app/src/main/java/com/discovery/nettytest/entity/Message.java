package com.discovery.nettytest.entity;


/**
 * @author ruanwenjiang
 * @date 18-12-14 上午11:17
 * 用于分包聚合信息中转
 */

public class Message {
    /**
     * 消息id
     */
    private short msgId;
    /**
     * 消息流水号
     */
    private short msgNum;
    /**
     * 消息体长度
     */
    private short bodyLen;
    /**
     * 消息来源
     */
    private short source;
    /**
     * 消息体加密方式
     */
    private short encryp;
    /**
     * 消息目标
     */
    private String target;
    /**
     * 是否分包
     */
    private boolean isPkg;
    /**
     * 包序号(当isPkg为true)
     */
    private int pkgNum;
    /**
     * 包数量(当isPkg为true)
     */
    private int totalPkg;
    /**
     * 消息体
     */
    private byte[] body;

    public short getMsgId() {
        return msgId;
    }

    public void setMsgId(short msgId) {
        this.msgId = msgId;
    }


    public short getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(short msgNum) {
        this.msgNum = msgNum;
    }

    public short getBodyLen() {
        return bodyLen;
    }

    public void setBodyLen(short bodyLen) {
        this.bodyLen = bodyLen;
    }

    public short getSource() {
        return source;
    }

    public void setSource(short source) {
        this.source = source;
    }

    public short getEncryp() {
        return encryp;
    }

    public void setEncryp(short encryp) {
        this.encryp = encryp;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isPkg() {
        return isPkg;
    }

    public void setPkg(boolean pkg) {
        isPkg = pkg;
    }

    public int getPkgNum() {
        return pkgNum;
    }

    public void setPkgNum(int pkgNum) {
        this.pkgNum = pkgNum;
    }

    public int getTotalPkg() {
        return totalPkg;
    }

    public void setTotalPkg(int totalPkg) {
        this.totalPkg = totalPkg;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
