package com.elavon.converge.model.mapper;

import android.util.Log;

import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.ElavonEntryMode;
import com.elavon.converge.model.type.ElavonPosMode;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CardUtil;
import com.elavon.converge.util.HexDump;

import java.text.MessageFormat;
import java.util.Map;

import javax.inject.Inject;

import co.poynt.api.model.EntryMode;
import co.poynt.api.model.FundingSourceEntryDetails;
import co.poynt.api.model.Transaction;

public class EmvMapper implements InterfaceMapper {

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
        FundingSourceEntryDetails entryDetails = t.getFundingSource().getEntryDetails();
        if (entryDetails.getEntryMode() == EntryMode.CONTACTLESS_INTEGRATED_CIRCUIT_CARD
                || entryDetails.getEntryMode() == EntryMode.CONTACTLESS_MAGSTRIPE) {
            request.setPosMode(ElavonPosMode.CL_CAPABLE);
            request.setEntryMode(ElavonEntryMode.CONTACTLESS);
        } else if (entryDetails.getEntryMode() == EntryMode.INTEGRATED_CIRCUIT_CARD) {
            request.setPosMode(ElavonPosMode.CT_ONLY);
            request.setEntryMode(ElavonEntryMode.EMV_WITH_CVV);
        }
        request.setFirstName(t.getFundingSource().getCard().getCardHolderFirstName());
        request.setLastName(t.getFundingSource().getCard().getCardHolderLastName());
        request.setEncryptedTrackData(t.getFundingSource().getCard().getTrack2data());
        request.setKsn(t.getFundingSource().getCard().getKeySerialNumber());
        request.setExpDate(CardUtil.getCardExpiry(t.getFundingSource().getCard()));
        request.setCardLast4(t.getFundingSource().getCard().getNumberLast4());
        if (t.getFundingSource().getVerificationData() != null) {
            request.setPinBlock(t.getFundingSource().getVerificationData().getPin());
            request.setPinKsn(t.getFundingSource().getVerificationData().getKeySerialNumber());
        }

        return request;
    }

    private String getTlvTags(final Transaction t) {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<String, String> tag : t.getFundingSource().getEmvData().getEmvTags().entrySet()) {
            final String kHex = tag.getKey().substring(2);
            final String lHex = HexDump.toHexString((byte) (tag.getValue().length() / 2));
            Log.d(TAG, String.format("%s=%s", kHex, tag.getValue()));

            if (kHex.equals("57")) {
                builder.append("D0");
                builder.append(lHex);
                builder.append(tag.getValue());
                // actual 57
                builder.append(kHex);
            } else if (kHex.equals("1F8102")) {
                builder.append("C0");
            } else if (kHex.startsWith("1F81")) {
                // skip all other tags
                continue;
            } else {
                builder.append(kHex);
            }
            builder.append(lHex);
            builder.append(tag.getValue());
            Log.d(TAG, MessageFormat.format("T:{0}, L:{1}, V:{2}", kHex, lHex, tag.getValue()));
        }
        return builder.toString();
    }
}
