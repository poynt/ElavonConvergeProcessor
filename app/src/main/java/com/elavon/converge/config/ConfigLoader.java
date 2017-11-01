package com.elavon.converge.config;

import android.content.res.Resources;

import com.elavon.converge.R;
import com.elavon.converge.exception.AppInitException;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConfigLoader {

    private Gson gson;
    private final Resources resources;
    private final Config.Environment environment;

    public ConfigLoader(final Resources resources, final Config.Environment environment) {
        this.gson = new Gson();
        this.resources = resources;
        this.environment = environment;
    }

    public Config load() {
        final int configResource;
        switch (environment) {
            case LIVE:
                configResource = R.raw.config_live;
                break;
            case TEST:
            default:
                configResource = R.raw.config_test;
                break;
        }

        final String configText = readFile(resources.openRawResource(configResource));
        final String credentialText = readFile(resources.openRawResource(R.raw.credential));

        final Config config = gson.fromJson(configText, Config.class);
        final Config.Credential credential = gson.fromJson(credentialText, Config.Credential.class);
        config.setCredential(credential);
        return config;
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
