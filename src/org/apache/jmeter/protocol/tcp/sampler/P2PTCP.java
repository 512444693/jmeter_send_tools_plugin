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
import java.util.IllegalFormatException;

import com.zm.data.BaseType;
import com.zm.mgr.DataMgr;
import com.zm.mgr.UI;
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
        ArrayList<BaseType> dataList = UI.strToDataList(string);
        DataMgr dataMgr = new DataMgr(dataList);
        return dataMgr.encode();
    }

    /**
     * Input (hex) string is converted to binary and written to the output stream.
     * @param os output stream
     * @param hexEncodedBinary hex-encoded binary
     */
    @Override
    public void write(OutputStream os, String hexEncodedBinary) throws IOException{
        try{
            os.write(stringToP2PTCP(hexEncodedBinary));
        }catch (Exception e ){
            JOptionPane.showMessageDialog(null, e.getMessage(), "P2P消息", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException(e.getMessage());
        }

        os.flush();
        if(log.isDebugEnabled()) {
            log.debug("Wrote: " + hexEncodedBinary);
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
            while ((x = is.read(buffer)) > -1) {
                w.write(buffer, 0, x);
                if (useEolByte && (buffer[x - 1] == eolByte)) {
                    break;
                }
            }

            IOUtils.closeQuietly(w); // For completeness
            final String hexString = JOrphanUtils.baToHexString(w.toByteArray());
            if(log.isDebugEnabled()) {
                log.debug("Read: " + w.size() + "\n" + hexString);
            }
            return hexString;
        } catch (IOException e) {
            throw new ReadException("", e, JOrphanUtils.baToHexString(w.toByteArray()));
        }
    }

}
