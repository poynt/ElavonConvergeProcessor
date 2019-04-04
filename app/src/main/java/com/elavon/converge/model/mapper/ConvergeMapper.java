package com.elavon.converge.model.mapper;

import android.util.Base64;
import android.util.Log;

import co.poynt.api.model.Business;
import co.poynt.api.model.ClientContext;
import com.elavon.converge.ElavonConvergeProcessorApplication;
import com.elavon.converge.exception.ConvergeMapperException;
import com.elavon.converge.model.ElavonResponse;
import com.elavon.converge.model.ElavonSettleRequest;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.ElavonTransactionSearchRequest;
import com.elavon.converge.model.type.AVSResponse;
import com.elavon.converge.model.type.CVV2Response;
import com.elavon.converge.model.type.CardType;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.model.type.ResponseCodes;
import com.elavon.converge.model.type.SignatureImageType;
import com.elavon.converge.util.CurrencyUtil;
import com.elavon.converge.util.HexDump;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import co.poynt.api.model.AVSResult;
import co.poynt.api.model.AVSResultType;
import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.CVResult;
import co.poynt.api.model.EMVData;
import co.poynt.api.model.EMVTag;
import co.poynt.api.model.EntryMode;
import co.poynt.api.model.FundingSource;
import co.poynt.api.model.FundingSourceAccountType;
import co.poynt.api.model.FundingSourceEntryDetails;
import co.poynt.api.model.FundingSourceType;
import co.poynt.api.model.Processor;
import co.poynt.api.model.ProcessorResponse;
import co.poynt.api.model.ProcessorStatus;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionAction;
import co.poynt.api.model.TransactionAmounts;
import co.poynt.api.model.TransactionReference;
import co.poynt.api.model.TransactionStatus;
import co.poynt.os.util.StringUtil;

import static co.poynt.api.model.EntryMode.CONTACTLESS_INTEGRATED_CIRCUIT_CARD;
import static co.poynt.api.model.EntryMode.INTEGRATED_CIRCUIT_CARD;

public class ConvergeMapper {

    private static final String TAG = ConvergeMapper.class.getSimpleName();

    private static final String DEFAULT_CURRENCY = "USD";

    private final MsrMapper msrMapper;
    private final MsrDebitMapper msrDebitMapper;
    private final MsrEbtMapper msrEbtMapper;
    private final MsrGiftcardMapper msrGiftcardMapper;
    private final EmvMapper emvMapper;
    private final KeyedMapper keyedMapper;
    private final KeyedEbtMapper keyedEbtMapper;
    private final KeyedGiftcardMapper keyedGiftcardMapper;
    private final CashMapper cashMapper;

    @Inject
    public ConvergeMapper(
            final MsrMapper msrMapper,
            final MsrDebitMapper msrDebitMapper,
            final MsrEbtMapper msrEbtMapper,
            final MsrGiftcardMapper msrGiftcardMapper,
            final EmvMapper emvMapper,
            final KeyedMapper keyedMapper,
            final KeyedEbtMapper keyedEbtMapper,
            final KeyedGiftcardMapper keyedGiftcardMapper,
            final CashMapper cashMapper) {
        this.msrMapper = msrMapper;
        this.msrDebitMapper = msrDebitMapper;
        this.msrEbtMapper = msrEbtMapper;
        this.msrGiftcardMapper = msrGiftcardMapper;
        this.emvMapper = emvMapper;
        this.keyedMapper = keyedMapper;
        this.keyedEbtMapper = keyedEbtMapper;
        this.keyedGiftcardMapper = keyedGiftcardMapper;
        this.cashMapper = cashMapper;
    }

    private InterfaceMapper getMapper(final FundingSource fundingSource) {
        if (fundingSource.getType() == FundingSourceType.CASH) {
            return cashMapper;
        } else {
            switch (fundingSource.getEntryDetails().getEntryMode()) {
                case TRACK_DATA_FROM_MAGSTRIPE:
                case CONTACTLESS_MAGSTRIPE:
                    if (Boolean.TRUE.equals(fundingSource.isDebit())) {
                        return msrDebitMapper;
                    } else if (fundingSource.getAccountType() == FundingSourceAccountType.EBT) {
                        return msrEbtMapper;
                    } else if (isGiftCard(fundingSource)) {
                        return msrGiftcardMapper;
                    } else {
                        return msrMapper;
                    }
                case INTEGRATED_CIRCUIT_CARD:
                case CONTACTLESS_INTEGRATED_CIRCUIT_CARD:
                    return emvMapper;
                case KEYED:
                    if (fundingSource.getAccountType() == FundingSourceAccountType.EBT) {
                        return keyedEbtMapper;
                    } else if (isGiftCard(fundingSource)) {
                        return keyedGiftcardMapper;
                    } else {
                        return keyedMapper;
                    }
                default:
                    throw new ConvergeMapperException("Invalid entry mode found");
            }
        }
    }

