package com.elavon.converge.model;

import java.math.BigDecimal;
import java.util.UUID;

import co.poynt.api.model.Processor;
import co.poynt.api.model.ProcessorResponse;
import co.poynt.api.model.ProcessorStatus;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionStatus;

/**
 * Created by dennis on 10/26/17.
 */

public class ConvergeMapper {
    /*
{
  "action": "SALE",
  "amounts": {
    "currency": "USD",
    "orderAmount": 552,
    "tipAmount": 0,
    "transactionAmount": 552
  },
  "authOnly": false,
  "context": {
    "businessId": "2ac806d1-73e7-40c3-94ec-be2bb401a2df",
    "businessType": "TEST_MERCHANT",
    "employeeUserId": 17371213,
    "mcc": "5812",
    "mid": "e10zu3b7xs",
    "source": "INSTORE",
    "sourceApp": "co.poynt.services",
    "storeAddressCity": "Palo Alto",
    "storeAddressTerritory": "California",
    "storeId": "992e7a4e-65e6-4919-825e-8b0f2f63a592",
    "storeTimezone": "America/Los_Angeles",
    "tid": "56uw"
  },
  "customerLanguage": "en",
  "fundingSource": {
    "card": {
      "cardHolderFirstName": "2020",
      "cardHolderFullName": "MONEY/2020",
      "cardHolderLastName": "MONEY",
      "encrypted": true,
      "expirationDate": 31,
      "expirationMonth": 10,
      "expirationYear": 2016,
      "keySerialNumber": "FFFF9876543210E0004B",
      "numberFirst6": "453213",
      "numberLast4": "1054",
      "track1data": "CD7CF4B5497D239E977946B67A5364D894E4C72E015AD731E444B6ED3BACE67AE17CE1C542A76FA77B0E478ADB013BF034DF7C87307AD11A",
      "track2data": "",
      "type": "VISA"
    },
    "emvData": {
      "emvTags": {
        "0x5F24": "161031",
        "0x1F815D": "34",
        "0x5F20": "4D4F4E45592F32303230",
        "0x1F8104": "31303534",
        "0x1F815F": "04",
        "0x1F8103": "343533323133",
        "0x5F2A": "0840",
        "0x1F8102": "FFFF9876543210E0004B",
        "0x5F30": "101F",
        "0x1F8161": "00",
        "0x5F36": "02",
        "0x57": "",
        "0x58": "",
        "0x9F39": "02",
        "0x1F8153": "9D3E2DE7",
        "0x56": "CD7CF4B5497D239E977946B67A5364D894E4C72E015AD731E444B6ED3BACE67AE17CE1C542A76FA77B0E478ADB013BF034DF7C87307AD11A"
      }
    },
    "entryDetails": {
      "customerPresenceStatus": "PRESENT",
      "entryMode": "TRACK_DATA_FROM_MAGSTRIPE"
    },
    "type": "CREDIT_DEBIT"
  },
  "references": [
    {
      "customType": "referenceId",
      "id": "5c9a6b74-015f-1000-6146-0e9b4d0e4042",
      "type": "CUSTOM"
    }
  ],
  "signatureRequired": true
}
     */
    public static ElavonTransactionRequest createMSRSaleRequest(Transaction t, boolean authOnly){
        ElavonTransactionRequest request = new ElavonTransactionRequest();
        //TODO credentials should be set in ConvergeClient?
        request.setMerchantId("009005");
        request.setUserId("devportal");
        request.setPin("BDDZY5KOUDCNPV4L3821K7PETO4Z7TPYOJB06TYBI1CW771IDHXBVBP51HZ6ZANJ");

        if (authOnly) {
            request.setTransactionType(ElavonTransactionType.AUTH_ONLY);
        }else{
            request.setTransactionType(ElavonTransactionType.SALE);
        }
        request.setPosMode(ElavonPosMode.SWIPE_CAPABLE);
        request.setAmount(BigDecimal.valueOf((double)t.getAmounts().getTransactionAmount() / 100.0));
        //TODO handle tip

        request.setFirstName(t.getFundingSource().getCard().getCardHolderFirstName());
        request.setEncryptedTrackData(t.getFundingSource().getCard().getTrack2data());
        String expiration = "" + t.getFundingSource().getCard().getExpirationMonth() +
                t.getFundingSource().getCard().getExpirationYear() % 100;
        request.setKsn(t.getFundingSource().getCard().getKeySerialNumber());
        request.setExpDate(expiration);
//        request.setCardNumber("4124939999999990");
//        request.setExpDate("1219");
        return request;
    }

    public static void handleMSRSaleResponse (Transaction t, ElavonTransactionResponse et){

/*
<txn>
   <ssl_card_short_description>MC</ssl_card_short_description>
   <ssl_cvv2_response />
   <ssl_account_balance>0.00</ssl_account_balance>
   <ssl_result_message>APPROVAL</ssl_result_message>
   <ssl_invoice_number />
   <ssl_promo_code />
   <ssl_result>0</ssl_result>
   <ssl_txn_id>271017A15-D11434F9-B6AE-4312-A9BB-034F961636AB</ssl_txn_id>
   <ssl_completion_date />
   <ssl_transaction_type>SALE</ssl_transaction_type>
   <ssl_avs_response />
   <ssl_account_status />
   <ssl_approval_code>CMC648</ssl_approval_code>
   <ssl_enrollment />
   <ssl_exp_date>1225</ssl_exp_date>
   <ssl_loyalty_program />
   <ssl_tender_amount />
   <ssl_departure_date />
   <ssl_card_type>CREDITCARD</ssl_card_type>
   <ssl_loyalty_account_balance />
   <ssl_salestax />
   <ssl_amount>5.20</ssl_amount>
   <ssl_card_number>54**********3330</ssl_card_number>
   <ssl_issue_points />
   <ssl_txn_time>10/27/2017 01:37:53 PM</ssl_txn_time>
   <ssl_access_code />
</txn>
 */
        // APPROVAL
        if (ElavonTransactionResponse.RESULT_MESSAGE.APPROVAL.equals(et.getResultMessage())){
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
        }else if (et.getResultMessage() == ElavonTransactionResponse.RESULT_MESSAGE.PARTIAL_APPROVAL) {// PARTIAL APPROVAL
            // TODO implement
        }else { // DECLINE
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
            if (et.getErrorCode()!=0){
                t.setStatus(TransactionStatus.DECLINED);
                ProcessorResponse response = createProcessorResponse();
                response.setStatus(ProcessorStatus.Failure);
                response.setStatusMessage(et.getErrorName());
                t.setProcessorResponse(response);
            }

        }
    }

    private static ProcessorResponse createProcessorResponse(){
        ProcessorResponse processorResponse = new ProcessorResponse();
        processorResponse.setProcessor(Processor.ELAVON);
        processorResponse.setAcquirer(Processor.ELAVON);
        return processorResponse;
    }
}
