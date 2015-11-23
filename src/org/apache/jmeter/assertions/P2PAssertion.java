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

/**
 * MD5HexAssertion class creates an MD5 checksum from the response <br/>
 * and matches it with the MD5 hex provided.
 * The assertion will fail when the expected hex is different from the <br/>
 * one calculated from the response OR when the expected hex is left empty.
 *
 */
package org.apache.jmeter.assertions;

import java.io.Serializable;

import com.zm.Field.CompareResult;
import com.zm.message.Message;
import com.zm.utils.BU;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class P2PAssertion extends AbstractTestElement implements Serializable, Assertion {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    /** Key for storing assertion-informations in the jmx-file. */
    private static final String P2P_KEY = "P2PAssertion.txt";

    /*
     * @param response @return
     */
    @Override
    public AssertionResult getResult(SampleResult response) {


        AssertionResult result = new AssertionResult(getName());
        result.setFailure(false);
        try{
            byte[] resultData = BU.hex2Bytes(new String(response.getResponseData()));

            if (resultData.length == 0) {
                result.setError(false);
                result.setFailure(true);
                result.setFailureMessage("Response was null");
                return result;
            }

            // no point in checking if we don't have anything to compare against
            if (getP2PTxt().replaceAll("\\s", "").equals("")) {
                result.setError(false);
                result.setFailure(true);
                result.setFailureMessage("没有设置预期结果");
                return result;
            }
    /*
            String md5Result = baMD5Hex(resultData);

            // String md5Result = DigestUtils.md5Hex(resultData);

            if (!md5Result.equalsIgnoreCase(getP2PTxt())) {
                result.setFailure(true);

                Object[] arguments = { md5Result, getP2PTxt() };
                String message = MessageFormat.format(JMeterUtils.getResString("P2P_assertion_failure"), arguments); // $NON-NLS-1$
                result.setFailureMessage(message);

            }
            */

            Message expect = new Message(getP2PTxt());
            expect.encode();

            Message fact = new Message(getP2PTxt(), resultData);
            fact.decode();

            CompareResult compareResult = expect.compare(fact);
            if(!compareResult.equal){
                result.setFailure(true);
                result.setFailureMessage(compareResult.msg);
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }
        return result;
    }

    public void setP2PTxt(String hex) {
        setProperty(new StringProperty(P2PAssertion.P2P_KEY, hex));
    }

    public String getP2PTxt() {
        return getPropertyAsString(P2PAssertion.P2P_KEY);
    }

    // package protected so can be accessed by test class
    /*
    static String baMD5Hex(byte ba[]) {
        byte[] md5Result = {};

        try {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            md5Result = md.digest(ba);
        } catch (NoSuchAlgorithmException e) {
            log.error("", e);
        }
        return JOrphanUtils.baToHexString(md5Result);
    }*/
}
