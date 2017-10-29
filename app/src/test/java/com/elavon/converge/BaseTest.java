package com.elavon.converge;

import com.elavon.converge.config.Config;
import com.elavon.converge.inject.AppModule;
import com.elavon.converge.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.BeforeClass;

public abstract class BaseTest {

    protected static AppModule appModule;
    private static XmlMapper xmlMapper;
    private static Gson gson;

    @BeforeClass
    public static void beforeClass() {
        // TODO load from resoruce file
        final Config.ConvergeClient convergeClient = new Config.ConvergeClient();
        convergeClient.setHost("https://api.demo.convergepay.com/VirtualMerchantDemo/processxml.do");
        convergeClient.setConnectTimeoutMs(10000);
        convergeClient.setReadTimeoutMs(30000);
        convergeClient.setWriteTimeoutMs(30000);
        final Config config = new Config();
        config.setConvergeClient(convergeClient);

        appModule = new AppModule(null, config);

        xmlMapper = appModule.provideXmlMapper();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    protected void print(Object o) {
        System.out.println(o);
    }

    protected void printXml(Object o) {
        try {
            System.out.println(xmlMapper.write(o));
        } catch (Exception e) {
            throw new RuntimeException("Failed to write to xml", e);
        }
    }

    protected void printJson(Object o) {
        System.out.println(gson.toJson(o));
    }
}
