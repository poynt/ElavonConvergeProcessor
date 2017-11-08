package com.elavon.converge.model.mapper;

import android.util.Log;

import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.ElavonEntryMode;
import com.elavon.converge.model.type.ElavonPosMode;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.HexDump;

import java.text.MessageFormat;
import java.util.Map;

import javax.inject.Inject;

import co.poynt.api.model.Transaction;

public class EmvMapper extends InterfaceMapper {

    private static final String TAG = "EmvMapper";

    @Inject
    public EmvMapper() {
    }

    @Override
    public ElavonTransactionRequest createAuth(final Transaction transaction) {
        final ElavonTransactionRequest request = createRequest(transaction);
        request.setTransactionType(ElavonTransactionType.EMV_CT_AUTH_ONLY);
        return request;
    }

    @Override
    public ElavonTransactionRequest createCapture(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    public ElavonTransactionRequest createVoid(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    public ElavonTransactionRequest createOfflineAuth(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    public ElavonTransactionRequest createRefund(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    public ElavonTransactionRequest createSale(final Transaction transaction) {
        final ElavonTransactionRequest request = createRequest(transaction);
        request.setTransactionType(ElavonTransactionType.EMV_CT_SALE);
        return request;
    }

    @Override
    public ElavonTransactionRequest createVerify(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    private ElavonTransactionRequest createRequest(final Transaction t) {
        //TODO handle tip
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTlvEnc(getTlvTags(t));
        request.setPosMode(ElavonPosMode.CT_ONLY);
        request.setEntryMode(ElavonEntryMode.EMV_WITH_CVV);
        return request;
    }

    private String getTlvTags(final Transaction t) {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<String, String> tag : t.getFundingSource().getEmvData().getEmvTags().entrySet()) {
            final String kHex = tag.getKey().substring(2);
            final String lHex = HexDump.toHexString((byte) (tag.getValue().length() / 2));
            builder.append(kHex);
            builder.append(lHex);
            builder.append(tag.getValue());
            Log.d(TAG, MessageFormat.format("T:{0}, L:{1}, V:{2}", kHex, lHex, tag.getValue()));
        }
        return builder.toString();
    }
}
