package com.elavon.converge.model;

import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.model.type.ResponseCodes;

import org.simpleframework.xml.Element;

public abstract class ElavonResponse {

    public static final String RESULT_SUCCESS = "0";

    public static class RESULT_MESSAGE {
        public static final String APPROVAL = "APPROVAL";// Approved
        public static final String PARTIAL_APPROVAL = "PARTIAL APPROVAL";// Approved for a Partial Amount
        public static final String DECLINE_CVV2 = "DECLINE CVV2";// Do not honor due to CVV2 mismatchfailure
        public static final String PICK_UP_CARD = "PICK UP CARD";// Pick up card
        public static final String AMOUNT_ERROR = "AMOUNT ERROR";// Tran Amount Error
        public static final String AMT_OVER_SVC_LMT = "AMT OVER SVC LMT";// Amount is more than established service limit
        public static final String APPL_TYPE_ERROR = "APPL TYPE ERROR";// Call for Assistance
        public static final String CANNOT_CONVERT = "CANNOT CONVERT";// Check is ok, but cannot be converted. Do Not Honor
        public static final String DECLINED = "DECLINED";// Do Not Honor
        public static final String DECLINED_T4 = "DECLINED T4";// Do Not Honor. Failed negative check, unpaid items
        public static final String DECLINED_HELP_9999 = "DECLINED-HELP 9999";// System Error
        public static final String DUP_CHECK_NBR = "DUP CHECK NBR";// Duplicate Check Number
        public static final String EXPIRED_CARD = "EXPIRED CARD";// Expired Card
        public static final String INCORRECT_PIN = "INCORRECT PIN";// Invalid PIN
        public static final String INVALID_CARD = "INVALID CARD";// Invalid Card
        public static final String INVALID_CAVV = "INVALID CAVV";// Invalid Cardholder Authentication Verification Value
        public static final String INVALID_TERM_ID = "INVALID TERM ID";// Invalid Terminal ID
        public static final String INVLD_RT_NBR = "INVLD R/T NBR";// Invalid Routing/Transit Number
        public static final String INVLD_TERM_ID1 = "INVLD TERM ID 1";// Invalid Merchant Number
        public static final String INVLD_TERM_ID2 = "INVLD TERM ID 2";// Invalid SE Number Note:AMEX Only
        public static final String INVLD_VOID_DATA = "INVLD VOID DATA";// Invalid Data Submitted for Void Transaction
        public static final String MAX_MONTHLY_VOL = "MAX MONTHLY VOL";// The maximum monthly volume has been reached
        public static final String MICR_ERROR_MICR = "MICR ERROR MICR";// Read Error
        public static final String MUST_SETTLE_MMDD = "MUST SETTLE MMDD";// Must settle, open batch is over 7 days old Note: Best Practice is to settle within 24 hours. Batch will be Auto Settled after 10 days
        public static final String NETWORK_ERROR = "NETWORK ERROR";// General System Error
        public static final String PLEASE_RETRY = "PLEASE RETRY";// Please Retry/Reenter Transaction
        public static final String RECORD_NOT_FOUND = "RECORD NOT FOUND";// Record not on the network
        public static final String REQ_EXCEEDS_BAL = "REQ. EXCEEDS BAL.";// Req. exceeds balance
        public static final String SEQ_ERR_PLS_CALL = "SEQ ERR PLS CALL";// Call for Assistance
        public static final String SERV_NOT_ALLOWED = "SERV NOT ALLOWED";// Invalid request
        public static final String TOO_MANY_CHECKS = "TOO MANY CHECKS";// Too Many Checks (Over Limit)
        public static final String CALL_AUTH_CENTER = "CALL AUTH. CENTER";// Refer to Issuer
        public static final String SUCCESS = "SUCCESS";// For successfully added, updated, deleted recurring or installment transactions
        public static final String ERROR = "ERROR";// For recurring or installment transactions that failed to be added, deleted or updated
    }

    @Element(name = "ssl_result", required = false)
    protected String result;

    @Element(name = "ssl_result_message", required = false)
    protected String resultMessage;

    // Error code returned only if an error occurred. Typically, when the transaction failed validation or the request is
    // incorrect. This will prevent the transaction from going to authorization.
    @Element(name = "errorCode", required = false)
    protected int errorCode;

    @Element(name = "errorName", required = false)
    protected String errorName;

    @Element(name = "errorMessage", required = false)
    protected String errorMessage;

    @Element(name = "ssl_transaction_type", required = false)
    protected ElavonTransactionType transactionType;

    @Element(name = "ssl_response_code", required = false)
    protected ResponseCodes responseCode;


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorName() {
        return errorName;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return RESULT_SUCCESS.equals(result);
    }

    public ElavonTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(ElavonTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public ResponseCodes getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(ResponseCodes responseCode) {
        this.responseCode = responseCode;
    }
}
