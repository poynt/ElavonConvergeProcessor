package com.elavon.converge.model;

import android.support.annotation.Nullable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Sample Response:
 <txn>
     <ssl_card_short_description>MC</ssl_card_short_description>
     <ssl_cvv2_response />
     <ssl_account_balance>10.00</ssl_account_balance>
     <ssl_result_message>APPROVAL</ssl_result_message>
     <ssl_invoice_number />
     <ssl_promo_code />
     <ssl_result>0</ssl_result>
     <ssl_txn_id>231017A15-67ADEA9E-4E0D-410A-B11C-61B0BE82DEB9</ssl_txn_id>
     <ssl_completion_date />
     <ssl_transaction_type>SALE</ssl_transaction_type>
     <ssl_avs_response />
     <ssl_account_status />
     <ssl_approval_code>CMC345</ssl_approval_code>
     <ssl_enrollment />
     <ssl_exp_date>1225</ssl_exp_date>
     <ssl_loyalty_program />
     <ssl_tender_amount />
     <ssl_departure_date />
     <ssl_card_type>CREDITCARD</ssl_card_type>
     <ssl_loyalty_account_balance />
     <ssl_salestax />
     <ssl_amount>10.00</ssl_amount>
     <ssl_card_number>54**********3330</ssl_card_number>
     <ssl_issue_points />
     <ssl_txn_time>10/23/2017 06:12:25 PM</ssl_txn_time>
     <ssl_access_code />
 </txn>
 */
@Root (name = "txn")
public class ElavonTransactionResponse extends ElavonResponse {
    public static final String RESULT_SUCCESS = "0";

    public static class RESULT_MESSAGE{
        public static final String APPROVAL="APPROVAL";// Approved
        public static final String PARTIAL_APPROVAL="PARTIAL APPROVAL";// Approved for a Partial Amount
        public static final String DECLINE_CVV2="DECLINE CVV2";// Do not honor due to CVV2 mismatchfailure
        public static final String PICK_UP_CARD="PICK UP CARD";// Pick up card
        public static final String AMOUNT_ERROR="AMOUNT ERROR";// Tran Amount Error
        public static final String AMT_OVER_SVC_LMT="AMT OVER SVC LMT";// Amount is more than established service limit
        public static final String APPL_TYPE_ERROR="APPL TYPE ERROR";// Call for Assistance
        public static final String CANNOT_CONVERT="CANNOT CONVERT";// Check is ok, but cannot be converted. Do Not Honor
        public static final String DECLINED="DECLINED";// Do Not Honor
        public static final String DECLINED_T4="DECLINED T4";// Do Not Honor. Failed negative check, unpaid items
        public static final String DECLINED_HELP_9999="DECLINED-HELP 9999";// System Error
        public static final String DUP_CHECK_NBR="DUP CHECK NBR";// Duplicate Check Number
        public static final String EXPIRED_CARD="EXPIRED CARD";// Expired Card
        public static final String INCORRECT_PIN="INCORRECT PIN";// Invalid PIN
        public static final String INVALID_CARD="INVALID CARD";// Invalid Card
        public static final String INVALID_CAVV="INVALID CAVV";// Invalid Cardholder Authentication Verification Value
        public static final String INVALID_TERM_ID="INVALID TERM ID";// Invalid Terminal ID
        public static final String INVLD_RT_NBR="INVLD R/T NBR";// Invalid Routing/Transit Number
        public static final String INVLD_TERM_ID1="INVLD TERM ID 1";// Invalid Merchant Number
        public static final String INVLD_TERM_ID2="INVLD TERM ID 2";// Invalid SE Number Note:AMEX Only
        public static final String INVLD_VOID_DATA="INVLD VOID DATA";// Invalid Data Submitted for Void Transaction
        public static final String MAX_MONTHLY_VOL="MAX MONTHLY VOL";// The maximum monthly volume has been reached
        public static final String MICR_ERROR_MICR="MICR ERROR MICR";// Read Error
        public static final String MUST_SETTLE_MMDD="MUST SETTLE MMDD";// Must settle, open batch is over 7 days old Note: Best Practice is to settle within 24 hours. Batch will be Auto Settled after 10 days
        public static final String NETWORK_ERROR="NETWORK ERROR";// General System Error
        public static final String PLEASE_RETRY="PLEASE RETRY";// Please Retry/Reenter Transaction
        public static final String RECORD_NOT_FOUND="RECORD NOT FOUND";// Record not on the network
        public static final String REQ_EXCEEDS_BAL="REQ. EXCEEDS BAL.";// Req. exceeds balance
        public static final String SEQ_ERR_PLS_CALL="SEQ ERR PLS CALL";// Call for Assistance
        public static final String SERV_NOT_ALLOWED="SERV NOT ALLOWED";// Invalid request
        public static final String TOO_MANY_CHECKS="TOO MANY CHECKS";// Too Many Checks (Over Limit)
        public static final String CALL_AUTH_CENTER="CALL AUTH. CENTER";// Refer to Issuer
        public static final String SUCCESS="SUCCESS";// For successfully added, updated, deleted recurring or installment transactions
        public static final String ERROR="ERROR";// For recurring or installment transactions that failed to be added, deleted or updated
    }

