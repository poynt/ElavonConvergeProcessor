package com.elavon.converge.model;

import com.elavon.converge.model.type.AccountType;
import com.elavon.converge.model.type.ElavonEntryMode;
import com.elavon.converge.model.type.ElavonPosMode;
import com.elavon.converge.model.type.PartialAuthIndicator;
import com.elavon.converge.model.type.SignatureImageType;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.math.BigDecimal;

/**
 * <txn>
 * <ssl_merchant_id>merchant_id</ssl_merchant_id>
 * <ssl_user_id>user_id</ssl_user_id>
 * <ssl_pin>pin</ssl_pin>
 * <ssl_test_mode>false</ssl_test_mode>
 * <ssl_transaction_type>ccsale</ssl_transaction_type>
 * <ssl_card_number>5472063333333330</ssl_card_number>
 * <ssl_exp_date>1225</ssl_exp_date>
 * <ssl_amount>10.00</ssl_amount>
 * <ssl_first_name>Test</ssl_first_name>
 * </txn>
 */
@Root(name = "txn")
public class ElavonTransactionRequest extends ElavonRequest {

    @Element(name = "poynt_user_id", required = false)
    private String poyntUserId;

    @Element(name = "ssl_txn_id", required = false)
    private String txnId;

    @Element(name = "ssl_merchant_txn_id", required = false)
    private String merchantTxnId;

    @Element(name = "ssl_reference_number", required = false)
    private String referenceNumber;

    @Element(name = "ssl_card_number", required = false)
    private String cardNumber;

    @Element(name = "ssl_token", required = false)
    private String token;

    /**
     * Do not send an expiration date with a token that is stored in the Card Manager.
     */
    @Element(name = "ssl_exp_date", required = false)
    private String expDate;

    @Element(name = "ssl_track_data", required = false)
    private String trackData;

    @Element(name = "ssl_enc_track_data", required = false)
    private String encryptedTrackData;

    @Element(name = "ssl_ksn", required = false)
    private String ksn;

    /**
     * When specifying amounts, be sure to submit the correct number of decimal places for the transaction currency.
     * For Japanese Yen, there are no currency exponents after the decimal place. Any numbers included
     * after the decimalplace will be dropped.
     * For those currencies with 3 possible digits, for example: Bahraini Dinar, we will automatically
     * round the transaction to two decimals. (forexample: 1.235 will round to 1.23).
     */
    @Element(name = "ssl_amount", required = false)
    private BigDecimal amount;

    @Element(name = "ssl_cashback_amount", required = false)
    private BigDecimal cashbackAmount;

    @Element(name = "ssl_cvv2cvc2_indicator", required = false)
    private String cvv2Indicator;

    @Element(name = "ssl_cvv2cvc2", required = false)
    private String cvv2;

    @Element(name = "ssl_first_name", required = false)
    private String firstName;

    @Element(name = "ssl_last_name", required = false)
    private String lastName;

    @Element(name = "ssl_account_type", required = false)
    private AccountType accountType;

    @Element(name = "ssl_card_present", required = false)
    private Boolean cardPresent;

    @Element(name = "ssl_avs_zip", required = false)
    private String avsZip;

    @Element(name = "ssl_invoice_number", required = false)
    private String invoiceNumber;

    @Element(name = "ssl_description", required = false)
    private String description;

    /**
     * Recommended for purchasing cards.
     * The Customer Code or PO Number that appears on the cardholder’s credit card billing statement.
     */
    @Element(name = "ssl_customer_code", required = false)
    private String customerCode;

    @Element(name = "ssl_salestax", required = false)
    private BigDecimal salesTax;

    @Element(name = "ssl_tip_amount", required = false)
    private BigDecimal tipAmount;

    /**
     * Use only with a terminal that is setup with DBA.
     * DBA name provided by the merchant with each transaction. The maximum allowable length of DBA
     * Name variable provided by the merchant can be 21, 17 or 12 based on the length setup for the DBA
     * constant in the field setup.
     */
    @Element(name = "ssl_dynamic_dba", required = false)
    private String dynamicDBA;

    @Element(name = "PartialAuthIndicator", required = false)
    private PartialAuthIndicator partialAuthIndicator;

    // ssl_departure_date - NOT USED (used for Travel Data)
    // ssl_completion_date - NOT USED
    // ssl_transaction_currency - NOT USED (multi-currency support on terminal required)

    @Element(name = "ssl_get_token", required = false)
    private Boolean generateToken;

    @Element(name = "ssl_pos_mode", required = false)
    private ElavonPosMode posMode;

    @Element(name = "ssl_entry_mode", required = false)
    private ElavonEntryMode entryMode;

    @Element(name = "ssl_tlv_enc", required = false)
    private String tlvEnc;

    @Element(name = "ssl_pin_block", required = false)
    private String pinBlock;

    @Element(name = "ssl_key_pointer", required = false)
    private String keyPointer;

    @Element(name = "ssl_dukpt", required = false)
    private String pinKsn;

    /**
     * Use only with a terminal that is setup with Tokenization.
     * Add to Card Manager indicator, used to indicate if you wish to generate a token and
     * store it in Card Manager. Defaults to false.
     * To add the token to the card manager you must send the card data and cardholder first/last name,
     * those are required. Once stored to Card Manager, the token number can be sent alone and will be
     * used as a substitute for the stored information.
     */
    @Element(name = "ssl_add_token", required = false)
    private Boolean generateAndStoreToken;

