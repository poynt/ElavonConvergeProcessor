package com.elavon.converge.config;

import android.content.res.Resources;

import com.elavon.converge.R;
import com.elavon.converge.util.FileUtil;
import com.google.gson.Gson;

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

        final String configText = FileUtil.readFile(resources.openRawResource(configResource));
        final String credentialText = FileUtil.readFile(resources.openRawResource(R.raw.credential));

        final Config config = gson.fromJson(configText, Config.class);
        final Config.Credential credential = gson.fromJson(credentialText, Config.Credential.class);
        config.setCredential(credential);
        return config;
    }
}
