package com.elavon.converge.model.mapper;

import android.util.Base64;
import android.util.Log;

import com.elavon.converge.exception.ConvergeMapperException;
import com.elavon.converge.model.ElavonResponse;
import com.elavon.converge.model.ElavonSettleRequest;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.ElavonTransactionSearchRequest;
import com.elavon.converge.model.type.AVSResponse;
import com.elavon.converge.model.type.CVV2Response;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.model.type.ResponseCodes;
import com.elavon.converge.model.type.SignatureImageType;
import com.elavon.converge.util.CurrencyUtil;
import com.elavon.converge.util.HexDump;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import co.poynt.api.model.Processor;
import co.poynt.api.model.ProcessorResponse;
import co.poynt.api.model.ProcessorStatus;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionReference;
import co.poynt.api.model.TransactionStatus;

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

    @Inject
    public ConvergeMapper(
            final MsrMapper msrMapper,
            final MsrDebitMapper msrDebitMapper,
            final MsrEbtMapper msrEbtMapper,
            final MsrGiftcardMapper msrGiftcardMapper,
            final EmvMapper emvMapper,
            final KeyedMapper keyedMapper,
            final KeyedEbtMapper keyedEbtMapper,
            final KeyedGiftcardMapper keyedGiftcardMapper) {
        this.msrMapper = msrMapper;
        this.msrDebitMapper = msrDebitMapper;
        this.msrEbtMapper = msrEbtMapper;
        this.msrGiftcardMapper = msrGiftcardMapper;
        this.emvMapper = emvMapper;
        this.keyedMapper = keyedMapper;
        this.keyedEbtMapper = keyedEbtMapper;
        this.keyedGiftcardMapper = keyedGiftcardMapper;
    }

    private InterfaceMapper getMapper(final FundingSource fundingSource) {
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

    private boolean isGiftCard(final FundingSource fundingSource) {
        // TODO need to implement this
        return false;
    }

    public ElavonTransactionRequest getTransactionRequest(final Transaction transaction) {
        Log.d(TAG, "Transaction Request:" + transaction);
        final InterfaceMapper mapper = getMapper(transaction.getFundingSource());
        final ElavonTransactionRequest request;
        switch (transaction.getAction()) {
            case AUTHORIZE:
                request = mapper.createAuth(transaction);
                break;
            case REFUND:
                request = mapper.createRefund(transaction);
                break;
            case SALE:
                request = mapper.createSale(transaction);
                break;
            default:
                throw new ConvergeMapperException("Invalid transaction action found");
        }
        request.setInvoiceNumber(getReference(transaction, "invoiceId"));
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
                && (entryDetails.getEntryMode() == EntryMode.INTEGRATED_CIRCUIT_CARD
                || entryDetails.getEntryMode() == EntryMode.CONTACTLESS_INTEGRATED_CIRCUIT_CARD)) {
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

    public ElavonTransactionSearchRequest getSearchRequest(final String cardLast4, final Date searchStartDate) {
        final ElavonTransactionSearchRequest search = new ElavonTransactionSearchRequest();
        search.setTestMode("false");
        search.setTransactionType(ElavonTransactionType.TRANSACTION_QUERY);
        search.setCardSuffix(cardLast4);
        search.setSearchStartDate(searchStartDate);
        return search;
    }

    public ElavonTransactionRequest getTransactionReversalRequest(final FundingSource fundingSource,
                                                                  final String transactionId) {
        final InterfaceMapper mapper = getMapper(fundingSource);
        return mapper.createReverse(transactionId);
    }

    public ElavonTransactionRequest getTransactionVoidRequest(final FundingSource fundingSource,
                                                              final String transactionId) {
        final InterfaceMapper mapper = getMapper(fundingSource);
        return mapper.createVoid(transactionId);
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

        final ProcessorResponse processorResponse = new ProcessorResponse();
        processorResponse.setProcessor(Processor.ELAVON);
        processorResponse.setAcquirer(Processor.ELAVON);

        // always generate a hash of the card info
        if (transaction.getFundingSource() != null && transaction.getFundingSource().getCard() != null) {
            transaction.getFundingSource().getCard().setNumberHashed(
                    generateHash(transaction.getFundingSource().getCard().getCardHolderFullName(),
                            transaction.getFundingSource().getCard().getNumberFirst6(),
                            transaction.getFundingSource().getCard().getNumberLast4(),
                            transaction.getFundingSource().getCard().getEncryptedExpirationDate())
            );
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
                    transaction.setStatus(TransactionStatus.PARTIALLY_CAPTURED);
                    break;
                case VOID:
                    transaction.setStatus(TransactionStatus.VOIDED);
                    break;
                case OFFLINE_AUTHORIZE:
                    transaction.setStatus(TransactionStatus.AUTHORIZED);
                    break;
                case REFUND:
                    transaction.setStatus(TransactionStatus.PARTIALLY_REFUNDED);
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

        if (etResponse.getResponseCode() == ResponseCodes.AA
                || etResponse.getResponseCode() == ResponseCodes.AP
                || ElavonResponse.RESULT_MESSAGE.APPROVAL.equals(etResponse.getResultMessage())
                || ElavonResponse.RESULT_MESSAGE.PARTIAL_APPROVAL.equals(etResponse.getResultMessage())) {
            if (etResponse.getAmount() != null) {
                processorResponse.setApprovedAmount(CurrencyUtil.getAmount(etResponse.getAmount(), transaction.getAmounts().getCurrency()));
            } else if (etResponse.getBaseAmount() != null) {
                processorResponse.setApprovedAmount(CurrencyUtil.getAmount(etResponse.getBaseAmount(), transaction.getAmounts().getCurrency()));
            }
        }

        // set  EMV response tags
        Map<String, String> emvTags = new HashMap<>();

        // csn
        if (etResponse.getCsn() != null) {
            emvTags.put("0x5F34", etResponse.getCsn());
        }

        // atc
        if (etResponse.getAtc() != null) {
            emvTags.put("0x9F36", etResponse.getAtc());
        }

        // arpc
        if (etResponse.getArpc() != null) {
            emvTags.put("0x91", etResponse.getArpc());
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
            if (etResponse.getIssuerResponse() != null) {
                emvTags.put("0xDFD9", numberToAsciiHex(etResponse.getIssuerResponse().toCharArray()));
            }
        } else {
            if (etResponse.getResponseCode() == ResponseCodes.AA) {
                // Create TLV for Referral Code 8A023032
                emvTags.put(EMVTag.RESPONSE_AUTHORIZATION_RESPONSE_CODE.tag(), "3030");
                if (etResponse.getIssuerResponse() != null) {
                    emvTags.put("0xDFD9", numberToAsciiHex(etResponse.getIssuerResponse().toCharArray()));
                }
            } else if (etResponse.getResponseCode() == ResponseCodes.AP) {
                // for partial approval, firmware team wants us to pass Approval..
                emvTags.put("0xDFD9", "3030");
                emvTags.put(EMVTag.RESPONSE_AUTHORIZATION_RESPONSE_CODE.tag(), "3030");
            } else if (etResponse.getResponseCode() == ResponseCodes.NR) {
                emvTags.put(EMVTag.RESPONSE_AUTHORIZATION_RESPONSE_CODE.tag(), "3031");
                emvTags.put("0xDFD9", "3031");
            } else {
                emvTags.put(EMVTag.RESPONSE_AUTHORIZATION_RESPONSE_CODE.tag(), "3035");
                if (etResponse.getIssuerResponse() != null) {
                    emvTags.put("0xDFD9", numberToAsciiHex(etResponse.getIssuerResponse().toCharArray()));
                }
            }
        }

        processorResponse.setEmvTags(emvTags);
        if (etResponse.getCvv2Response() != null) {
            processorResponse.setCvResult(mapCvResponse(etResponse.getCvv2Response()));
            processorResponse.setCvActualResult(etResponse.getCvv2Response().getValue());
        }
        if (etResponse.getAvsResponse() != null) {
            processorResponse.setAvsResult(mapAvsResponse(etResponse.getAvsResponse()));
        }
        transaction.setProcessorResponse(processorResponse);

        // TODO temporary fix
        if (transaction.getId() == null) {
            transaction.setId(UUID.randomUUID());
        }
        if (transaction.isSignatureCaptured() == null) {
            transaction.setSignatureCaptured(false);
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
        processorResponse.setProcessor(Processor.ELAVON);
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
