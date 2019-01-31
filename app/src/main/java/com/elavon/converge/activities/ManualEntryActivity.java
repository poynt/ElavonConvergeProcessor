package com.elavon.converge.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.elavon.converge.ElavonConvergeProcessorApplication;
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
    private String selectedPaymentMethodType;
    private ProgressDialog progress;

    @Inject
    TransactionManager transactionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AppComponent component = ElavonConvergeProcessorApplication.getInstance().getAppComponent();
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
            selectedPaymentMethodType = intent.getStringExtra("selectedPaymentMethodType");
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
        virtualTerminalService = new VirtualTerminalService(transactionManager, payment, selectedPaymentMethodType);

        final String manualEntryHtml = FileUtil.readFile(getResources().openRawResource(R.raw.vt));
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
        // stop the progress dialog if it's still running
        stopProgressDialog();
        if (transaction != null) {
            Intent result = new Intent(Intents.ACTION_COLLECT_PAYMENT_RESULT);
            result.putExtra("transaction", transaction);
            setResult(Activity.RESULT_OK, result);
            finish();
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //manualEntryWebView.evaluateJavascript("updateResult('" + message + "');", null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ManualEntryActivity.this);
                    builder.setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    Log.e(TAG, "Manual entry transaction failed:" + message);
                                    Intent result = new Intent(Intents.ACTION_COLLECT_PAYMENT_RESULT);
                                    setResult(Activity.RESULT_CANCELED, result);
                                    finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
    }

    @Override
    public void onCancel() {
        Log.e(TAG, "Manual entry canceled");
        // stop progress bar if it's still running
        stopProgressDialog();
        Intent result = new Intent(Intents.ACTION_COLLECT_PAYMENT_RESULT);
        setResult(Activity.RESULT_CANCELED, result);
        finish();
    }

    @Override
    public void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ManualEntryActivity.this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void showProgressDialog() {
        if (progress == null) {
            progress = new ProgressDialog(this);
            progress.setMessage("processing...");
            progress.setCancelable(false);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
        }
        progress.show();
    }

    @Override
    public void stopProgressDialog() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }

    @Override
    public void collectAuthorizationCode(final VirtualTerminalService.AuthorizationCodeCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Authorization Code");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onApprovalCodeEntered(input.getText().toString());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onApprovalCodeCanceled();
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void collectEBTVoucherNumber(final VirtualTerminalService.AuthorizationCodeCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Voucher Number");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onApprovalCodeEntered(input.getText().toString());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onApprovalCodeCanceled();
                dialog.cancel();
            }
        });

        builder.show();
    }
}
