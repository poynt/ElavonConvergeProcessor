package com.elavon.converge.model.mapper;

import com.elavon.converge.exception.ConvergeMapperException;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.ElavonTransactionSearchRequest;
import com.elavon.converge.model.type.ElavonTransactionType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import co.poynt.api.model.EntryMode;
import co.poynt.api.model.Processor;
import co.poynt.api.model.ProcessorResponse;
import co.poynt.api.model.ProcessorStatus;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionStatus;

public class ConvergeMapper {

    private final Map<EntryMode, InterfaceMapper> interfaceMappers;

    @Inject
    public ConvergeMapper(final MsrMapper msrMapper, final EmvMapper emvMapper) {
        interfaceMappers = new HashMap<>();
        interfaceMappers.put(EntryMode.KEYED, null);
        interfaceMappers.put(EntryMode.TRACK_DATA_FROM_MAGSTRIPE, msrMapper);
        interfaceMappers.put(EntryMode.CONTACTLESS_MAGSTRIPE, null);
        interfaceMappers.put(EntryMode.INTEGRATED_CIRCUIT_CARD, emvMapper);
        interfaceMappers.put(EntryMode.CONTACTLESS_INTEGRATED_CIRCUIT_CARD, null);
    }

    public ElavonTransactionRequest createSale(final Transaction transaction) {

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
    public void handleMSRSaleResponse(Transaction t, ElavonTransactionResponse et) {
        // APPROVAL
        if (ElavonTransactionResponse.RESULT_MESSAGE.APPROVAL.equals(et.getResultMessage())) {
            t.setStatus(TransactionStatus.CAPTURED);

            ProcessorResponse processorResponse = createProcessorResponse();
            processorResponse.setStatus(ProcessorStatus.Successful);
            processorResponse.setStatusCode(et.getResult());
//          //TODO Move to Manual Entry handler and add other responses
//            if(et.getCvv2Response() == null) {
//                processorResponse.setCvResult(CVResult.NO_RESPONSE);
//            }
            processorResponse.setTransactionId(et.getTxnId());
            processorResponse.setApprovalCode(et.getApprovalCode());
            // TODO for now assuming that we are only dealing with currency that have decimal values
            processorResponse.setApprovedAmount(et.getAmount().multiply(new BigDecimal(100)).longValue());
            t.setProcessorResponse(processorResponse);

            // TODO temporary fix
            if (t.getId() == null) {
                t.setId(UUID.randomUUID());
            }
        } else if (et.getResultMessage() == ElavonTransactionResponse.RESULT_MESSAGE.PARTIAL_APPROVAL) {// PARTIAL APPROVAL
            // TODO implement
        } else { // DECLINE
            // TODO
            /*

            //TODO implement response below
            <txn>
               <errorCode>5000</errorCode>
               <errorName>Credit Card Number Invalid</errorName>
               <errorMessage>The Credit Card Number supplied in the authorization request appears to be invalid.</errorMessage>
            </txn>

            <txn>
               <errorMessage>Only Test Cards Allowed in this environment</errorMessage>
               <errorName>Only Test Cards Allowed</errorName>
               <errorCode>9999</errorCode>
               <ssl_conversion_rate>0.0</ssl_conversion_rate>
            </txn>

            */
            if (et.getErrorCode() != 0) {
                t.setStatus(TransactionStatus.DECLINED);
                ProcessorResponse response = createProcessorResponse();
                response.setStatus(ProcessorStatus.Failure);
                response.setStatusMessage(et.getErrorName());
                t.setProcessorResponse(response);
            }

        }
    }

    private ProcessorResponse createProcessorResponse() {
        ProcessorResponse processorResponse = new ProcessorResponse();
        processorResponse.setProcessor(Processor.ELAVON);
        processorResponse.setAcquirer(Processor.ELAVON);
        return processorResponse;
    }

    public ElavonTransactionSearchRequest createSearchRequest(final String cardLast4, final Date searchStartDate) {
        final ElavonTransactionSearchRequest search = new ElavonTransactionSearchRequest();
        search.setTestMode("false");
        search.setTransactionType(ElavonTransactionType.TRANSACTION_QUERY);
        search.setCardSuffix(cardLast4);
        search.setSearchStartDate(searchStartDate);
        return search;
    }
}
