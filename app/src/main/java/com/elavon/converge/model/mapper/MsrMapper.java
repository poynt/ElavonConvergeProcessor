package com.elavon.converge.model.mapper;

import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.ElavonEntryMode;
import com.elavon.converge.model.type.ElavonPosMode;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CardUtil;
import com.elavon.converge.util.CurrencyUtil;

import javax.inject.Inject;

import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.EntryMode;
import co.poynt.api.model.FundingSourceEntryDetails;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionStatus;
import co.poynt.os.util.StringUtil;

import static com.elavon.converge.model.type.ElavonTransactionType.DELETE;
import static com.elavon.converge.model.type.ElavonTransactionType.VOID;

public class MsrMapper extends InterfaceMapper {

    @Inject
    public MsrMapper() {
    }

    @Override
    public ElavonTransactionRequest createAuth(final Transaction transaction) {
        final ElavonTransactionRequest request = createRequest(transaction);
        if (transaction.getFundingSource().getEntryDetails().isIccFallback()
                == Boolean.TRUE) {
            request.setTransactionType(ElavonTransactionType.EMV_SWIPE_AUTH_ONLY);
            request.setEntryMode(ElavonEntryMode.ICC_FALLBACK);
        } else {
            request.setTransactionType(ElavonTransactionType.AUTH_ONLY);
            //Because there's no info on Converge's doc for ssl_tip_amount in ccauthonly. So add the tip amount to total amount.
            if(transaction.getAmounts().getTipAmount() != null) {
                request.setAmount(CurrencyUtil.getAmount(transaction.getAmounts().getTipAmount()
                        + transaction.getAmounts().getOrderAmount(), transaction.getAmounts().getCurrency()));
            }
        }
        return request;
    }

    @Override
    public ElavonTransactionRequest createReverse(String transactionId) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();

        // from Converge documentation
        // ccdelete - deletes and attempts a reversal on a Sale or Auth Only credit transaction.
        //            This transaction type is typically used in a Partial approval  scenario
        // Best suited for reversals + authonly

