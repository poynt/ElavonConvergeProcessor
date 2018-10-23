package com.elavon.converge.model.mapper;

import android.util.Log;

import com.elavon.converge.exception.ConvergeMapperException;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.ElavonEntryMode;
import com.elavon.converge.model.type.ElavonPosMode;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CardUtil;
import com.elavon.converge.util.CurrencyUtil;
import com.elavon.converge.util.HexDump;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.EntryMode;
import co.poynt.api.model.FundingSourceEntryDetails;
import co.poynt.api.model.Transaction;

import static com.elavon.converge.model.type.ElavonTransactionType.DELETE;
import static com.elavon.converge.model.type.ElavonTransactionType.VOID;

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
    public ElavonTransactionRequest createRefund(final Transaction t) {
        ElavonTransactionRequest request = null;
        // if action in transaction is refund but no parent id then it's
        // a Non-Ref Credit otherwise it's a regular refund
        if (t.getParentId() != null) {
            request = new ElavonTransactionRequest();
            request.setTransactionType(ElavonTransactionType.RETURN);
            request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(), t.getAmounts().getCurrency()));
            // add retrieval ref number if we have it
            if (t.getProcessorResponse() != null) {
                request.setTxnId(t.getProcessorResponse().getRetrievalRefNum());
            }
        } else {
            request = createRefundRequest(t);
            request.setTransactionType(ElavonTransactionType.CREDIT);
        }
        return request;
    }

    private ElavonTransactionRequest createRefundRequest(Transaction t) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setPoyntUserId(t.getContext().getEmployeeUserId().toString());
        request.setPosMode(ElavonPosMode.ICC_DUAL);
        FundingSourceEntryDetails entryDetails = t.getFundingSource().getEntryDetails();
        if (entryDetails.getEntryMode() == EntryMode.CONTACTLESS_INTEGRATED_CIRCUIT_CARD
                || entryDetails.getEntryMode() == EntryMode.CONTACTLESS_MAGSTRIPE) {
            request.setEntryMode(ElavonEntryMode.EMV_PROXIMITY_READ);
        } else if (entryDetails.getEntryMode() == EntryMode.INTEGRATED_CIRCUIT_CARD) {
            request.setEntryMode(ElavonEntryMode.EMV_WITH_CVV);
        }
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(), t.getAmounts().getCurrency()));
        if (t.getAmounts().getTipAmount() != null) {
            request.setTipAmount((CurrencyUtil.getAmount(t.getAmounts().getTipAmount(), t.getAmounts().getCurrency())));
        }
        request.setFirstName(t.getFundingSource().getCard().getCardHolderFirstName());
        request.setLastName(t.getFundingSource().getCard().getCardHolderLastName());
        request.setEncryptedTrackData(t.getFundingSource().getCard().getTrack2data());
        request.setKsn(t.getFundingSource().getCard().getKeySerialNumber());
        request.setCardLast4(t.getFundingSource().getCard().getNumberLast4());
        if (t.getFundingSource().getVerificationData() != null) {
            request.setPinBlock(t.getFundingSource().getVerificationData().getPin());
            request.setPinKsn(t.getFundingSource().getVerificationData().getKeySerialNumber());
        }

        return request;
    }

    @Override
    public ElavonTransactionRequest createReverse(String transactionId) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.EMV_REVERSAL);
        // elavon transactionId
        request.setTxnId(transactionId);
        return request;
    }

    @Override
    public ElavonTransactionRequest createVoid(Transaction transaction, String transactionId) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        if (transaction.isAuthOnly() == Boolean.TRUE) {
            request.setTransactionType(DELETE);
        } else {
            request.setTransactionType(VOID);
        }
        // elavon transactionId
        request.setTxnId(transactionId);
        return request;
    }

    @Override
    public ElavonTransactionRequest createSale(final Transaction transaction) {
        final ElavonTransactionRequest request = createRequest(transaction);
        request.setTransactionType(ElavonTransactionType.EMV_CT_SALE);
        return request;
    }

    @Override
    public ElavonTransactionRequest createVerify(final Transaction transaction) {
        final ElavonTransactionRequest request = createRequest(transaction);
        request.setTransactionType(ElavonTransactionType.VERIFY);
        return request;
    }

    private ElavonTransactionRequest createRequest(final Transaction t) {
        //TODO handle tip
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setPoyntUserId(t.getContext().getEmployeeUserId().toString());
        request.setTlvEnc(getTlvTags(t));
        FundingSourceEntryDetails entryDetails = t.getFundingSource().getEntryDetails();
        request.setPosMode(ElavonPosMode.ICC_DUAL);
        if (entryDetails.getEntryMode() == EntryMode.CONTACTLESS_INTEGRATED_CIRCUIT_CARD
                || entryDetails.getEntryMode() == EntryMode.CONTACTLESS_MAGSTRIPE) {
            request.setEntryMode(ElavonEntryMode.EMV_PROXIMITY_READ);
        } else if (entryDetails.getEntryMode() == EntryMode.INTEGRATED_CIRCUIT_CARD) {
            request.setEntryMode(ElavonEntryMode.EMV_WITH_CVV);
        }
        if (t.getAmounts().getTipAmount() != null) {
            request.setTipAmount((CurrencyUtil.getAmount(t.getAmounts().getTipAmount(), t.getAmounts().getCurrency())));
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
        ArrayList<Map.Entry<String, String>> tlvList = new ArrayList<>();
        for (final Map.Entry<String, String> tag : t.getFundingSource().getEmvData().getEmvTags().entrySet()) {
            final String kHex = tag.getKey().substring(2);
            final String lHex = HexDump.toHexString((byte) (tag.getValue().length() / 2));
            Log.i(TAG, String.format("%s=%s", kHex, tag.getValue()));

            //Adding the tags starting with DF to a list, to push all 3-byte tags to the end, based on Converge request
            if(kHex.startsWith("DF") && kHex.length() == 6) {
                tlvList.add(tag);
                continue;
            }

            // maps tags as per converge needs
            // 57 needs to be copied as D0
            if (kHex.equals("57")) {
                builder.append("D0");
                builder.append(lHex);
                builder.append(tag.getValue());
                // actual 57
                builder.append(kHex);
            } else if (kHex.equals("1F8102")) {
                // Data KSN
                builder.append("C0");
            } else if (kHex.equals("1F8101")) {
                // PIN KSN
                builder.append("C1");
            } else if (kHex.startsWith("1F81")) {
                // skip all other custom POYNT tags
                continue;
            } else {
                // everything else just add it
                builder.append(kHex);
            }
            builder.append(lHex);
            builder.append(tag.getValue());
            Log.d(TAG, MessageFormat.format("T:{0}, L:{1}, V:{2}", kHex, lHex, tag.getValue()));
        }

        // HACK - add 9F03 if it doesn't exist
        if (!t.getFundingSource().getEmvData().getEmvTags().containsKey("0x9F03")) {
            builder.append("9F03");
            builder.append("06");
            builder.append("000000000000");
        }

        //Pushing all 3-byte tags to the end of tlv string, based on Converge request
        if(tlvList != null && tlvList.size() > 0) {
            for(Map.Entry<String, String> tag : tlvList) {
                final String kHex = tag.getKey().substring(2);
                final String lHex = HexDump.toHexString((byte) (tag.getValue().length() / 2));
                builder.append(kHex);
                builder.append(lHex);
                builder.append(tag.getValue());
                Log.d(TAG, MessageFormat.format("T:{0}, L:{1}, V:{2}", kHex, lHex, tag.getValue()));
            }
        }

        /**
         * Optional for US domestic debit:
         *   Debit Account Type = ‘5F57’
         *   Checking = ‘0’
         *   Savings  = ‘1’
         */
        // TODO
        return builder.toString();
    }

    @Override
    ElavonTransactionRequest createBalanceInquiry(BalanceInquiry balanceInquiry) {
        throw new ConvergeMapperException("Not supported");
    }
}
