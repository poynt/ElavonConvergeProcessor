package com.elavon.converge.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.math.BigDecimal;

/**
 * <txn>
 * <ssl_merchant_id>009005</ssl_merchant_id>
 * <ssl_user_id>devportal</ssl_user_id>
 * <ssl_pin>BDDZY5KOUDCNPV4L3821K7PETO4Z7TPYOJB06TYBI1CW771IDHXBVBP51HZ6ZANJ</ssl_pin>
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
    @Element(name = "ssl_transaction_type")
    private ElavonTransactionType transactionType;
    @Element(name = "ssl_card_number", required = false)
    private String cardNumber;
    @Element(name = "ssl_exp_date", required = false)
    /**
     * Do not send an expiration date with a token that is stored in the Card Manager.
     */
    private String expDate;
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
    @Element(name = "ssl_cvv2cvc2_indicator", required = false)
    private String cvv2Indicator;
    @Element(name = "ssl_cvv2cvc2", required = false)
    private String cvv2;
    @Element(name = "ssl_first_name", required = false)
    private String firstName;

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

    public ElavonTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(ElavonTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
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
}
