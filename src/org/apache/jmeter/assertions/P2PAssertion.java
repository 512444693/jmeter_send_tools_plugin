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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zm.Field.CompareResult;
import com.zm.message.Message;
import com.zm.utils.BU;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
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
            byte[] resultData = BU.hex2Bytes(response.getResponseDataAsString());

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

            //处理保存字段值
            String propertyStr = "";
            if(!(propertyStr = getPropertyTxt().trim()).equals("")){
                String[] pNames = propertyStr.split(",");
                String factString = fact.toString();
                if(pNames.length > 0){
                    JMeterVariables vars = getThreadContext().getVariables();
                    Pattern pattern = null;
                    Matcher matcher = null;
                    for(int i = 0; i < pNames.length; i++){
                        //至少含一个非空白字符，可处理数组
                        pattern = Pattern.compile(pNames[i] + "[0-9]*=(\\S+)\r\n",  Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);
                        matcher = pattern.matcher(factString);
                        //循环将数组放入相同的变量名中，制表符分隔
                        String value = "";
                        while(matcher.find()){
                            value += matcher.group(1) + "\t";
                        }
                        if ((value = value.trim()).equals("")) {
                            value = "NOT_FOUND";
                        }
                        vars.put(pNames[i], value);
                    }
                }
            }

            //处理保存body到文件
            if(getSaveBodyToFile()) {
                String samplerName = response.getSampleLabel();
                String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                        .format(new Date(System.currentTimeMillis()));

                Message req = new Message(response.getSamplerData());
                byte[] reqBytesData = processHttpBody(req.encode());

                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(samplerName + ".req." + time));
                out.write(reqBytesData);
                out.close();

                out = new BufferedOutputStream(
                        new FileOutputStream(samplerName + ".req.hex." + time));
                out.write(BU.bytes2Hex(reqBytesData).getBytes());
                out.close();

                byte[] resBytesData = processHttpBody(resultData);

                out = new BufferedOutputStream(new FileOutputStream(samplerName + ".res." + time));
                out.write(resBytesData);
                out.close();

                out = new BufferedOutputStream(new FileOutputStream(samplerName + ".res.hex" + time));
                out.write(BU.bytes2Hex(resBytesData).getBytes());
                out.close();
            }

        }catch (Exception e){
            //e.printStackTrace();
            result.setFailure(true);
            result.setFailureMessage("异常：" + e.getMessage());
        }
        return result;
    }

    public byte[] processHttpBody(byte[] data) {
        int index = BU.findFirst(data, "\r\n\r\n".getBytes());
        if(index != -1) {
            index += 4;
            return BU.subByte(data, index, data.length - index);
        }
        return data;
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

    //供别人调用
    public void setSaveBodyToFile(boolean saveBodyToFile) {
        setProperty(new BooleanProperty("SaveBodyToFile", saveBodyToFile));
    }

    //得到值
    public boolean getSaveBodyToFile() {
        return getPropertyAsBoolean("SaveBodyToFile");
    }

}
