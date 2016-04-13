package com.zm.request;

import com.zm.rmi.Exists;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by zhangmin on 2016/4/12.
 */
public class FileExists extends AbstractJavaSamplerClient {
    private static final Logger LOG = LoggingManager.getLoggerForClass();
    private String[] queryTypes = {"文件是否存在", "目录是否存在", "目录下是否有文件", "文件列表"};
    private String samplerData = "";

    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        params.addArgument("rmiServer", "127.0.0.1");
        params.addArgument("type", "0:文件存在/1:目录存在/2:目录下有文件/3:列出文件列表");
        params.addArgument("filePath", "");
        return params;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult sr = new SampleResult();
        String rmiServer = javaSamplerContext.getParameter("rmiServer");
        int type = 0;
        try{
            type = Integer.parseInt(javaSamplerContext.getParameter("type"));
        }catch (Exception e){
            sr.setResponseMessage("类型错误，应为0、1、2或3");
            sr.setSuccessful(false);
            return sr;
        }
        if(type > 3 || type < 0){
            sr.setResponseMessage("类型错误，应为0、1、2或3");
            sr.setSuccessful(false);
            return sr;
        }
        String filePath = javaSamplerContext.getParameter("filePath");

        sr.setSampleLabel("请求查看" + queryTypes[type]);
        samplerData = "请求查看" + filePath + queryTypes[type];
        sr.setSamplerData(samplerData);
        if(LOG.isDebugEnabled()) {
            System.out.println(samplerData);
        }

        sr.sampleStart();
        Exists exists = null;
        String  result = "false";
        try {
            exists = (Exists) Naming.lookup("rmi://" + rmiServer + "/exists");
            switch (type){
                case 0:
                    result = String.valueOf(exists.fileExist(filePath));
                    break;
                case 1:
                    result = String.valueOf(exists.directoryExist(filePath));
                    break;
                case 2:
                    result = String.valueOf(exists.directoryHasFiles(filePath));
                    break;
                case 3:
                    result = exists.listDir(filePath);
                    break;
            }
            sr.setResponseData(result, null);
            sr.setDataEncoding(SampleResult.TEXT);
            sr.setSuccessful(true);
            sr.sampleEnd();
            if(LOG.isDebugEnabled()) {
                System.out.println(result);
            }
        } catch (Exception e) {
            sr.setResponseMessage("远程rmi服务器连接错误");
            sr.setSuccessful(false);
        }
        return sr;
    }
}
