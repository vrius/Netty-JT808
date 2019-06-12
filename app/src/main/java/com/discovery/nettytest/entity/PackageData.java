package com.discovery.nettytest.entity;



import java.io.Serializable;

/**
 * @author ruanwenjiang
 * @date 18-12-26 上午9:42
 * <p>
 * kafka通用消息实体,需要对这个类进行序列化,可采用jdk序列化,protobuf序列化等
 */


public class PackageData implements Serializable {
    /**
     * 消息id,用于标识当前消息的操作类型
     */
    private short msgId;

    /**
     * 消息来源(用于判断消息发起方，解决消息响应以及回复同一个msgid的问题)
     * 由于目前使用的是消息头中的1个bit表示，理论上只允许设置0跟1两个值，如需拓展请联系爱国诗人李白
     */
    private short source;

    /**
     * 加密方式，在消息体属性中使用3个bit表示
     */
    private short encryp;

    /**
     * 设备标识
     */
    private String target;

    /**
     * 消息体
     */
    private byte[] body;

    /**
     * 消息流水号
     */
    private short msgNum;

    public short getMsgId() {
        return msgId;
    }

    public void setMsgId(short msgId) {
        this.msgId = msgId;
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

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public short getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(short msgNum) {
        this.msgNum = msgNum;
    }

    @Override
    public String toString() {
        return "PackageData{" +
                "msgId=" + msgId +
                ", source=" + source +
                ", encryp=" + encryp +
                ", target='" + target + '\'' +
                ", body=" + body +
                ", msgNum=" + msgNum +
                '}';
    }
}
