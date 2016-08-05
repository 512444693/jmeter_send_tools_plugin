/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/*
 * TCP Sampler Client implementation which reads and writes binary data.
 *
 * Input/Output strings are passed as hex-encoded binary strings.
 *
 */
package org.apache.jmeter.protocol.tcp.sampler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import com.zm.message.Message;
import com.zm.utils.BU;
import org.apache.commons.io.IOUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

import javax.swing.*;

/**
 * TCPClient implementation.
 * Reads data until the defined EOM byte is reached.
 * If there is no EOM byte defined, then reads until
 * the end of the stream is reached.
 * The EOM byte is defined by the property "tcp.BinaryTCPClient.eomByte".
 *
 * Input data is assumed to be in hex, and is converted to binary
 */
public class P2PTCP extends AbstractTCPClient {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final int eomInt = JMeterUtils.getPropDefault("tcp.BinaryTCPClient.eomByte", 1000); // $NON_NLS-1$

    public P2PTCP() {
        super();
        setEolByte(eomInt);
        if (useEolByte) {
            log.info("Using eomByte=" + eolByte);
        }
    }

    /**
     * Convert hex string to binary byte array.
     *
     * @param hexEncodedBinary - hex-encoded binary string
     * @return Byte array containing binary representation of input hex-encoded string
     * @throws IllegalArgumentException if string is not an even number of hex digits
     */
    public static final byte[] stringToP2PTCP(String string) {
        Message message = new Message(string);
        return message.encode();
    }

    /**
     * Input (hex) string is converted to binary and written to the output stream.
     * @param os output stream
     * @param hexEncodedBinary hex-encoded binary
     */
    @Override
    public void write(OutputStream os, String hexEncodedBinary) throws IOException{
        byte[] data = null;
        try{
            data = stringToP2PTCP(hexEncodedBinary);
            os.write(data);
        }catch (Exception e ){
            JOptionPane.showMessageDialog(null, e.getMessage(), "P2P消息【发包】", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException(e.getMessage());
        }

        os.flush();

        if(log.isDebugEnabled()) {
            log.debug("Write: \n" + hexEncodedBinary);
            log.debug("Write(hex): \n" + BU.bytes2Hex(data));
            System.out.println("============发送TCP==========" + new Date() + "==================");
            System.out.println();
            System.out.println(BU.bytes2HexGoodLook(data));
            System.out.println();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(OutputStream os, InputStream is) {
        throw new UnsupportedOperationException(
                "Method not supported for Length-Prefixed data.");
    }

    /**
     * Reads data until the defined EOM byte is reached.
     * If there is no EOM byte defined, then reads until
     * the end of the stream is reached.
     * Response data is converted to hex-encoded binary
     * @return hex-encoded binary string
     * @throws ReadException when reading fails
     */
    @Override
    public String read(InputStream is) throws ReadException {
        ByteArrayOutputStream w = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int x = 0;
            //while ((x = is.read(buffer)) > -1) {
            if ((x = is.read(buffer)) > -1) {
                w.write(buffer, 0, x);
                /*if (useEolByte && (buffer[x - 1] == eolByte)) {
                    break;
                }*/
            }

            IOUtils.closeQuietly(w); // For completeness
            final String hexString = JOrphanUtils.baToHexString(w.toByteArray());
            if(log.isDebugEnabled()) {
                log.debug("Read: " + w.size() + "(size)\n" + hexString);
                System.out.println("============接收TCP=========="+ new Date().toString()+"==================");
                System.out.println();
                System.out.println(BU.bytes2HexGoodLook(BU.hex2Bytes(hexString)));
                System.out.println();
            }
            return hexString;
        } catch (IOException e) {
            throw new ReadException("", e, JOrphanUtils.baToHexString(w.toByteArray()));
        }
    }

}