    @Element(name = "ssl_image_type", required = false)
    private SignatureImageType signatureImageType;

    /**
     * Base64 encoded image
     */
    @Element(name = "ssl_signature_image", required = false)
    private String signatureImage;


    @Element(name = "ssl_icc_isr", required = false)
    private String issuerScriptResults;

    @Element(name = "ssl_icc_tsi", required = false)
    private String transactionStatusInformation;

    @Element(name = "ssl_approval_code", required = false)
    private String approvalCode;

    @Element(name = "ssl_voucher_number", required = false)
    private String voucherNumber;

    @Element(name = "ssl_egc_tender_type", required = false)
    private String giftcardTenderType;

    @Element(name = "ssl_original_date", required = false)
    private String originalDate;

    @Element(name = "ssl_original_time", required = false)
    private String originalTime;

    // not part of xml
    private String cardLast4;

    public String getPoyntUserId() {
        return poyntUserId;
    }

    public void setPoyntUserId(String poyntUserId) {
        this.poyntUserId = poyntUserId;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getMerchantTxnId() {
        return merchantTxnId;
    }

    public void setMerchantTxnId(String merchantTxnId) {
        this.merchantTxnId = merchantTxnId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public ElavonEntryMode getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(ElavonEntryMode entryMode) {
        this.entryMode = entryMode;
    }

    public String getDynamicDBA() {
        return dynamicDBA;
    }

    public void setDynamicDBA(String dynamicDBA) {
        this.dynamicDBA = dynamicDBA;
    }

    public ElavonPosMode getPosMode() {
        return posMode;
    }

    public void setPosMode(ElavonPosMode posMode) {
        this.posMode = posMode;
    }

    public Boolean getGenerateAndStoreToken() {
        return generateAndStoreToken;
    }

    public void setGenerateAndStoreToken(Boolean generateAndStoreToken) {
        this.generateAndStoreToken = generateAndStoreToken;
    }

    public Boolean getGenerateToken() {
        return generateToken;
    }

    public void setGenerateToken(Boolean generateToken) {
        this.generateToken = generateToken;
    }

    public PartialAuthIndicator getPartialAuthIndicator() {
        return partialAuthIndicator;
    }

    public void setPartialAuthIndicator(PartialAuthIndicator partialAuthIndicator) {
        this.partialAuthIndicator = partialAuthIndicator;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
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

    public String getAvsZip() {
        return avsZip;
    }

    public void setAvsZip(String avsZip) {
        this.avsZip = avsZip;
    }

    public Boolean getCardPresent() {
        return cardPresent;
    }

    public void setCardPresent(Boolean cardPresent) {
        this.cardPresent = cardPresent;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getTrackData() {
        return trackData;
    }

    public void setTrackData(String trackData) {
        this.trackData = trackData;
    }

    public String getEncryptedTrackData() {
        return encryptedTrackData;
    }

    public void setEncryptedTrackData(String encryptedTrackData) {
        this.encryptedTrackData = encryptedTrackData;
    }

    public String getKsn() {
        return ksn;
    }

    public void setKsn(String ksn) {
        this.ksn = ksn;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getCashbackAmount() {
        return cashbackAmount;
    }

    public void setCashbackAmount(BigDecimal cashbackAmount) {
        this.cashbackAmount = cashbackAmount;
    }

    public String getCvv2Indicator() {
        return cvv2Indicator;
    }

    public void setCvv2Indicator(String cvv2Indicator) {
        this.cvv2Indicator = cvv2Indicator;
    }

    public String getCvv2() {
        return cvv2;
    }

    public void setCvv2(String cvv2) {
        this.cvv2 = cvv2;
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

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }

    public String getTlvEnc() {
        return tlvEnc;
    }

    public void setTlvEnc(String tlvEnc) {
        this.tlvEnc = tlvEnc;
    }

    public SignatureImageType getSignatureImageType() {
        return signatureImageType;
    }

    public void setSignatureImageType(SignatureImageType signatureImageType) {
        this.signatureImageType = signatureImageType;
    }

    public String getSignatureImage() {
        return signatureImage;
    }

    public void setSignatureImage(String signatureImage) {
        this.signatureImage = signatureImage;
    }

    public String getKeyPointer() {
        return keyPointer;
    }

    public void setKeyPointer(String keyPointer) {
        this.keyPointer = keyPointer;
    }

    public String getPinBlock() {
        return pinBlock;
    }

    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
    }

    public String getPinKsn() {
        return pinKsn;
    }

    public void setPinKsn(String pinKsn) {
        this.pinKsn = pinKsn;
    }

    public String getIssuerScriptResults() {
        return issuerScriptResults;
    }

    public void setIssuerScriptResults(String issuerScriptResults) {
        this.issuerScriptResults = issuerScriptResults;
    }

    public String getTransactionStatusInformation() {
        return transactionStatusInformation;
    }

    public void setTransactionStatusInformation(String transactionStatusInformation) {
        this.transactionStatusInformation = transactionStatusInformation;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public String getGiftcardTenderType() {
        return giftcardTenderType;
    }

    public void setGiftcardTenderType(String giftcardTenderType) {
        this.giftcardTenderType = giftcardTenderType;
    }

    public String getOriginalDate() {
        return originalDate;
    }

    public void setOriginalDate(String originalDate) {
        this.originalDate = originalDate;
    }

    public String getOriginalTime() {
        return originalTime;
    }

    public void setOriginalTime(String originalTime) {
        this.originalTime = originalTime;
    }
}
