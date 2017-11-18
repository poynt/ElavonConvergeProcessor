package com.elavon.converge.model.mapper;

import android.util.Base64;
import android.util.Log;

import com.elavon.converge.exception.ConvergeMapperException;
import com.elavon.converge.model.ElavonResponse;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.ElavonTransactionSearchRequest;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.model.type.ResponseCodes;
import com.elavon.converge.model.type.SignatureImageType;
import com.elavon.converge.util.CurrencyUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.EMVData;
import co.poynt.api.model.EMVTag;
import co.poynt.api.model.EntryMode;
import co.poynt.api.model.FundingSourceEntryDetails;
import co.poynt.api.model.Processor;
import co.poynt.api.model.ProcessorResponse;
import co.poynt.api.model.ProcessorStatus;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionStatus;

public class ConvergeMapper {

    private static final String TAG = ConvergeMapper.class.getSimpleName();
    private final Map<EntryMode, InterfaceMapper> interfaceMappers;

    @Inject
    public ConvergeMapper(final MsrMapper msrMapper, final EmvMapper emvMapper) {
        interfaceMappers = new HashMap<>();
        interfaceMappers.put(EntryMode.KEYED, null);
        interfaceMappers.put(EntryMode.TRACK_DATA_FROM_MAGSTRIPE, msrMapper);
        interfaceMappers.put(EntryMode.CONTACTLESS_MAGSTRIPE, msrMapper);
        interfaceMappers.put(EntryMode.INTEGRATED_CIRCUIT_CARD, emvMapper);
        interfaceMappers.put(EntryMode.CONTACTLESS_INTEGRATED_CIRCUIT_CARD, emvMapper);
    }

    public ElavonTransactionRequest getTransactionRequest(final Transaction transaction) {
        final InterfaceMapper mapper = interfaceMappers.get(transaction.getFundingSource().getEntryDetails().getEntryMode());
        if (mapper == null) {
            throw new ConvergeMapperException("Invalid entry mode found");
        }

        switch (transaction.getAction()) {
            case AUTHORIZE:
                return mapper.createAuth(transaction);
            case CAPTURE:
                return mapper.createCapture(transaction);
            case VOID:
                return mapper.createVoid(transaction);
            case OFFLINE_AUTHORIZE:
                return mapper.createOfflineAuth(transaction);
            case REFUND:
                return mapper.createRefund(transaction);
            case SALE:
                return mapper.createSale(transaction);
            case VERIFY:
                return mapper.createVerify(transaction);
            default:
                throw new ConvergeMapperException("Invalid transaction action found");
        }
    }

    public ElavonTransactionRequest getTransactionTipUpdateRequest(FundingSourceEntryDetails entryDetails,
                                                                   final String transactionId,
                                                                   final AdjustTransactionRequest adjustTransactionRequest) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        if (entryDetails != null
                && (entryDetails.getEntryMode() == EntryMode.INTEGRATED_CIRCUIT_CARD
                || entryDetails.getEntryMode() == EntryMode.CONTACTLESS_INTEGRATED_CIRCUIT_CARD)) {
            request.setTransactionType(ElavonTransactionType.EMV_CT_UPDATE);
        } else {
            request.setTransactionType(ElavonTransactionType.UPDATE_TIP);
        }
        // elavon transactionId
        request.setTxnId(transactionId);
        request.setTipAmount(CurrencyUtil.getAmount(adjustTransactionRequest.getAmounts().getTipAmount(), adjustTransactionRequest.getAmounts().getCurrency()));
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

    public ElavonTransactionRequest getTransactionSignatureUpdateRequest(final String transactionId, final AdjustTransactionRequest adjustTransactionRequest) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.SIGNATURE);
        request.setTxnId(transactionId);
        request.setSignatureImageType(SignatureImageType.PNG);
        request.setSignatureImage(Base64.encodeToString(adjustTransactionRequest.getSignature(), Base64.DEFAULT));
        return request;
    }

    public ElavonTransactionSearchRequest getSearchRequest(final String cardLast4, final Date searchStartDate) {
        final ElavonTransactionSearchRequest search = new ElavonTransactionSearchRequest();
        search.setTestMode("false");
        search.setTransactionType(ElavonTransactionType.TRANSACTION_QUERY);
        search.setCardSuffix(cardLast4);
        search.setSearchStartDate(searchStartDate);
        return search;
    }

    public ElavonTransactionRequest getTransactionReversalRequest(final FundingSourceEntryDetails entryDetails,
                                                                  final String transactionId) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        if (entryDetails != null
                && (entryDetails.getEntryMode() == EntryMode.INTEGRATED_CIRCUIT_CARD
                || entryDetails.getEntryMode() == EntryMode.CONTACTLESS_INTEGRATED_CIRCUIT_CARD)) {
            request.setTransactionType(ElavonTransactionType.EMV_REVERSAL);
        } else {
            request.setTransactionType(ElavonTransactionType.VOID);
        }
        // elavon transactionId
        request.setTxnId(transactionId);
        return request;
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
        processorResponse.setProcessor(Processor.CONVERGE);
        processorResponse.setAcquirer(Processor.ELAVON);


        if (etResponse.isSuccess()) {
            processorResponse.setStatus(ProcessorStatus.Successful);
        } else {
            processorResponse.setStatus(ProcessorStatus.Failure);
        }
        processorResponse.setStatusCode(etResponse.getResult());
        if (etResponse.getResultMessage() != null) {
            processorResponse.setStatusMessage(etResponse.getResultMessage());
        } else if (etResponse.getErrorMessage() != null) {
            processorResponse.setStatusMessage(etResponse.getErrorMessage());
        } else {
            processorResponse.setStatusMessage(Integer.toString(etResponse.getErrorCode()));
        }

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

        if (etResponse.getApprovalCode() != null) {
            processorResponse.setApprovalCode(etResponse.getApprovalCode());
        }

        if (etResponse.getResponseCode() == ResponseCodes.AA
                || etResponse.getResponseCode() == ResponseCodes.AP
                || ElavonResponse.RESULT_MESSAGE.APPROVAL.equals(etResponse.getResultMessage())
                || ElavonResponse.RESULT_MESSAGE.PARTIAL_APPROVAL.equals(etResponse.getResultMessage())) {
            processorResponse.setApprovedAmount(CurrencyUtil.getAmount(etResponse.getAmount(),
                    transaction.getAmounts().getCurrency()));
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
        transaction.setProcessorResponse(processorResponse);

        // TODO temporary fix
        if (transaction.getId() == null) {
            transaction.setId(UUID.randomUUID());
        }
        if (transaction.isSignatureCaptured() == null) {
            transaction.setSignatureCaptured(false);
        }

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
}
