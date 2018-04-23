package com.elavon.converge.model;

import com.elavon.converge.model.type.AVSResponse;
import com.elavon.converge.model.type.CVV2Response;
import com.elavon.converge.model.type.CardShortDescription;
import com.elavon.converge.model.type.CardType;
import com.elavon.converge.model.type.ElavonEntryMode;
import com.elavon.converge.model.type.TokenResponse;
import com.elavon.converge.model.type.TransactionStatus;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Sample Response:
 * <txn>
 * <ssl_card_short_description>MC</ssl_card_short_description>
 * <ssl_cvv2_response />
 * <ssl_account_balance>10.00</ssl_account_balance>
 * <ssl_result_message>APPROVAL</ssl_result_message>
 * <ssl_invoice_number />
 * <ssl_promo_code />
 * <ssl_result>0</ssl_result>
 * <ssl_txn_id>231017A15-67ADEA9E-4E0D-410A-B11C-61B0BE82DEB9</ssl_txn_id>
 * <ssl_completion_date />
 * <ssl_transaction_type>SALE</ssl_transaction_type>
 * <ssl_avs_response />
 * <ssl_account_status />
 * <ssl_approval_code>CMC345</ssl_approval_code>
 * <ssl_enrollment />
 * <ssl_exp_date>1225</ssl_exp_date>
 * <ssl_loyalty_program />
 * <ssl_tender_amount />
 * <ssl_departure_date />
 * <ssl_card_type>CREDITCARD</ssl_card_type>
 * <ssl_loyalty_account_balance />
 * <ssl_salestax />
 * <ssl_amount>10.00</ssl_amount>
 * <ssl_card_number>54**********3330</ssl_card_number>
 * <ssl_issue_points />
 * <ssl_txn_time>10/23/2017 06:12:25 PM</ssl_txn_time>
 * <ssl_access_code />
 * <ssl_trans_status>STL</ssl_trans_status>
 * </txn>
 */
@Root(name = "txn")
public class ElavonTransactionResponse extends ElavonResponse {

    @Element(name = "ssl_txn_id", required = false)
    private String txnId;

    @Element(name = "ssl_txn_time", required = false)
    private Date txnTime;

    // Transaction approval code
    @Element(name = "ssl_approval_code", required = false)
    private String approvalCode;

    @Element(name = "ssl_amount", required = false)
    private String amount;

    @Element(name = "ssl_base_amount", required = false)
    private String baseAmount;

    @Element(name = "ssl_cashback_amount", required = false)
    private String cashbackAmount;

    //The amount originally requested on partial approvals only
    @Element(name = "ssl_requested_amount", required = false)
    private String requestedAmount;

    // This is the difference of the amount requested versus the amount authorized that the merchant
    // has to collect from the consumer on partial approvals only
    @Element(name = "ssl_balance_due", required = false)
    private String balanceDue;

    // The balance left on the card, which is always 0.00 for a partially authorized transaction.
    @Element(name = "ssl_account_balance", required = false)
    private String accountBalance;

    @Element(name = "ssl_avs_response", required = false)
    private AVSResponse avsResponse;

    @Element(name = "ssl_cvv2_response", required = false)
    private CVV2Response cvv2Response;

    @Element(name = "ssl_card_number", required = false)
    private String cardNumberMasked;

    @Element(name = "ssl_invoice_number", required = false)
    private String invoiceNumber;

    @Element(name = "ssl_conversion_rate", required = false)
    private float conversionRate;

    @Element(name = "ssl_cardholder_currency", required = false)
    private String cardCurrency;

    // Total amount in cardholder currency, only returned on DCC transactions
    @Element(name = "ssl_cardholder_amount", required = false)
    private BigDecimal cardholderAmount;

    // Base amount in cardholder currency, only returned on DCC transactions.
    @Element(name = "ssl_cardholder_base_amount", required = false)
    private BigDecimal cardholderBaseAmount;

    // Tip amount in cardholder currency, only returned on DCC transactions with Service market segment
    @Element(name = "ssl_cardholder_tip_amount", required = false)
    private BigDecimal cardholderTipAmount;

    // Server ID submitted with the request. Only returned on Service market segment based on the merchant setup.
    @Element(name = "ssl_server", required = false)
    private String serverId;

    // Shift information submitted with the request. Only returned on Service market segment based on the merchant setup
    @Element(name = "ssl_shift", required = false)
    private String shift;

    // ssl_eci_ind - NOT USED. returned on 3D Secure transactions

    // Transaction currency. Returned only if terminal is setup for Multi- Currency
    @Element(name = "ssl_card_short_description", required = false)
    private CardShortDescription cardShortDescription;

    @Element(name = "ssl_card_type", required = false)
    private CardType cardType;

    @Element(name = "ssl_transaction_currency", required = false)
    private String transactionCurrency;

    @Element(name = "ssl_token", required = false)
    private String token;

    @Element(name = "ssl_token_response", required = false)
    private TokenResponse tokenResponse;

