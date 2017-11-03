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

public class EmvMapper implements InterfaceMapper {

    @Inject
    public EmvMapper() {
    }

    @Override
    public ElavonTransactionRequest createAuth(final Transaction transaction) {
        return createAuthOrSale(transaction, true);
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
        return createAuthOrSale(transaction, false);
    }

    @Override
    public ElavonTransactionRequest createVerify(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    private ElavonTransactionRequest createAuthOrSale(final Transaction t, final boolean isAuthOnly) {
        //TODO handle tip
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(isAuthOnly ? ElavonTransactionType.EMV_CT_AUTH_ONLY : ElavonTransactionType.EMV_CT_SALE);
        request.setTlvEnc(getTlvTags(t));
        request.setPosMode(ElavonPosMode.CT_MAGSTRIPE);
        request.setEntryMode(ElavonEntryMode.EMV_WITH_CVV);
        return request;
    }

    private String getTlvTags(Transaction t) {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<String, String> tag : t.getFundingSource().getEmvData().getEmvTags().entrySet()) {

            final String kHex = tag.getKey().substring(2);
            final String lHex = HexDump.toHexString((byte) (tag.getValue().length() / 2));
            builder.append(kHex);
            builder.append(lHex);
            builder.append(tag.getValue());

            Log.i("EmvMapper", MessageFormat.format("T:%s, L:%s, V:%s", kHex, lHex, tag.getValue()));
        }
        return builder.toString();
    }
}
