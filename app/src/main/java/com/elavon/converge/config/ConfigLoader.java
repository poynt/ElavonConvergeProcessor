package com.elavon.converge.config;

import android.content.res.Resources;

import com.elavon.converge.R;
import com.elavon.converge.exception.AppInitException;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConfigLoader {

    private final Resources resources;
    private final Config.Environment environment;

    public ConfigLoader(final Resources resources, final Config.Environment environment) {
        this.resources = resources;
        this.environment = environment;
    }

    public Config load() {
        final int config;
        switch (environment) {
            case LIVE:
                config = R.raw.config_live;
                break;
            case TEST:
            default:
                config = R.raw.config_test;
                break;
        }

        final String configText = readFile(resources.openRawResource(config));
        return new Gson().fromJson(configText, Config.class);
    }

    private String readFile(final InputStream inputStream) {
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
            throw new AppInitException("Failed to read config file");
        }
        return outputStream.toString();
    }
}