    @Element(name ="ssl_result", required=false)
    private String result;

    @Element (name = "ssl_result_message", required = false)
    private String resultMessage;

    @Element (name = "ssl_txn_id", required = false)
    private String txnId;

    @Element (name = "ssl_txn_time", required = false)
    private String txnTime;

    // Transaction approval code
    @Element (name = "ssl_approval_code", required = false)
    private String approvalCode;

    @Element (name = "ssl_amount", required = false)
    private BigDecimal amount;

    //The amount originally requested on partial approvals only
    @Element (name = "ssl_requested_amount", required = false)
    private BigDecimal requestedAmount;

    // This is the difference of the amount requested versus the amount authorized that the merchant
    // has to collect from the consumer on partial approvals only
    @Element (name = "ssl_balance_due", required = false)
    private BigDecimal balanceDue;

    // The balance left on the card, which is always 0.00 for a partially authorized transaction.
    @Element (name = "ssl_account_balance", required = false)
    private BigDecimal accountBalance;

    @Element (name = "ssl_avs_response", required = false)
    private AVSResponse avsResponse;

    @Element (name = "ssl_cvv2_response", required = false)
    private CVV2Response cvv2Response;

    @Element (name = "ssl_card_number", required = false)
    private CVV2Response cardNumberMasked;

    @Element (name = "ssl_invoice_number", required = false)
    private String invoiceNumber;

    @Element (name = "ssl_conversion_rate", required = false)
    private float conversionRate;

    @Element (name = "ssl_cardholder_currency", required = false)
    private String cardCurrency;

    // Total amount in cardholder currency, only returned on DCC transactions
    @Element (name = "ssl_cardholder_amount", required = false)
    private BigDecimal cardholderAmount;

    // Base amount in cardholder currency, only returned on DCC transactions.
    @Element (name = "ssl_cardholder_base_amount", required = false)
    private BigDecimal cardholderBaseAmount;

    // Tip amount in cardholder currency, only returned on DCC transactions with Service market segment
    @Element (name = "ssl_cardholder_tip_amount", required = false)
    private BigDecimal cardholderTipAmount;

    // Server ID submitted with the request. Only returned on Service market segment based on the merchant setup.
    @Element (name = "ssl_server", required = false)
    private String serverId;

    // Shift information submitted with the request. Only returned on Service market segment based on the merchant setup
    @Element (name = "ssl_shift", required = false)
    private String shift;

    // ssl_eci_ind - NOT USED. returned on 3D Secure transactions

