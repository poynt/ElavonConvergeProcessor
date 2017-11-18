package com.elavon.converge;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;

import co.poynt.os.model.Intents;
import co.poynt.os.services.v1.IPoyntConfigurationService;
import co.poynt.os.services.v1.IPoyntConfigurationUpdateListener;
import fr.devnied.bitlib.BytesUtils;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private final byte MODE_MODIFY = (byte) 0x00;
    private final byte INTERFACE_CT = (byte) 0x04;
    private final byte INTERFACE_CL = (byte) 0x02;
    private final byte INTERFACE_MSR = (byte) 0x01;


    private IPoyntConfigurationService poyntConfigurationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button setTrackFormats = (Button) findViewById(R.id.setTrackFormats);
        setTrackFormats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte trackDataFormat = (byte) 0x02;
//                setTrackDataFormat(trackDataFormat);
                setTrackDataFormat(INTERFACE_MSR);
                setTrackDataFormat(INTERFACE_CT);
                setTrackDataFormat(INTERFACE_CL);
            }
        });
    }

    public void setTrackDataFormat(byte interfaceType) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // set the sentinals
            byte[] tag = BytesUtils.fromString("DFDB");
            byte trackDataFormat = (byte) 0x02; // SS, ER and LRC included
            baos.write(tag);
            baos.write((byte) 0x01); // value length
            baos.write(trackDataFormat); // value

            // set track data to ascii
            byte[] trackFormatTag = BytesUtils.fromString("1F8133");
            baos.write(trackFormatTag);
            baos.write((byte) 0x01); // length
            baos.write((byte) 0x01);

            poyntConfigurationService.setTerminalConfiguration(MODE_MODIFY, interfaceType, baos.toByteArray(),
                    new IPoyntConfigurationUpdateListener.Stub() {

                        @Override
                        public void onSuccess() throws RemoteException {
                            Log.i(TAG, "TrackDataFormat update success");
                        }

                        @Override
                        public void onFailure() throws RemoteException {
                            Log.i(TAG, "TrackDataFormat update fail");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Class for interacting with the Configuration Service
     */
    private ServiceConnection poyntConfigurationServiceConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "IPoyntConfigurationService is now connected");
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            poyntConfigurationService = IPoyntConfigurationService.Stub.asInterface(service);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "IPoyntConfigurationService has unexpectedly disconnected");
            poyntConfigurationService = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        ComponentName COMPONENT_POYNT_CONFIGURATION_SERVICE = new ComponentName("co.poynt.services", "co.poynt.services.PoyntConfigurationService");
        bindService(Intents.getComponentIntent(COMPONENT_POYNT_CONFIGURATION_SERVICE),
                poyntConfigurationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(poyntConfigurationServiceConnection);
    }
}