    //TODO convert to enum. Should verify return values with Elavon
    // Outcome of the Add to Card Manager request, examples: Card Added, Card Updated, Not Permitted, or
    // FAILURE - First Name - is required.
    // Returned only if storing token is requested in a terminal that setup for Tokenization.
    @Element(name = "ssl_add_token_response", required = false)
    private String addTokenResponse;

    @Element(name = "ssl_account_type", required = false)
    private String accountType;

    @Element(name = "ssl_icc_issuerscript", required = false)
    private String issuerScript;

    @Element(name = "ssl_icc_csn", required = false)
    private String csn;

    @Element(name = "ssl_icc_atc", required = false)
    private String atc;

    @Element(name = "ssl_issuer_response", required = false)
    private String issuerResponse;

    @Element(name = "ssl_icc_arpc", required = false)
    private String arpc;

    @Element(name = "ssl_update_emv_keys", required = false)
    private boolean updateEmvKeys;

    @Element(name = "ssl_icc_cardtype", required = false)
    private String iccCardType;

    @Element(name = "ssl_icc_cvmr", required = false)
    private String cvmr;

    @Element(name = "ssl_icc_aid", required = false)
    private String aid;

    @Element(name = "ssl_icc_tvr", required = false)
    private String tvr;

    @Element(name = "ssl_icc_tsi", required = false)
    private String tsi;

    @Element(name = "ssl_icc_app_arc", required = false)
    private String arc;

    @Element(name = "ssl_icc_app_name", required = false)
    private String appName;

    @Element(name = "ssl_card_scheme", required = false)
    private String cardScheme;

    @Element(name = "ssl_debit_response_code", required = false)
    private String debitResponseCode;

    @Element(name = "ssl_mac_key", required = false)
    private String interacMacKey;

    @Element(name = "ssl_pin_key", required = false)
    private String interacPinKey;

    @Element(name = "ssl_mac_value", required = false)
    private String interacMacValue;

    @Element(name = "ssl_mac_amount", required = false)
    private String interacMacAmount;

    @Element(name = "ssl_sys_trace_audit_no", required = false)
    private String interacSTAN;

    @Element(name = "ssl_processing_code", required = false)
    private String interacProcessingCode;

    @Element(name = "ssl_trans_status", required = false)
    private TransactionStatus transactionStatus;

    @Element(name = "ssl_first_name", required = false)
    private String firstName;

    @Element(name = "ssl_last_name", required = false)
    private String lastName;

    @Element(name = "ssl_user_id", required = false)
    private String userId;

    @Element(name = "ssl_salestax", required = false)
    private BigDecimal salesTax;

    @Element(name = "ssl_tip_amount", required = false)
    private BigDecimal tipAmount;

    @Element(name = "ssl_is_voidable", required = false)
    private String isVoidable;

    @Element(name = "ssl_entry_mode", required = false)
    private ElavonEntryMode entryMode;

    @Element(name = "ssl_merchant_txn_id", required = false)
    private String merchantTxnId;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public Date getTxnTime() {
        return txnTime;
    }

