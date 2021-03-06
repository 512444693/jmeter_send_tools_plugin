package kg.apc.jmeter.samplers;

import com.zm.message.Message;
import com.zm.utils.BU;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

import javax.swing.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by zhangmin on 2015/11/9.
 */
public class P2PUDP implements UDPTrafficDecoder {
    private static final Logger log = LoggingManager.getLoggerForClass();

    @Override
    public ByteBuffer encode(String s) {
        byte[] data = new byte[0];
        try{
            Message message = new Message(s);
            data = message.encode();
        }catch (Exception e ){
            JOptionPane.showMessageDialog(null, e.getMessage(), "P2P消息【发包】", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException(e.getMessage());
        }

        if(log.isDebugEnabled()){
            log.debug("Write: \n" + s);
            log.debug("Write(hex): \n" + BU.bytes2Hex(data));
            System.out.println("============发送UDP==========" + new Date() + "==================");
            System.out.println();
            System.out.println(BU.bytes2HexGoodLook(data));
            System.out.println();
        }

        return ByteBuffer.wrap(data);
    }

    @Override
    public byte[] decode(byte[] bytes) {
        if(log.isDebugEnabled()){
            log.debug("Read: " + bytes.length + "(size)\n" + BU.bytes2Hex(bytes));
            System.out.println("============接收UDP=========="+ new Date().toString()+"==================");
            System.out.println();
            System.out.println(BU.bytes2HexGoodLook(bytes));
            System.out.println();
        }

        return JOrphanUtils.baToHexString(bytes).getBytes();
    }
}
