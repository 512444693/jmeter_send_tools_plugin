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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zm.Field.CompareResult;
import com.zm.message.Message;
import com.zm.utils.BU;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
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

            if(log.isDebugEnabled()) {
                System.out.println(fact);
                if(fact.dataCntLeftToDecode() > 0)
                    System.out.println("还剩" + fact.dataCntLeftToDecode() + "字节数据没有解码");
            }

            if(fact.dataCntLeftToDecode() > 0){
                result.setFailure(true);
                result.setFailureMessage("还剩" + fact.dataCntLeftToDecode() + "字节数据没有解码");
                return result;
            }

            CompareResult compareResult = expect.compare(fact);
            if(!compareResult.equal){
                String expectStr = "\r\n================预期================\r\n" + expect;
                String factStr = "\r\n================实际================\r\n" + fact;
                result.setFailure(true);
                result.setFailureMessage(compareResult.msg + expectStr + factStr);
                return result;
            }

            String propertyStr = "";
            if(!(propertyStr = getPropertyTxt().trim()).equals("")){
                String[] pNames = propertyStr.split(",");
                if(pNames.length > 0){
                    JMeterContext context = getThreadContext();
                    JMeterVariables vars = context.getVariables();
                    Pattern pattern = null;
                    Matcher matcher = null;
                    for(int i = 0; i < pNames.length; i++){
                        //至少含一个非空白字符
                        pattern = Pattern.compile(pNames[i] + "=(\\S+)\r\n",  Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
                        matcher = pattern.matcher(fact.toString());
                        if(matcher.find()){
                            vars.put(pNames[i], matcher.group(1));
                        }else{
                            vars.put(pNames[i], "NOT_FOUND");
                        }
                    }
                }
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

    public void setPropertyTxt(String value){
        setProperty(new StringProperty("Property.txt", value));
    }

    public String getPropertyTxt() {
        return getPropertyAsString("Property.txt");
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
