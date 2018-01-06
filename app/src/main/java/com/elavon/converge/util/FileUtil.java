package com.elavon.converge.util;

import com.elavon.converge.exception.AppInitException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    public static String readFile(final InputStream inputStream) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        final byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            throw new AppInitException("Failed to read file");
        }
        return outputStream.toString();
    }
}