        // ccvoid -  removes a Sale,Credit or force transaction from the open batch. The ccvoid
        //           command is typically used for same day returns or to correct cashier mistakes.
        // Best suited for voiding txns before they are settled.
        request.setTransactionType(DELETE);
        // elavon transactionId
        request.setTxnId(transactionId);
        return request;
    }

    @Override
    public ElavonTransactionRequest createVoid(Transaction transaction, String transactionId) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();

        // from Converge documentation
        // ccdelete - deletes and attempts a reversal on a Sale or Auth Only credit transaction.
        //            This transaction type is typically used in a Partial approval  scenario
        // Best suited for reversals + authonly

        // ccvoid -  removes a Sale,Credit or force transaction from the open batch. The ccvoid
        //           command is typically used for same day returns or to correct cashier mistakes.
        // Best suited for voiding txns before they are settled.
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
            request = createRequest(t);
            request.setTransactionType(ElavonTransactionType.CREDIT);
        }
        return request;
    }

    @Override
    public ElavonTransactionRequest createSale(final Transaction transaction) {
        final ElavonTransactionRequest request = createRequest(transaction);
        if (transaction.getFundingSource().getEntryDetails().isIccFallback()
                == Boolean.TRUE) {
            request.setTransactionType(ElavonTransactionType.EMV_SWIPE_SALE);
            request.setEntryMode(ElavonEntryMode.ICC_FALLBACK);
        } else {
            request.setTransactionType(isForce(transaction) ? ElavonTransactionType.FORCE : ElavonTransactionType.SALE);
        }
        // add approval code if there is one
        request.setApprovalCode(transaction.getApprovalCode());
        return request;
    }

    @Override
    public ElavonTransactionRequest createVerify(final Transaction transaction) {
        final ElavonTransactionRequest request = createRequest(transaction);
        request.setTransactionType(ElavonTransactionType.VERIFY);
        return request;
    }

    private boolean isForce(final Transaction t) {
        return t.getStatus() == TransactionStatus.CAPTURED && StringUtil.notEmpty(t.getApprovalCode());
    }

    /**
     * <pre><code>
     * Example Transaction:
     * {
     *   "action": "SALE",
     *   "amounts": {
     *   "currency": "USD",
     *   "orderAmount": 552,
     *   "tipAmount": 0,
     *   "transactionAmount": 552
     * },
     * "authOnly": false,
     * "context": {
     *   "businessId": "2ac806d1-73e7-40c3-94ec-be2bb401a2df",
     *   "businessType": "TEST_MERCHANT",
     *   "employeeUserId": 17371213,
     *   "mcc": "5812",
     *   "mid": "e10zu3b7xs",
     *   "source": "INSTORE",
     *   "sourceApp": "co.poynt.services",
     *   "storeAddressCity": "Palo Alto",
     *   "storeAddressTerritory": "California",
     *   "storeId": "992e7a4e-65e6-4919-825e-8b0f2f63a592",
     *   "storeTimezone": "America/Los_Angeles",
     *   "tid": "56uw"
     * },
     * "customerLanguage": "en",
     * "fundingSource": {
     *   "card": {
     *     "cardHolderFirstName": "2020",
     *     "cardHolderFullName": "MONEY/2020",
     *     "cardHolderLastName": "MONEY",
     *     "encrypted": true,
     *     "expirationDate": 31,
     *     "expirationMonth": 10,
     *     "expirationYear": 2016,
     *     "keySerialNumber": "FFFF9876543210E0004B",
     *     "numberFirst6": "453213",
     *     "numberLast4": "1054",
     *     "track1data": "CD7CF4B5497D239E977946B67A5364D894E4C72E015AD731E444B6ED3BACE67AE17CE1C542A76FA77B0E478ADB013BF034DF7C87307AD11A",
     *     "track2data": "",
     *     "type": "VISA"
     *   },
     *   "emvData": {
     *     "emvTags": {
     *     "0x5F24": "161031",
     *     "0x1F815D": "34",
     *     "0x5F20": "4D4F4E45592F32303230",
     *     "0x1F8104": "31303534",
     *     "0x1F815F": "04",
     *     "0x1F8103": "343533323133",
     *     "0x5F2A": "0840",
     *     "0x1F8102": "FFFF9876543210E0004B",
     *     "0x5F30": "101F",
     *     "0x1F8161": "00",
     *     "0x5F36": "02",
     *     "0x57": "",
     *     "0x58": "",
     *     "0x9F39": "02",
     *     "0x1F8153": "9D3E2DE7",
     *     "0x56": "CD7CF4B5497D239E977946B67A5364D894E4C72E015AD731E444B6ED3BACE67AE17CE1C542A76FA77B0E478ADB013BF034DF7C87307AD11A"
     *     }
     *   },
     *   "entryDetails": {
     *     "customerPresenceStatus": "PRESENT",
     *     "entryMode": "TRACK_DATA_FROM_MAGSTRIPE"
     *   },
     *   "type": "CREDIT_DEBIT"
     * },
     * "references": [
     *   {
     *     "customType": "referenceId",
     *     "id": "5c9a6b74-015f-1000-6146-0e9b4d0e4042",
     *     "type": "CUSTOM"
     *   }
     * ],
     * "signatureRequired": true
     * }
     * </code></pre>
     */
    private ElavonTransactionRequest createRequest(final Transaction t) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setPoyntUserId(t.getContext().getEmployeeUserId().toString());
        // always ICC DUAL - highest capability
        request.setPosMode(ElavonPosMode.ICC_DUAL);
        FundingSourceEntryDetails entryDetails = t.getFundingSource().getEntryDetails();

        if (entryDetails.getEntryMode() == EntryMode.CONTACTLESS_INTEGRATED_CIRCUIT_CARD
                || entryDetails.getEntryMode() == EntryMode.CONTACTLESS_MAGSTRIPE) {
            request.setEntryMode(ElavonEntryMode.CONTACTLESS);
        } else if (entryDetails.getEntryMode() == EntryMode.TRACK_DATA_FROM_MAGSTRIPE) {
            request.setEntryMode(ElavonEntryMode.SWIPED);
        }
        //This should only be the order amount. converge has another field for tip amount.
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getOrderAmount(), t.getAmounts().getCurrency()));
        if(t.getAmounts().getTipAmount() != null) {
            request.setTipAmount(CurrencyUtil.getAmount(t.getAmounts().getTipAmount(), t.getAmounts().getCurrency()));
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

    @Override
    ElavonTransactionRequest createBalanceInquiry(final BalanceInquiry b) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.BALANCE_INQUIRY);
        request.setPosMode(ElavonPosMode.ICC_DUAL);
        request.setEncryptedTrackData(b.getFundingSource().getCard().getTrack2data());
        request.setKsn(b.getFundingSource().getCard().getKeySerialNumber());
        request.setExpDate(CardUtil.getCardExpiry(b.getFundingSource().getCard()));
        return request;
    }
}
