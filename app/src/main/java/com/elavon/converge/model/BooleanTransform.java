package com.elavon.converge.model;

import org.simpleframework.xml.transform.Transform;

/**
 * Most boolean values in Converge spec take values of "Y" or "N"
 */

public class BooleanTransform implements Transform<Boolean> {
    private final Class type;

    public BooleanTransform(Class type){
        this.type = type;
    }
    @Override
    public Boolean read(String value) throws Exception {
        if ("Y".equalsIgnoreCase(value)){
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }

    @Override
    public String write(Boolean value) throws Exception {
        if (Boolean.TRUE == value) {
            return "Y";
        }else{
            return "N";
        }
    }
}
