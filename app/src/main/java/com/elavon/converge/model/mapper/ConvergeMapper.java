package com.elavon.converge.model.mapper;

import android.util.Base64;

import com.elavon.converge.exception.ConvergeMapperException;
import com.elavon.converge.model.ElavonResponse;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.ElavonTransactionSearchRequest;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.model.type.SignatureImageType;
import com.elavon.converge.util.CurrencyUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.EntryMode;
import co.poynt.api.model.Processor;
import co.poynt.api.model.ProcessorResponse;
import co.poynt.api.model.ProcessorStatus;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionStatus;

public class ConvergeMapper {

    private final Map<EntryMode, InterfaceMapper> interfaceMappers;

    @Inject
    public ConvergeMapper(final MsrMapper msrMapper, final EmvMapper emvMapper, final ContactlessMapper contactlessMapper) {
        interfaceMappers = new HashMap<>();
        interfaceMappers.put(EntryMode.KEYED, null);
        interfaceMappers.put(EntryMode.TRACK_DATA_FROM_MAGSTRIPE, msrMapper);
        interfaceMappers.put(EntryMode.CONTACTLESS_MAGSTRIPE, null);
        interfaceMappers.put(EntryMode.INTEGRATED_CIRCUIT_CARD, emvMapper);
        interfaceMappers.put(EntryMode.CONTACTLESS_INTEGRATED_CIRCUIT_CARD, contactlessMapper);
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

    public ElavonTransactionRequest getTransactionTipUpdateRequest(final String transactionId, final AdjustTransactionRequest adjustTransactionRequest) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.UPDATE_TIP);
        request.setTxnId(transactionId);
        request.setTipAmount(CurrencyUtil.getAmount(adjustTransactionRequest.getAmounts().getTipAmount(), adjustTransactionRequest.getAmounts().getCurrency()));
        return request;
    }

    public ElavonTransactionRequest getTransactionSignatureUpdateRequest(final String transactionId, final AdjustTransactionRequest adjustTransactionRequest) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.SIGNATURE);
        request.setTxnId(transactionId);
        request.setImageType(SignatureImageType.PNG);
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

        if (etResponse.isSuccess()) {
            processorResponse.setStatus(ProcessorStatus.Successful);
            processorResponse.setStatusCode(etResponse.getResult());
            processorResponse.setTransactionId(etResponse.getTxnId());
            processorResponse.setApprovalCode(etResponse.getApprovalCode());
            processorResponse.setApprovedAmount(CurrencyUtil.getAmount(etResponse.getAmount(), transaction.getAmounts().getCurrency()));
            transaction.setProcessorResponse(processorResponse);

            // TODO temporary fix
            if (transaction.getId() == null) {
                transaction.setId(UUID.randomUUID());
            }
            if (transaction.isSignatureCaptured() == null) {
                transaction.setSignatureCaptured(false);
            }

            switch (transaction.getAction()) {
                case AUTHORIZE:
                case SALE:
                    transaction.setStatus(TransactionStatus.CAPTURED);
                    break;
                case CAPTURE:
                    break;
                case VOID:
                    break;
                case OFFLINE_AUTHORIZE:
                    break;
                case REFUND:
                    transaction.setStatus(TransactionStatus.REFUNDED);
                    break;
                case VERIFY:
                    break;
                default:
                    throw new ConvergeMapperException("Invalid transaction action found");
            }
        } else if (ElavonResponse.RESULT_MESSAGE.PARTIAL_APPROVAL.equals(etResponse.getResultMessage())) {
            // TODO implement
        } else { // DECLINE
            if (etResponse.getErrorCode() != 0) {
                transaction.setStatus(TransactionStatus.DECLINED);
                processorResponse.setStatus(ProcessorStatus.Failure);
                processorResponse.setStatusMessage(etResponse.getErrorName());
                transaction.setProcessorResponse(processorResponse);
            }
        }
    }
}
