package com.elavon.converge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;

public abstract class BaseTest {

    private Gson gson;

    @Before
    public void beforeClass() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    protected void print(Object o) {
        System.out.println(o);
    }

    protected void printJson(Object o) {
        System.out.println(gson.toJson(o));
    }
}
