package com.elavon.converge;

import com.elavon.converge.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.BeforeClass;

public abstract class BaseTest {

    private static XmlMapper xmlMapper;
    private static Gson gson;

    @BeforeClass
    public static void beforeClass() {
        xmlMapper = new XmlMapper();
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
