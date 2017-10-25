package com.elavon.converge.model;

import org.simpleframework.xml.Element;

public enum ElavonTransactionType {
    SALE("ccsale"),
    AUTH_ONLY("ccauthonly"),
    AVS_ONLY("ccavsonly"),
    VERIFY("ccverify"),
    CREDIT("cccredit"),
    FORCE("ccforce"),
    VOID("ccvoid"),
    RETURN("ccreturn"),
    COMPLETE("cccomplete"),
    GET_TOKEN("ccgettoken"),
    DELETE("ccdelete"),
    UPDATE_TIP("ccupdatetip"),
    SIGNATURE("ccsignature"),
    ADD_RECURRING("ccaddrecurring"),
    UPDATE_RECURRING("ccupdaterecurring"),
    DELETE_RECURRING("ccdeleterecurring"),
    ADD_INSTALL("ccaddinstall"),
    RECURRING_SALE("ccrecurringsale"),
    DELETE_INSTALL("ccdeleteinstall"),
    INSTALL_SALE("ccinstallsale"),
    DEBIT_SALE("dbpurchase"),
    DEBIT_RETURN("dbreturn"),
    DEBIT_INQUIRY("dbbainquiry"),
    PINLESS_DEBIT_SALE("pldpurchase"),
    CASH_SALE("cashsale"),
    CASH_CREDIT("cashcredit"),
    EMV_CT_SALE("emvchipsale"),
    EMV_CT_AUTH_ONLY("emvchipauthonly"),
    EMV_SWIPE_SALE("emvswipesale"),
    EMV_SWIPE_AUTH_ONLY("emvswipeauthonly"),
    // The emvchipupdatetxn transaction is used to update the system with information from the chip card.
    // The Chip card may update the transaction
    //data after issuer information has been received from the emvchipsale results.
    EMV_CT_UPDATE("emvchipupdatetxn"),
    EMV_REVERSAL("emvreverse"),
    EMV_KEY_EXCHANGE("emvkeyexchange"),
    LEGACY_KEY_EXCHANGE("caddbkeyexchange"),
    EBT_SALE("fspurchase"),
    EBT_RETURN("fsreturn"),
    EBT_INQUIRY("fsbainquiry"),
    EBT_FORCE_SALE("fsforcepurchase"),
    EBT_FORCE_RETURN("fsforcereturn"),
    EBT_CASH_SALE("cbpurchase"),
    EBT_CASH_INQUIRY("cbbainquiry"),
    GIFT_CARD_ACTIVATION("egcactivation"),
    GIFT_CARD_SALE("egcsale"),
    GIFT_CARD_REFUND("egccardrefund"),
    GIFT_CARD_RELOAD("egcreload"),
    GIFT_CARD_INQUIRY("egcbalinquiry"),
    GIFT_CARD_CREDIT("egccredit"),
    GIFT_CARD_GET_TOKEN("egcgettoken"),
    LOYALTY_CARD_ENROLLMENT("ltenrollment"),
    LOYALTY_CARD_REDEMPTION("ltredeem"),
    LOYALTY_CARD_RETURN("ltreturn"),
    LOYALTY_CARD_ADD_POINTS("ltaddpoints"),
    LOYALTY_CARD_INQUIRY("ltinquiry"),
    LOYALTY_MEMBER_INQUIRY("ltmemberinquiry"),
    LOYALTY_VOID("ltvoid"),
    LOYALTY_CARD_DELETE("ltdelete"),
    TOKEN_QUERY("ccquerytoken"),
    TOKEN_UPDATE("ccupdatetoken"),
    TOKEN_DELETE("ccdeletetoken"),
    TRANSACTION_EMAIL("txnemail"),
    TRANSACTION_QUERY("txnquery"),
    SETTLE("settle"),
    CREDIT_BATCH_IMPORT("ccimport");

    @Element(name = "ssl_transaction_type")
    private final String value;

    ElavonTransactionType(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
