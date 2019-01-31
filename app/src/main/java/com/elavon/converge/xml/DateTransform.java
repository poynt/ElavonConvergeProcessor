package com.elavon.converge.xml;

import org.simpleframework.xml.transform.Transform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTransform implements Transform<Date> {

    private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm:ss a";
    // TODO should match merchant's timezone
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("America/New_York");

    @Override
    public Date read(String value) throws Exception {
        final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(TIME_ZONE);
        return dateFormat.parse(value);
    }

    @Override
    public String write(Date value) throws Exception {
        final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(TIME_ZONE);
        return dateFormat.format(value);
    }
}