    private boolean isGiftCard(final FundingSource fundingSource) {
        // TODO need to implement this
        return false;
    }

    public ElavonTransactionRequest getTransactionRequest(final Transaction transaction) {
        Log.d(TAG, "Transaction Request:" + transaction);
        final InterfaceMapper mapper = getMapper(transaction.getFundingSource());
        final ElavonTransactionRequest request;
        TransactionAmounts amounts = transaction.getAmounts();
        switch (transaction.getAction()) {
            case AUTHORIZE:
                // if amount is 0 - then it should be verification request
                if (amounts != null &&
                        (amounts.getTransactionAmount() == null
                                || amounts.getTransactionAmount() == 0l)) {
                    request = mapper.createVerify(transaction);
                } else {
                    request = mapper.createAuth(transaction);
                }
                break;
            case REFUND:
                request = mapper.createRefund(transaction);
                break;
            case SALE:
                // if amount is 0 - then it should be verification request
                if (amounts != null &&
                        (amounts.getTransactionAmount() == null
                                || amounts.getTransactionAmount() == 0l)) {
                    request = mapper.createVerify(transaction);
                } else {
                    request = mapper.createSale(transaction);
                }
                break;
            case VERIFY:
                request = mapper.createVerify(transaction);
                break;
            default:
                throw new ConvergeMapperException("Invalid transaction action found");
        }
        request.setInvoiceNumber(getReference(transaction, "invoiceId"));
        // we always use the merchant_txn_id as our unique identifier to bind poynt txn w/ converge txn
        if (transaction.getId() != null) {
            request.setMerchantTxnId(transaction.getId().toString());
        } else {
            request.setMerchantTxnId(UUID.randomUUID().toString());
        }
        return request;
    }

    private String getReference(final Transaction t, final String type) {
        if (t.getReferences() != null) {
            for (final TransactionReference ref : t.getReferences()) {
                if (type.equals(ref.getCustomType())) {
                    return ref.getId();
                }
            }
        }
        return null;
    }

