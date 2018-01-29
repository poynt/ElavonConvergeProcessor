package com.elavon.converge.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.elavon.converge.R;
import com.elavon.converge.core.TransactionManager;
import com.elavon.converge.inject.AppComponent;
import com.elavon.converge.inject.AppModule;
import com.elavon.converge.inject.DaggerAppComponent;
import com.elavon.converge.util.FileUtil;

import javax.inject.Inject;

import co.poynt.api.model.Transaction;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;

public class ManualEntryActivity extends AppCompatActivity
        implements VirtualTerminalService.VirtualTerminalListener {

    private static final String TAG = ManualEntryActivity.class.getSimpleName();
    protected VirtualTerminalService virtualTerminalService;
    private WebView manualEntryWebView;
    private Payment payment;

    @Inject
    TransactionManager transactionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AppComponent component = DaggerAppComponent.builder().appModule(new AppModule(this.getApplicationContext())).build();
        component.inject(this);
        //Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // disable clicking outside closing the activity
        setFinishOnTouchOutside(false);

        setContentView(R.layout.activity_manual_entry);

        // Get the intent that started this activity
        Intent intent = getIntent();
        if (intent != null) {
            handleIntent(intent);
        } else {
            Log.e(TAG, "ManualEntryActivity launched with no intent!");
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if ("COLLECT_MANUAL_ENTRY".equalsIgnoreCase(action)) {
            payment = intent.getParcelableExtra("payment");

            if (payment == null) {
                Log.e(TAG, "ManualEntryActivity launched with no payment object");
                Intent result = new Intent(Intents.ACTION_COLLECT_PAYMENT_RESULT);
                setResult(Activity.RESULT_CANCELED, result);
                finish();
            } else {
                createManualEntryView();
            }
        } else {
            Log.e(TAG, "ManualEntryActivity launched with unknown action: " + action);
            Intent result = new Intent(Intents.ACTION_COLLECT_PAYMENT_RESULT);
            setResult(Activity.RESULT_CANCELED, result);
            finish();
        }
    }

    private void createManualEntryView() {
        VirtualTerminalService virtualTerminalService = new VirtualTerminalService(transactionManager, payment);

        final String manualEntryHtml = FileUtil.readFile(getResources().openRawResource(R.raw.manual_entry_form));
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
    public void onProcessed(final Transaction transaction, final String message) {
        if (transaction != null) {
            Intent result = new Intent(Intents.ACTION_COLLECT_PAYMENT_RESULT);
            result.putExtra("transaction", transaction);
            setResult(Activity.RESULT_OK, result);
            finish();
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    manualEntryWebView.evaluateJavascript("updateResult('" + message + "');", null);
                }
            });
        }
    }

    @Override
    public void onCancel() {
        Log.e(TAG, "Manual entry canceled");
        Intent result = new Intent(Intents.ACTION_COLLECT_PAYMENT_RESULT);
        setResult(Activity.RESULT_CANCELED, result);
        finish();
    }
}