    public void setTxnTime(Date txnTime) {
        this.txnTime = txnTime;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(String baseAmount) {
        this.baseAmount = baseAmount;
    }

    public void setCashbackAmount(String cashbackAmount) {
        this.cashbackAmount = cashbackAmount;
    }

    public String getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(String requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public String getBalanceDue() {
        return balanceDue;
    }

    public void setBalanceDue(String balanceDue) {
        this.balanceDue = balanceDue;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(String accountBalance) {
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


    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public float getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(float conversionRate) {
        this.conversionRate = conversionRate;
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

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getArc() {
        return arc;
    }

    public void setArc(String arc) {
        this.arc = arc;
    }

    public String getArpc() {
        return arpc;
    }

    public void setArpc(String arpc) {
        this.arpc = arpc;
    }

    public String getAtc() {
        return atc;
    }

    public void setAtc(String atc) {
        this.atc = atc;
    }

    public void setCardNumberMasked(String cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public String getCsn() {
        return csn;
    }

    public void setCsn(String csn) {
        this.csn = csn;
    }

    public String getCvmr() {
        return cvmr;
    }

    public void setCvmr(String cvmr) {
        this.cvmr = cvmr;
    }

    public String getDebitResponseCode() {
        return debitResponseCode;
    }

    public void setDebitResponseCode(String debitResponseCode) {
        this.debitResponseCode = debitResponseCode;
    }


    public String getInteracMacAmount() {
        return interacMacAmount;
    }

    public void setInteracMacAmount(String interacMacAmount) {
        this.interacMacAmount = interacMacAmount;
    }

    public String getInteracMacKey() {
        return interacMacKey;
    }

    public void setInteracMacKey(String interacMacKey) {
        this.interacMacKey = interacMacKey;
    }

    public String getInteracMacValue() {
        return interacMacValue;
    }

    public void setInteracMacValue(String interacMacValue) {
        this.interacMacValue = interacMacValue;
    }

    public String getInteracPinKey() {
        return interacPinKey;
    }

    public void setInteracPinKey(String interacPinKey) {
        this.interacPinKey = interacPinKey;
    }

    public String getInteracProcessingCode() {
        return interacProcessingCode;
    }

    public void setInteracProcessingCode(String interacProcessingCode) {
        this.interacProcessingCode = interacProcessingCode;
    }

    public String getInteracSTAN() {
        return interacSTAN;
    }

    public void setInteracSTAN(String interacSTAN) {
        this.interacSTAN = interacSTAN;
    }

    public String getIssuerResponse() {
        return issuerResponse;
    }

    public void setIssuerResponse(String issuerResponse) {
        this.issuerResponse = issuerResponse;
    }

    public String getIssuerScript() {
        return issuerScript;
    }

    public void setIssuerScript(String issuerScript) {
        this.issuerScript = issuerScript;
    }

    public String getTsi() {
        return tsi;
    }

    public void setTsi(String tsi) {
        this.tsi = tsi;
    }

    public String getTvr() {
        return tvr;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public boolean isUpdateEmvKeys() {
        return updateEmvKeys;
    }

    public void setUpdateEmvKeys(boolean updateEmvKeys) {
        this.updateEmvKeys = updateEmvKeys;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    public String getIccCardType() {
        return iccCardType;
    }

    public void setIccCardType(String iccCardType) {
        this.iccCardType = iccCardType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getSalesTax() {
        return salesTax;
    }

    public void setSalesTax(BigDecimal salesTax) {
        this.salesTax = salesTax;
    }

    public BigDecimal getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(BigDecimal tipAmount) {
        this.tipAmount = tipAmount;
    }

    public String getIsVoidable() {
        return isVoidable;
    }

    public void setIsVoidable(String isVoidable) {
        this.isVoidable = isVoidable;
    }


    public String getMerchantTxnId() {
        return merchantTxnId;
    }

    public void setMerchantTxnId(String merchantTxnId) {
        this.merchantTxnId = merchantTxnId;
    }

    public ElavonEntryMode getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(ElavonEntryMode entryMode) {
        this.entryMode = entryMode;
    }

    public String getCashbackAmount() {
        return cashbackAmount;
    }

    @Override
    public String toString() {
        return "ElavonTransactionResponse{" +
                "txnId='" + txnId + '\'' +
                ", txnTime=" + txnTime +
                ", approvalCode='" + approvalCode + '\'' +
                ", amount=" + amount +
                ", baseAmount=" + baseAmount +
                ", cashbackAmount=" + cashbackAmount +
                ", requestedAmount=" + requestedAmount +
                ", balanceDue=" + balanceDue +
                ", accountBalance='" + accountBalance + '\'' +
                ", avsResponse=" + avsResponse +
                ", cvv2Response=" + cvv2Response +
                ", cardNumberMasked='" + cardNumberMasked + '\'' +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", conversionRate=" + conversionRate +
                ", cardCurrency='" + cardCurrency + '\'' +
                ", cardholderAmount=" + cardholderAmount +
                ", cardholderBaseAmount=" + cardholderBaseAmount +
                ", result='" + result + '\'' +
                ", resultMessage='" + resultMessage + '\'' +
                ", cardholderTipAmount=" + cardholderTipAmount +
                ", serverId='" + serverId + '\'' +
                ", errorCode=" + errorCode +
                ", errorName='" + errorName + '\'' +
                ", shift='" + shift + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", transactionType=" + transactionType +
                ", cardShortDescription=" + cardShortDescription +
                ", responseCode=" + responseCode +
                ", cardType=" + cardType +
                ", transactionCurrency='" + transactionCurrency + '\'' +
                ", token='" + token + '\'' +
                ", tokenResponse=" + tokenResponse +
                ", addTokenResponse='" + addTokenResponse + '\'' +
                ", accountType='" + accountType + '\'' +
                ", issuerScript='" + issuerScript + '\'' +
                ", csn='" + csn + '\'' +
                ", atc='" + atc + '\'' +
                ", issuerResponse='" + issuerResponse + '\'' +
                ", arpc='" + arpc + '\'' +
                ", updateEmvKeys=" + updateEmvKeys +
                ", iccCardType='" + iccCardType + '\'' +
                ", cvmr='" + cvmr + '\'' +
                ", aid='" + aid + '\'' +
                ", tvr='" + tvr + '\'' +
                ", tsi='" + tsi + '\'' +
                ", arc='" + arc + '\'' +
                ", appName='" + appName + '\'' +
                ", cardScheme='" + cardScheme + '\'' +
                ", debitResponseCode='" + debitResponseCode + '\'' +
                ", interacMacKey='" + interacMacKey + '\'' +
                ", interacPinKey='" + interacPinKey + '\'' +
                ", interacMacValue='" + interacMacValue + '\'' +
                ", interacMacAmount='" + interacMacAmount + '\'' +
                ", interacSTAN='" + interacSTAN + '\'' +
                ", interacProcessingCode='" + interacProcessingCode + '\'' +
                ", transactionStatus=" + transactionStatus +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userId='" + userId + '\'' +
                ", salesTax=" + salesTax +
                ", tipAmount=" + tipAmount +
                ", isVoidable='" + isVoidable + '\'' +
                ", entryMode=" + entryMode +
                ", merchantTxnId='" + merchantTxnId + '\'' +
                '}';
    }
}