    // Transaction currency. Returned only if terminal is setup for Multi- Currency
    @Element (name = "ssl_card_short_description", required = false)
    private CardShortDescription cardShortDescription;

    @Element (name = "ssl_card_type", required = false)
    private CardType cardType;

    @Element (name = "ssl_transaction_currency", required = false)
    private String transactionCurrency;

    @Element (name = "ssl_token", required = false)
    private String token;

    @Element (name = "ssl_token_response", required = false)
    private TokenResponse tokenResponse;

    //TODO convert to enum. Should verify return values with Elavon
    // Outcome of the Add to Card Manager request, examples: Card Added, Card Updated, Not Permitted, or
    // FAILURE - First Name - is required.
    // Returned only if storing token is requested in a terminal that setup for Tokenization.
    @Element (name = "ssl_add_token_response", required = false)
    private String addTokenResponse;

    // Error code returned only if an error occurred. Typically, when the transaction failed validation or the request is
    // incorrect. This will prevent the transaction from going to authorization.
    @Element (name = "errorCode", required = false)
    private int errorCode;

    @Element (name = "errorName", required = false)
    private String errorName;

    @Element (name = "errorMessage", required = false)
    private String errorMessage;


    public float getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(float conversionRate) {
        this.conversionRate = conversionRate;
    }

    public String getTxnTime() {
        return txnTime;
    }

    public void setTxnTime(String txnTime){
        this.txnTime = txnTime;
    }

    @Nullable
    public Date getTxnTimeAsDate(){
        SimpleDateFormat isoFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        // TODO per Elavon: The timestamp comes from Converge and is in customer MID local time.
        //
        isoFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        try {
            return isoFormat.parse(txnTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getResultSuccess() {
        return RESULT_SUCCESS;
    }

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

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getBalanceDue() {
        return balanceDue;
    }

    public void setBalanceDue(BigDecimal balanceDue) {
        this.balanceDue = balanceDue;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public AVSResponse getAvsResponse() {
        return avsResponse;
    }

    public void setAvsResponse(AVSResponse avsResponse) {
        this.avsResponse = avsResponse;
    }

    public CVV2Response getCvv2Response() {
        return cvv2Response;
    }

    public void setCvv2Response(CVV2Response cvv2Response) {
        this.cvv2Response = cvv2Response;
    }

    public CVV2Response getCardNumberMasked() {
        return cardNumberMasked;
    }

    public void setCardNumberMasked(CVV2Response cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCardCurrency() {
        return cardCurrency;
    }

    public void setCardCurrency(String cardCurrency) {
        this.cardCurrency = cardCurrency;
    }

    public BigDecimal getCardholderAmount() {
        return cardholderAmount;
    }

    public void setCardholderAmount(BigDecimal cardholderAmount) {
        this.cardholderAmount = cardholderAmount;
    }

    public BigDecimal getCardholderBaseAmount() {
        return cardholderBaseAmount;
    }

    public void setCardholderBaseAmount(BigDecimal cardholderBaseAmount) {
        this.cardholderBaseAmount = cardholderBaseAmount;
    }

    public BigDecimal getCardholderTipAmount() {
        return cardholderTipAmount;
    }

    public void setCardholderTipAmount(BigDecimal cardholderTipAmount) {
        this.cardholderTipAmount = cardholderTipAmount;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public CardShortDescription getCardShortDescription() {
        return cardShortDescription;
    }

    public void setCardShortDescription(CardShortDescription cardShortDescription) {
        this.cardShortDescription = cardShortDescription;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public TokenResponse getTokenResponse() {
        return tokenResponse;
    }

    public void setTokenResponse(TokenResponse tokenResponse) {
        this.tokenResponse = tokenResponse;
    }

    public String getAddTokenResponse() {
        return addTokenResponse;
    }

    public void setAddTokenResponse(String addTokenResponse) {
        this.addTokenResponse = addTokenResponse;
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
}