    public ElavonTransactionRequest getTransactionUpdateRequest(FundingSourceEntryDetails entryDetails,
                                                                final String transactionId,
                                                                final AdjustTransactionRequest adjustTransactionRequest) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        if (entryDetails != null
                && (entryDetails.getEntryMode() == INTEGRATED_CIRCUIT_CARD
                || entryDetails.getEntryMode() == CONTACTLESS_INTEGRATED_CIRCUIT_CARD)) {
            request.setTransactionType(ElavonTransactionType.EMV_CT_UPDATE);
            // add signature - only for emv update
            if (adjustTransactionRequest.getSignature() != null) {
                request.setSignatureImage(Base64.encodeToString(
                        adjustTransactionRequest.getSignature(), Base64.DEFAULT));
                request.setSignatureImageType(SignatureImageType.PNG);
            }
        } else {
            request.setTransactionType(ElavonTransactionType.UPDATE_TIP);
            // no signature for ccupdatetip
        }
        // elavon transactionId
        request.setTxnId(transactionId);
        // update tip if customer did not opted No Tip
        if (adjustTransactionRequest.getAmounts() != null
                && adjustTransactionRequest.getAmounts().isCustomerOptedNoTip() != Boolean.TRUE) {
            request.setTipAmount(CurrencyUtil.getAmount(adjustTransactionRequest.getAmounts().getTipAmount(),
                    adjustTransactionRequest.getAmounts().getCurrency()));
        }
        // add emv tags
        if (adjustTransactionRequest.getEmvData() != null) {
            EMVData emvData = adjustTransactionRequest.getEmvData();
            Map<String, String> emvTags = emvData.getEmvTags();
            for (final Map.Entry<String, String> tag : emvTags.entrySet()) {
                Log.d(TAG, String.format("%s=%s", tag.getKey(), tag.getValue()));
            }

            if (emvTags != null && emvTags.size() > 0) {
                if (emvTags.containsKey("0xE012")) {
                    request.setIssuerScriptResults(emvTags.get("0xE012"));
                }
                if (emvTags.containsKey("0x9B")) {
                    request.setTransactionStatusInformation(emvTags.get("0x9B"));
                }
            }
        }
        return request;
    }

    public ElavonTransactionRequest getUpdateSignatureRequest(FundingSourceEntryDetails entryDetails,
                                                              final String transactionId,
                                                              final AdjustTransactionRequest adjustTransactionRequest) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.SIGNATURE);

        // elavon transactionId
        request.setTxnId(transactionId);
        // update tip if customer did not opted No Tip
        if (adjustTransactionRequest.getAmounts() != null
                && adjustTransactionRequest.getAmounts().isCustomerOptedNoTip() != Boolean.TRUE) {
            request.setTipAmount(CurrencyUtil.getAmount(adjustTransactionRequest.getAmounts().getTipAmount(),
                    adjustTransactionRequest.getAmounts().getCurrency()));
        }
        // add emv tags
        if (adjustTransactionRequest.getEmvData() != null) {
            EMVData emvData = adjustTransactionRequest.getEmvData();
            Map<String, String> emvTags = emvData.getEmvTags();
            for (final Map.Entry<String, String> tag : emvTags.entrySet()) {
                Log.d(TAG, String.format("%s=%s", tag.getKey(), tag.getValue()));
            }

            if (emvTags != null && emvTags.size() > 0) {
                if (emvTags.containsKey("0xE012")) {
                    request.setIssuerScriptResults(emvTags.get("0xE012"));
                }
                if (emvTags.containsKey("0x9B")) {
                    request.setTransactionStatusInformation(emvTags.get("0x9B"));
                }
            }
        }

        // add signature
        if (adjustTransactionRequest.getSignature() != null) {
            request.setSignatureImage(Base64.encodeToString(
                    adjustTransactionRequest.getSignature(), Base64.DEFAULT));
            request.setSignatureImageType(SignatureImageType.PNG);
        }

        return request;
    }

    public ElavonTransactionRequest getTransactionCompleteRequest(final FundingSource fundingSource,
                                                                  final String transactionId,
                                                                  final AdjustTransactionRequest adjustTransactionRequest) {
        final InterfaceMapper mapper = getMapper(fundingSource);
        return mapper.createCapture(transactionId, adjustTransactionRequest);
    }

    public ElavonTransactionSearchRequest getSearchRequest(final String transactionId) {
        final ElavonTransactionSearchRequest search = new ElavonTransactionSearchRequest();
        search.setTestMode("false");
        search.setTransactionType(ElavonTransactionType.TRANSACTION_QUERY);
        search.setTransactionId(transactionId);
        return search;
    }

    public ElavonTransactionSearchRequest getSearchRequest(final String cardLast4,
                                                           final Date searchStartDate) {
        final ElavonTransactionSearchRequest search = new ElavonTransactionSearchRequest();
        search.setTestMode("false");
        search.setTransactionType(ElavonTransactionType.TRANSACTION_QUERY);
        search.setCardSuffix(cardLast4);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        search.setSearchStartDate(dateFormat.format(searchStartDate));
        return search;
    }

    public ElavonTransactionSearchRequest getSearchRequest(final String searchStartDate,
                                                           final String searchEndDate) {
        final ElavonTransactionSearchRequest search = new ElavonTransactionSearchRequest();
        search.setTestMode("false");
        search.setTransactionType(ElavonTransactionType.TRANSACTION_QUERY);
        if (searchStartDate != null) {
            search.setSearchStartDate(searchStartDate);
        }
        if (searchEndDate != null) {
            search.setSearchEndDate(searchEndDate);
        }
        return search;
    }

    public ElavonTransactionSearchRequest getSearchRequest(final String transactionType,
                                                           final String searchStartDate,
                                                           final String searchEndDate) {
        final ElavonTransactionSearchRequest search = new ElavonTransactionSearchRequest();
        search.setTestMode("false");
        search.setTransactionType(ElavonTransactionType.TRANSACTION_QUERY);
        search.setSearchTransactionType(transactionType);
        if (searchStartDate != null) {
            search.setSearchStartDate(searchStartDate);
        }
        if (searchEndDate != null) {
            search.setSearchEndDate(searchEndDate);
        }
        return search;
    }

    public ElavonTransactionSearchRequest getSearchByMerchantTransactionIdRequest(
            final String merchantTransactionId, final Date searchStartDate) {
        final ElavonTransactionSearchRequest search = new ElavonTransactionSearchRequest();
        search.setTestMode("false");
        search.setTransactionType(ElavonTransactionType.TRANSACTION_QUERY);
        search.setMerchantTxnId(merchantTransactionId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        search.setSearchStartDate(dateFormat.format(searchStartDate));
        return search;
    }

    public ElavonTransactionRequest getTransactionReversalRequest(final FundingSource fundingSource,
                                                                  final String transactionId) {
        final InterfaceMapper mapper = getMapper(fundingSource);
        return mapper.createReverse(transactionId);
    }

    public ElavonTransactionRequest getTransactionVoidRequest(final Transaction transaction,
                                                              final String transactionId) {
        final InterfaceMapper mapper = getMapper(transaction.getFundingSource());
        return mapper.createVoid(transaction, transactionId);
    }

    public ElavonTransactionRequest getBalanceInquiryRequest(final BalanceInquiry balanceInquiry) {
        Log.d(TAG, "Balance Inquiry Request");
        final InterfaceMapper mapper = getMapper(balanceInquiry.getFundingSource());
        return mapper.createBalanceInquiry(balanceInquiry);
    }

    /**
     * <pre><code>
     * Example Transaction:
     * <txn>
     *   <ssl_card_short_description>MC</ssl_card_short_description>
     *   <ssl_cvv2_response />
     *   <ssl_account_balance>0.00</ssl_account_balance>
     *   <ssl_result_message>APPROVAL</ssl_result_message>
     *   <ssl_invoice_number />
     *   <ssl_promo_code />
     *   <ssl_result>0</ssl_result>
     *   <ssl_txn_id>271017A15-D11434F9-B6AE-4312-A9BB-034F961636AB</ssl_txn_id>
     *   <ssl_completion_date />
     *   <ssl_transaction_type>SALE</ssl_transaction_type>
     *   <ssl_avs_response />
     *   <ssl_account_status />
     *   <ssl_approval_code>CMC648</ssl_approval_code>
     *   <ssl_enrollment />
     *   <ssl_exp_date>1225</ssl_exp_date>
     *   <ssl_loyalty_program />
     *   <ssl_tender_amount />
     *   <ssl_departure_date />
     *   <ssl_card_type>CREDITCARD</ssl_card_type>
     *   <ssl_loyalty_account_balance />
     *   <ssl_salestax />
     *   <ssl_amount>5.20</ssl_amount>
     *   <ssl_card_number>54**********3330</ssl_card_number>
     *   <ssl_issue_points />
     *   <ssl_txn_time>10/27/2017 01:37:53 PM</ssl_txn_time>
     *   <ssl_access_code />
     * </txn>
     * </code></pre>
     */
    public void mapTransactionResponse(final ElavonTransactionResponse etResponse, final Transaction transaction) {

        Log.d(TAG, "Received response:" + etResponse);

        final ProcessorResponse processorResponse = new ProcessorResponse();
        /*
         * As requested in AX-1963 Converge: set PROCESSOR=CONVERGE, not ELAVON
         * Changed from Processor.ELAVON to Processor.CONVERGE
         */
        processorResponse.setProcessor(Processor.CONVERGE);
        processorResponse.setAcquirer(Processor.ELAVON);

        // always generate a hash of the card info
        if (transaction.getFundingSource() != null && transaction.getFundingSource().getCard() != null) {
            // set number hash if it's not already a card token
            if (transaction.getFundingSource().getEntryDetails().getEntryMode() != EntryMode.KEYED) {
                transaction.getFundingSource().getCard().setNumberHashed(
                        generateHash(transaction.getFundingSource().getCard().getCardHolderFullName(),
                                transaction.getFundingSource().getCard().getNumberFirst6(),
                                transaction.getFundingSource().getCard().getNumberLast4(),
                                transaction.getFundingSource().getCard().getEncryptedExpirationDate())
                );
            } else {
                // for keyed transaction card token is already set as number hash for converge
            }
            // remove sensitive fields from getting recorded in Poynt
            if (transaction.getFundingSource().getEntryDetails().getEntryMode() == EntryMode.KEYED) {
                transaction.getFundingSource().getCard().setNumber(null);
                if (transaction.getFundingSource().getVerificationData() != null) {
                    transaction.getFundingSource().getVerificationData().setCvData(null);
                }
            }
            // HACK: Poynt Server require 4 digit expiry year :-/
            // NOTE: If it is 2-digit (yy) then the year is set to be between 80 years before and
            // 20 years after the date the SimpleDateFormat instance is created
            if (transaction.getFundingSource().getCard().getExpirationYear() != null) {
                String expiryYear = transaction.getFundingSource().getCard().getExpirationYear().toString();
                SimpleDateFormat sdfmt1 = new SimpleDateFormat("yy");
                SimpleDateFormat sdfmt2 = new SimpleDateFormat("yyyy");
                try {
                    Date dDate = sdfmt1.parse(expiryYear);
                    String strOutput = sdfmt2.format(dDate);
                    Log.d(TAG, "Converting year from " + expiryYear + " to " + strOutput);
                    transaction.getFundingSource().getCard().setExpirationYear(Integer.parseInt(strOutput));
                } catch (ParseException e) {
                    Log.e(TAG, "Failed to convert expiry year from YY to YYYY", e);
                }

            }
        }

        setStatusResponse(processorResponse, etResponse);

        if (etResponse.getResponseCode() == ResponseCodes.AA
                || ElavonResponse.RESULT_MESSAGE.APPROVAL.equals(etResponse.getResultMessage())) {
            switch (transaction.getAction()) {
                case AUTHORIZE:
                    transaction.setStatus(TransactionStatus.AUTHORIZED);
                    break;
                case SALE:
                case CAPTURE:
                    transaction.setStatus(TransactionStatus.CAPTURED);
                    break;
                case VOID:
                    transaction.setStatus(TransactionStatus.VOIDED);
                    break;
                case OFFLINE_AUTHORIZE:
                    transaction.setStatus(TransactionStatus.AUTHORIZED);
                    break;
                case REFUND:
                    transaction.setStatus(TransactionStatus.REFUNDED);
                    break;
                case VERIFY:
                    break;
                default:
                    throw new ConvergeMapperException("Invalid transaction action found");
            }
        } else if (etResponse.getResponseCode() == ResponseCodes.AP
                || ElavonResponse.RESULT_MESSAGE.PARTIAL_APPROVAL.equals(etResponse.getResultMessage())) {
            switch (transaction.getAction()) {
                case AUTHORIZE:
                    transaction.setStatus(TransactionStatus.AUTHORIZED);
                    break;
                case SALE:
                case CAPTURE:
                    transaction.setStatus(TransactionStatus.CAPTURED);
                    break;
                case VOID:
                    transaction.setStatus(TransactionStatus.VOIDED);
                    break;
                case OFFLINE_AUTHORIZE:
                    transaction.setStatus(TransactionStatus.AUTHORIZED);
                    break;
                case REFUND:
                    transaction.setStatus(TransactionStatus.REFUNDED);
                    break;
                case VERIFY:
                    break;
                default:
                    throw new ConvergeMapperException("Invalid transaction action found");
            }
        } else if (etResponse.getResponseCode() == ResponseCodes.NR
                || ElavonResponse.RESULT_MESSAGE.CALL_AUTH_CENTER.equals(etResponse.getResultMessage())) {
            // TODO : when referral is required what's the status should be
            switch (transaction.getAction()) {
                case AUTHORIZE:
                    transaction.setStatus(TransactionStatus.AUTHORIZED);
                    break;
                case SALE:
                case CAPTURE:
                    transaction.setStatus(TransactionStatus.CAPTURED);
                    break;
                case VOID:
                    transaction.setStatus(TransactionStatus.VOIDED);
                    break;
                case OFFLINE_AUTHORIZE:
                    transaction.setStatus(TransactionStatus.AUTHORIZED);
                    break;
                case REFUND:
                    transaction.setStatus(TransactionStatus.REFUNDED);
                    break;
                case VERIFY:
                    break;
                default:
                    throw new ConvergeMapperException("Invalid transaction action found");
            }
        } else {
            // decline
            switch (transaction.getAction()) {
                case AUTHORIZE:
                    transaction.setStatus(TransactionStatus.DECLINED);
                    break;
                case SALE:
                case CAPTURE:
                    transaction.setStatus(TransactionStatus.DECLINED);
                    break;
                case VOID:
                    transaction.setStatus(TransactionStatus.DECLINED);
                    break;
                case OFFLINE_AUTHORIZE:
                    transaction.setStatus(TransactionStatus.DECLINED);
                    break;
                case REFUND:
                    transaction.setStatus(TransactionStatus.DECLINED);
                    break;
                case VERIFY:
                    break;
                default:
                    throw new ConvergeMapperException("Invalid transaction action found");
            }
        }

        setTransactionIdResponse(processorResponse, etResponse);

        if (etResponse.getApprovalCode() != null) {
            processorResponse.setApprovalCode(etResponse.getApprovalCode());
        }

        if(!StringUtil.isEmpty(etResponse.getAmount())) {
            processorResponse.setApprovedAmount(CurrencyUtil.getAmount(etResponse.getAmount(),
                    transaction.getAmounts().getCurrency()));
        }

        if (etResponse.getResponseCode() == ResponseCodes.AA
                || etResponse.getResponseCode() == ResponseCodes.AP
                || ElavonResponse.RESULT_MESSAGE.APPROVAL.equals(etResponse.getResultMessage())
                || ElavonResponse.RESULT_MESSAGE.PARTIAL_APPROVAL.equals(etResponse.getResultMessage())) {
            // don't set amounts for cash transaction types
            if (etResponse.getCardType() != CardType.CASH) {
                // update the transaction amount
                TransactionAmounts amounts = transaction.getAmounts();
                if (etResponse.getAmount() != null) {
                    amounts.setTransactionAmount(CurrencyUtil.getAmount(etResponse.getAmount(),
                            transaction.getAmounts().getCurrency()));
                    if (etResponse.getCashbackAmount() != null) {
                        amounts.setCashbackAmount(CurrencyUtil.getAmount(etResponse.getCashbackAmount(),
                                transaction.getAmounts().getCurrency()));
                    }
                } else if (etResponse.getBaseAmount() != null) {
                    amounts.setTransactionAmount(CurrencyUtil.getAmount(etResponse.getBaseAmount(),
                            transaction.getAmounts().getCurrency()));
                    if (etResponse.getCashbackAmount() != null) {
                        amounts.setCashbackAmount(CurrencyUtil.getAmount(etResponse.getCashbackAmount(),
                                transaction.getAmounts().getCurrency()));
                    }
                }
            }
        }

        // set  EMV response tags - if it's EMV transaction
        if (transaction != null
                && transaction.getFundingSource() != null
                && transaction.getFundingSource().getEntryDetails() != null
                && (transaction.getFundingSource().getEntryDetails().getEntryMode()
                == INTEGRATED_CIRCUIT_CARD
                || transaction.getFundingSource().getEntryDetails().getEntryMode()
                == CONTACTLESS_INTEGRATED_CIRCUIT_CARD)) {
            Map<String, String> emvTags = new HashMap<>();

            // NOTE do not pass CSN, atc and other non relevant tags in response as Poynt
            // firmware might fail the transaction when it receives unexpected tags

            // arpc
            if (etResponse.getArpc() != null) {
                emvTags.put("0x91", etResponse.getArpc());
            }

            if (etResponse.getIssuerScript() != null) {
                String fullField = etResponse.getIssuerScript();
                String tagNo = fullField.substring(0, 2);
                emvTags.put("0x" + tagNo, fullField.substring(4));
            }

//            if (er.hasField("ICCIssuerScript") && er.getField("ICCIssuerScript") != null
//                    && er.getField("ICCIssuerScript").length() >= 4) {
//                String fullField = er.getField("ICCIssuerScript");
//                String tagNo = fullField.substring(0, 2);
//                String scriptFieldLength = fullField.substring(2, 4);
//                emvTags.put("0x" + tagNo, fullField.substring(4));
//            } else {
//                // emvTags.put("0x71", "");
//            }

            if (etResponse.getArc() != null) {
                emvTags.put(EMVTag.RESPONSE_AUTHORIZATION_RESPONSE_CODE.tag(),
                        numberToAsciiHex(etResponse.getArc().toCharArray()));
            } else {
                if (etResponse.getResponseCode() == ResponseCodes.AA) {
                    emvTags.put(EMVTag.RESPONSE_AUTHORIZATION_RESPONSE_CODE.tag(), "3030");
                } else if (etResponse.getResponseCode() == ResponseCodes.AP) {
                    // for partial approval, firmware team wants us to pass Approval..
                    emvTags.put(EMVTag.RESPONSE_AUTHORIZATION_RESPONSE_CODE.tag(), "3030");
                } else if (etResponse.getResponseCode() == ResponseCodes.NR) {
                    emvTags.put(EMVTag.RESPONSE_AUTHORIZATION_RESPONSE_CODE.tag(), "3031");
                } else {
                    emvTags.put(EMVTag.RESPONSE_AUTHORIZATION_RESPONSE_CODE.tag(), "3035");
                }
            }
            processorResponse.setEmvTags(emvTags);
        }
        if (etResponse.getCvv2Response() != null) {
            processorResponse.setCvResult(mapCvResponse(etResponse.getCvv2Response()));
            processorResponse.setCvActualResult(etResponse.getCvv2Response().getValue());
        }
        if (etResponse.getAvsResponse() != null) {
            processorResponse.setAvsResult(mapAvsResponse(etResponse.getAvsResponse()));
        }
        transaction.setProcessorResponse(processorResponse);

        // make sure the transactionId in Poynt is same as merchant-txn-id
        if (StringUtil.notEmpty(etResponse.getMerchantTxnId())) {
            transaction.setId(UUID.fromString(etResponse.getMerchantTxnId()));
        } else if (transaction.getId() == null) {
            transaction.setId(UUID.randomUUID());
        }

        if (transaction.isSignatureCaptured() == null) {
            transaction.setSignatureCaptured(false);
        }

        if (transaction.getAction() == TransactionAction.CAPTURE &&
                transaction.getStatus() == TransactionStatus.CAPTURED) {
            transaction.setParentId(transaction.getId());
            transaction.setId(UUID.randomUUID());
        }

        Business business = ElavonConvergeProcessorApplication.getInstance().getBusiness();
        if (transaction.getContext() == null) {
            ClientContext clientContext = new ClientContext();
            clientContext.setEmployeeUserId(0L);
            if (business != null) {
                clientContext.setBusinessId(business.getId());
            }
            transaction.setContext(clientContext);
        } else {
            ClientContext clientContext = transaction.getContext();
            if (clientContext.getBusinessId() == null) {
                if (business != null) {
                    clientContext.setBusinessId(business.getId());
                }
            }
        }
    }

    private void setStatusResponse(
            final ProcessorResponse processorResponse,
            final ElavonTransactionResponse etResponse) {

        processorResponse.setStatus(etResponse.isSuccess() ? ProcessorStatus.Successful : ProcessorStatus.Failure);
        processorResponse.setStatusCode(etResponse.getResult());

        if (etResponse.getResultMessage() != null) {
            processorResponse.setStatusMessage(etResponse.getResultMessage());
        } else if (etResponse.getErrorMessage() != null) {
            processorResponse.setStatusMessage(etResponse.getErrorMessage());
        } else {
            processorResponse.setStatusMessage(Integer.toString(etResponse.getErrorCode()));
        }
    }

    private void setTransactionIdResponse(
            final ProcessorResponse processorResponse,
            final ElavonTransactionResponse etResponse) {
        // TODO currently there is issue with processor response transaction id overwritten
        // TODO with transaction id. using retrieval ref num to store converge transaction id
        if (etResponse.getTxnId() != null) {
            processorResponse.setTransactionId(etResponse.getTxnId());
            processorResponse.setRetrievalRefNum(etResponse.getTxnId());
        } else {
            //TODO - our API Service requires a processor transactionId even for declines
            processorResponse.setTransactionId(UUID.randomUUID().toString());
            //TODO - what do we do for retrieval reference number
            // may be when it doesn't exist we can block the call here
        }
    }

    private CVResult mapCvResponse(final CVV2Response cvv2Response) {
        switch (cvv2Response) {
            case MATCH:
                return CVResult.MATCH;
            case NO_MATCH:
                return CVResult.NO_MATCH;
            case NOT_PROCESSED:
                return CVResult.NOT_PROCESSED;
            case CVV_EXPECTED:
                return CVResult.SHOULD_HAVE_BEEN_PRESENT;
            case NOT_AVAILABLE:
                return CVResult.ISSUER_NOT_CERTIFIED;
        }
        return CVResult.INVALID;
    }

    private AVSResult mapAvsResponse(final AVSResponse avsResponse) {
        final AVSResult avsResult = new AVSResult();
        switch (avsResponse) {
            case MATCH:
            case MATCH_INTERNATIONAL:
            case MATCH_INTERNATIONAL_INTL:
            case MATCH_UK:
            case STREET_ZIP5_MATCH:
                avsResult.setAddressResult(AVSResultType.MATCH);
                break;
            case STREET_MATCH:
            case STREET_MATCH_MALFORMED_ZIP:
            case ZIP_MATCH:
            case ZIP9_MATCH:
            case ZIP5_MATCH:
                avsResult.setAddressResult(AVSResultType.PARTIAL_MATCH);
                break;
            case MALFORMED_STREET_ZIP:
                avsResult.setAddressResult(AVSResultType.BAD_FORMAT);
                break;
            case AVS_ERROR:
                avsResult.setAddressResult(AVSResultType.ERROR);
                break;
            case NOT_VERIFIED:
                avsResult.setAddressResult(AVSResultType.NOT_VERIFIED);
                break;
            case NO_MATCH:
                avsResult.setAddressResult(AVSResultType.NO_MATCH);
                break;
            case NO_RESPONSE:
                avsResult.setAddressResult(AVSResultType.NO_RESPONSE_FROM_CARD_ASSOCIATION);
                break;
            case NOT_SUPPORTED_INTL:
            case NOT_SUPPORTED:
                avsResult.setAddressResult(AVSResultType.UNSUPPORTED_BY_ISSUER);
                break;
            case UNAVAILABLE:
            case ADDRESS_UNAVAILABLE:
                avsResult.setAddressResult(AVSResultType.UNAVAILABLE);
                break;
            default:
                avsResult.setAddressResult(AVSResultType.ERROR);
        }
        avsResult.setActualResult(avsResponse.getValue());
        return avsResult;
    }

    private String numberToAsciiHex(char[] numberChars) {
        String result = "";

        for (char c : numberChars) {
            try {
                result += Integer.toHexString((int) c);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return "3030";
            }
        }
        return result;
    }

    private String generateHash(String name, String first6, String last4, String expiry) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            if (name != null) {
                md.update(name.getBytes("UTF-8"));
            }
            if (first6 != null) {
                md.update(first6.getBytes("UTF-8"));
            }
            if (last4 != null) {
                md.update(last4.getBytes("UTF-8"));
            }
            if (expiry != null) {
                md.update(expiry.getBytes("UTF-8"));
            }
            hash = HexDump.toHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e(TAG, "couldn't make hash of card");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(TAG, "couldn't make hash of card");
        }
        return hash;
    }

    public void mapBalanceInquiryResponse(final ElavonTransactionResponse etResponse, final BalanceInquiry balanceInquiry) {
        final ProcessorResponse processorResponse = new ProcessorResponse();
        /*
         * As requested in AX-1963 Converge: set PROCESSOR=CONVERGE, not ELAVON
         * Changed from Processor.ELAVON to Processor.CONVERGE
         */
        processorResponse.setProcessor(Processor.CONVERGE);
        processorResponse.setAcquirer(Processor.ELAVON);
        setStatusResponse(processorResponse, etResponse);
        setTransactionIdResponse(processorResponse, etResponse);
        if (etResponse.getApprovalCode() != null) {
            processorResponse.setApprovalCode(etResponse.getApprovalCode());
        }
        processorResponse.setRemainingBalance(CurrencyUtil.getAmount(etResponse.getAccountBalance(), DEFAULT_CURRENCY));
        balanceInquiry.setProcessorResponse(processorResponse);
    }

    public ElavonTransactionRequest getGenerateTokenRequest(final String cardNumber, final String expiry) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.GET_TOKEN);
        request.setCardNumber(cardNumber);
        request.setExpDate(expiry);
        return request;
    }

    public ElavonSettleRequest getSettleRequest(final List<String> transactionIds) {
        final ElavonSettleRequest request = new ElavonSettleRequest();
        request.setTransactionType(ElavonTransactionType.SETTLE);
        // set transaction ids
        if (transactionIds != null && !transactionIds.isEmpty()) {
            final ElavonSettleRequest.TransactionGroup tg = new ElavonSettleRequest.TransactionGroup();
            tg.setTransactionIds(transactionIds);
            request.setTransactionGroup(tg);
        }
        return request;
    }
}
