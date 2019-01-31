package com.elavon.converge.xml;

import org.simpleframework.xml.transform.Transform;

/**
 * https://github.com/FasterXML/jackson-dataformat-xml#android-quirks
 */
public class EnumTransform implements Transform<Enum> {
    private final Class type;

    public EnumTransform(Class type) {
        this.type = type;
    }

    @Override
    public Enum read(String value) throws Exception {
        for (Object o : type.getEnumConstants()) {
            if (o.toString().equals(value)) {
                return (Enum) o;
            }
        }
        return null;
    }

    @Override
    public String write(Enum value) throws Exception {
        return value.toString();
    }
}
