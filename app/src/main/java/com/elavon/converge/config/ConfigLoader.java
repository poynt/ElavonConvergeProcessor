package com.elavon.converge.config;

import android.content.res.Resources;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.elavon.converge.R;
import com.elavon.converge.util.FileUtil;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ConfigLoader {

    private static final String TAG = ConfigLoader.class.getSimpleName();
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
        String credentialText = null;
        switch (environment) {
            case LIVE:
                configResource = R.raw.config_live;
                break;
            case TEST:
            default:
                configResource = R.raw.config_test;
                File f = new File(Environment.getExternalStorageDirectory() + "/credential.json");
                if (f.exists()) {
                    Log.i(TAG, "Found custom credential file in sdcard - using it for testing");
                    try {
                        credentialText = FileUtil.readFile(new FileInputStream(f));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.i(TAG, "Failed to load credentials from sdcard - falling back to resource file");
                        credentialText = FileUtil.readFile(resources.openRawResource(R.raw.credential));
                    }
                } else {
                    Log.i(TAG, "Loading credentials from resource file");
                    credentialText = FileUtil.readFile(resources.openRawResource(R.raw.credential));
                }
                break;
        }

        final String configText = FileUtil.readFile(resources.openRawResource(configResource));

        // check if we have a credential file in /sdcard - if so use it for testing otherwise use the bundled credential
        // TODO - remove this logging before going live
        Log.i(TAG, credentialText);

        final Config config = gson.fromJson(configText, Config.class);
        if(!TextUtils.isEmpty(credentialText)) {
            final Config.Credential credential = gson.fromJson(credentialText, Config.Credential.class);
            config.setCredential(credential);
        }
        return config;
    }
}
