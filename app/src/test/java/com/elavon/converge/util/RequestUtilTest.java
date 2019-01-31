package com.elavon.converge.util;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class RequestUtilTest {

    @Before
    public void setUp(){

    }

    @Test
    public void testGetDeviceUserValue(){
        assertEquals("PoyntUser", RequestUtil.getDeviceUserValue(null, null, null));
        assertEquals("Stub", RequestUtil.getDeviceUserValue("Stub", null, null));
        assertEquals("Stub", RequestUtil.getDeviceUserValue(null, "Stub", null));
        assertEquals("Stub", RequestUtil.getDeviceUserValue(null, null, "Stub"));
        assertEquals("Stub-User", RequestUtil.getDeviceUserValue("User", null, "Stub"));
        assertEquals("Stub-Converge-User", RequestUtil.getDeviceUserValue("Converge","User", "Stub"));
    }
}
