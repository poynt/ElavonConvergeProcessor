package com.elavon.converge.xml;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlMapper {

    private final Serializer serializer;

    public XmlMapper() {
        serializer = new Persister(new Matcher() {
            @Override
            public Transform match(Class type) throws Exception {
                if (type.isEnum()) {
                    return new EnumTransform(type);
                } else if (type == Boolean.TYPE) {
                    return new BooleanTransform(type);
                }
                return null;
            }
        });
    }

    public <T> T read(final String xml, final Class<T> clazz) throws Exception {
        final Reader reader = new StringReader(xml);
        return serializer.read(clazz, reader, false);
    }

    public String write(final Object o) throws Exception {
        final StringWriter writer = new StringWriter();
        serializer.write(o, writer);
        return writer.toString();
    }
}
