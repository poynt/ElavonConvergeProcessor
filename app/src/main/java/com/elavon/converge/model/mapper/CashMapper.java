package com.elavon.converge.model.mapper;

import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CurrencyUtil;

import javax.inject.Inject;

import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.Transaction;

import static com.elavon.converge.model.type.ElavonTransactionType.VOID;

public class CashMapper extends InterfaceMapper {

    @Inject
    public CashMapper() {
    }

    @Override
    public ElavonTransactionRequest createAuth(final Transaction transaction) {
        // Cash is always SALE
        return null;
    }

    @Override
    public ElavonTransactionRequest createReverse(String transactionId) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(VOID);
        // elavon transactionId
        request.setTxnId(transactionId);
        return request;
    }

    @Override
    public ElavonTransactionRequest createRefund(final Transaction t) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.CASH_CREDIT);
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(), t.getAmounts().getCurrency()));
        request.setTxnId(t.getProcessorResponse().getRetrievalRefNum());
        return request;
    }

    @Override
    public ElavonTransactionRequest createSale(final Transaction transaction) {
        final ElavonTransactionRequest request = createRequest(transaction);
        request.setTransactionType(ElavonTransactionType.CASH_SALE);
        return request;
    }

    @Override
    public ElavonTransactionRequest createVerify(final Transaction transaction) {
        // no op for Cash
        return null;
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
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(), t.getAmounts().getCurrency()));
        return request;
    }

    @Override
    ElavonTransactionRequest createBalanceInquiry(final BalanceInquiry b) {
        // no op for cash
        return null;
    }
}
