package com.elavon.converge;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.elavon.converge.core.TransactionManager;
import com.elavon.converge.inject.AppComponent;
import com.elavon.converge.inject.AppModule;
import com.elavon.converge.inject.DaggerAppComponent;
import com.elavon.converge.model.ElavonSettleResponse;
import com.elavon.converge.processor.ConvergeCallback;
import com.elavon.converge.util.FileUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.services.v1.IPoyntConfigurationService;
import co.poynt.os.services.v1.IPoyntConfigurationUpdateListener;
import fr.devnied.bitlib.BytesUtils;

public class MainActivity extends Activity implements VirtualTerminalService.VirtualTerminalListener {

    private static final String TAG = "MainActivity";

    private final byte MODE_MODIFY = (byte) 0x00;
    private final byte INTERFACE_CT = (byte) 0x04;
    private final byte INTERFACE_CL = (byte) 0x02;
    private final byte INTERFACE_MSR = (byte) 0x01;

    private static final int BALANCE_INQUIRY_REQUEST = 12345;

    @Inject
    protected TransactionManager transactionManager;
    @Inject
    protected VirtualTerminalService virtualTerminalService;

    private IPoyntConfigurationService poyntConfigurationService;
    private WebView manualEntryWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AppComponent component = DaggerAppComponent.builder().appModule(new AppModule(this.getApplicationContext())).build();
        component.inject(this);

        setContentView(R.layout.activity_main);
        createTrackFormatView();
        createSettleView();
        createManualEntryView();

        Button balaceInquiryBtn = (Button) findViewById(R.id.balanceInquiry);
        balaceInquiryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Payment payment = new Payment();
                payment.setDisableCash(true);
                payment.setDisableOther(true);
                payment.setDisableCheck(true);
                payment.setDisableTip(true);
                payment.setDisableEMVCL(true);
                payment.setDisableManual(true);
                payment.setBalanceInquiry(true);
                payment.setCashbackAmount(0);
                payment.setDisableEbtVoucher(true);
                payment.setCurrency(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
                payment.setAmount(0);
                payment.setActionLabel("Balance Inquiry");
                String action = Intents.ACTION_COLLECT_PAYMENT;
                Intent intent = new Intent(action);
                intent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
                startActivityForResult(intent, BALANCE_INQUIRY_REQUEST);
            }
        });
    }

    private void createTrackFormatView() {
        final Button setTrackFormats = (Button) findViewById(R.id.setTrackFormats);
        setTrackFormats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte trackDataFormat = (byte) 0x02;
                // setTrackDataFormat(trackDataFormat);
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

    private void createSettleView() {
        final TextView settleStatusTextView = (TextView) findViewById(R.id.settleStatusTextView);
        final EditText settleTransactionIds = (EditText) findViewById(R.id.settleTransactionIds);
        final Button settleAll = (Button) findViewById(R.id.settleButton);
        settleAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> transactionIdList = getTransactionIdList(settleTransactionIds.getText().toString());
                settleTransactionIds.getText().clear();
                settleStatusTextView(settleStatusTextView, "");
                transactionManager.settle(transactionIdList, new ConvergeCallback<ElavonSettleResponse>() {
                    @Override
                    public void onResponse(final ElavonSettleResponse elavonResponse) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                settleStatusTextView(
                                        settleStatusTextView,
                                        "Success! Settled " + elavonResponse.getTxnMainCount()
                                                + " with amount: " + elavonResponse.getTxnMainAmount());
                            }
                        });
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                settleStatusTextView(settleStatusTextView, "Failed!");
                            }
                        });
                        Log.e(TAG, "Failed to settle", t);
                    }
                });
            }
        });
    }

    private void settleStatusTextView(final TextView settleStatusTextView, final String text) {
        settleStatusTextView.setText("    Status: " + text);
    }

    private List<String> getTransactionIdList(final String transactionIds) {
        final List<String> list = new ArrayList<>();
        for (String id : transactionIds.split(",")) {
            String trim = id.trim();
            if (!trim.isEmpty()) {

                if (trim.equals("aaa")) {
                    list.add("151217A15-459608A6-9B4C-4590-8897-8031B3D64D9F");
                }

                list.add(trim);
            }
        }
        return list;
    }

    private void createManualEntryView() {
        final String manualEntryHtml = FileUtil.readFile(getResources().openRawResource(R.raw.manual_entry));
        manualEntryWebView = (WebView) findViewById(R.id.manualEntryWebView);
        manualEntryWebView.getSettings().setJavaScriptEnabled(true);
        manualEntryWebView.getSettings().setAllowFileAccessFromFileURLs(true); // maybe you don't need this rule
        manualEntryWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        manualEntryWebView.setWebViewClient(new WebViewClient());
        manualEntryWebView.addJavascriptInterface(virtualTerminalService, "Converge");
        manualEntryWebView.loadData(manualEntryHtml, "text/html", "UTF-8");
        virtualTerminalService.setVirtualTerminalListener(this);
    }

    @Override
    public void onProcessed(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                manualEntryWebView.evaluateJavascript("updateResult('" + message + "');", null);
            }
        });
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
