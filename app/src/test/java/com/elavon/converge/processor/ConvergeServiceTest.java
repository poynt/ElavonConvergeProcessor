package com.elavon.converge.processor;

import com.elavon.converge.BaseTest;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.MockObjectFactory;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.Assert.assertTrue;

public class ConvergeServiceTest extends BaseTest {

    private ConvergeService convergeService;

    @Before
    public void initialize() throws Exception {
        convergeService = appModule.provideConvergeService(
                appModule.provideConvergeMapper(appModule.provideMsrMapper(), appModule.provideEmvMapper(), appModule.provideContactlessMapper()),
                appModule.provideConvergeClient(appModule.provideXmlMapper())
        );
    }

    @Test
    public void createThenUpdate() throws Exception {
        // SETUP - create
        final ElavonTransactionRequest txnReq = MockObjectFactory.getElavonTransactionRequest();

        // TEST - create
        final ElavonTransactionResponse txnRes = callSync(txnReq);

        // VERIFY - create
        assertTrue(txnRes.isSuccess());

        // SETUP - update
        final ElavonTransactionRequest updateReq = MockObjectFactory.getElavonTransactionUpdateRequest(txnRes.getTxnId());

        // TEST - update
        final ElavonTransactionResponse updateRes = callSync(updateReq);

        // VERIFY - update
        assertTrue(updateRes.isSuccess());
    }

    private ElavonTransactionResponse callSync(ElavonTransactionRequest request) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean isSuccess = new AtomicBoolean();
        final AtomicReference<ElavonTransactionResponse> res = new AtomicReference<>();

        convergeService.create(request, new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(final ElavonTransactionResponse response) {
                isSuccess.set(true);
                res.set(response);
                latch.countDown();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                latch.countDown();
                throwable.printStackTrace();
            }
        });

        assertTrue(latch.await(50000, TimeUnit.MILLISECONDS));
        assertTrue(isSuccess.get());
        return res.get();
    }

    @Test
    public void find() throws Exception {
        // SETUP
        final ElavonTransactionRequest req = MockObjectFactory.getElavonTransactionRequest();
        final Date dateAfter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").parse("10/01/2017 00:00:00 AM");
        final String cardLast4 = req.getCardNumber().substring(req.getCardNumber().length() - 4, req.getCardNumber().length());
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean isSuccess = new AtomicBoolean();
        final AtomicReference<ElavonTransactionResponse> res = new AtomicReference<>();

        // TEST
        convergeService.find(cardLast4, req.getAmount(), dateAfter, new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(final ElavonTransactionResponse response) {
                isSuccess.set(true);
                res.set(response);
                latch.countDown();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                latch.countDown();
                throwable.printStackTrace();
            }
        });

        // VERIFY
        assertTrue(latch.await(50000, TimeUnit.MILLISECONDS));
        assertTrue(isSuccess.get());

        printXml(res.get());
        assertTrue(res.get().getApprovalCode().length() > 0);
    }
}
