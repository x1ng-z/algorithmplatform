package hs.algorithmplatform.pydriver.command;


import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/2/16 15:25
 */

public enum CommandImp implements Command {

    /**
     * 发送计算结果
     */
    RESULT(0x01),

    /**
     * 发送参数
     */
    PARAM(0x02),

    /**
     * 发送心跳
     */
    HEART(0x03),
    ACK(0x04),
    STOP(0x05);


    public Logger logger = LoggerFactory.getLogger(CommandImp.class);
    private byte command;
    private long nodeid;

    private CommandImp(int main_command) {
        this.command = (byte) (main_command & 0xff);// (new Integer(main_command).byteValue());
    }

    @Override
    public String toString() {
        return super.toString() + command;
    }

    public byte getCommand() {
        return command;
    }


    public long getNodeid() {
        return nodeid;
    }

    public void setNodeid(long nodeid) {
        this.nodeid = nodeid;
    }

    @Override
    public JSONObject analye(byte[] context) {

        byte[] paramercontext = Arrays.copyOfRange(context, 15, context.length);
        try {
            String str = new String(paramercontext, "UTF-8");
//            logger.info(str);
            try {
                return (JSONObject) JSONObject.parseObject(str);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public byte[] build(byte[] context, long nodeid) {
        int length = context.length + 15;
        byte[] result = new byte[length];
        //header
        result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
        result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
        //command
        result[2] = command;
        //nodeid
        result[3] = (byte) ((nodeid >> 56) & 0xff);
        result[4] = (byte) ((nodeid >> 48) & 0xff);
        result[5] = (byte) ((nodeid >> 40) & 0xff);
        result[6] = (byte) ((nodeid >> 32) & 0xff);

        result[7] = (byte) ((nodeid >> 24) & 0xff);
        result[8] = (byte) ((nodeid >> 16) & 0xff);
        result[9] = (byte) ((nodeid >> 8) & 0xff);
        result[10] = (byte) ((nodeid >> 0) & 0xff);

        //context length
        result[11] = (byte) ((context.length >> 24) & 0xff);
        result[12] = (byte) ((context.length >> 16) & 0xff);
        result[13] = (byte) ((context.length >> 8) & 0xff);
        result[14] = (byte) ((context.length >> 0) & 0xff);

        for (int index = 15; index < length; index++) {
            result[index] = context[index - 15];
        }
        return result;
    }

    @Override
    public boolean valid(byte[] context) {
        //头校验
        if (context.length <= 15) {
            return false;
        }
        //header check
        if ((0x88 != context[0]) && (0x18 != context[1])) {
            return false;
        }
        if (command != context[2]) {
            return false;
        }
        byte[] paramercontext = Arrays.copyOfRange(context, 15, context.length);
        try {
            String str = new String(paramercontext, "UTF-8");
                logger.info(str);
            try {
                JSONObject.parseObject(str);
                return true;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return false;

    }
}
