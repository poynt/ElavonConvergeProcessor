package com.elavon.converge.processor;

import com.elavon.converge.BaseTest;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.MockObjectFactory;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.Assert.assertTrue;

public class ConvergeClientTest extends BaseTest {

    private ConvergeClient convergeClient;

    @Before
    public void initialize() {
        convergeClient = new ConvergeClient();
    }

    @Test
    public void callElavonTransactionRequest() throws Exception {
        // SETUP
        final ElavonTransactionRequest req = MockObjectFactory.getElavonTransactionRequest();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean isSuccess = new AtomicBoolean();
        final AtomicReference<ElavonTransactionResponse> res = new AtomicReference<>();

        // TEST
        convergeClient.call(req, new ConvergeCallback<ElavonTransactionResponse>() {
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
        assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
        assertTrue(isSuccess.get());

        printXml(res.get());
        assertTrue(res.get().getApprovalCode().length() > 0);
    }

    @Test
    public void callSyncElavonTransactionRequest() throws Exception {
        // SETUP
        final ElavonTransactionRequest req = MockObjectFactory.getElavonTransactionRequest();

        // TEST
        final ElavonTransactionResponse response = convergeClient.callSync(req, ElavonTransactionResponse.class);

        // VERIFY
        printXml(response);
        assertTrue(response.getApprovalCode().length() > 0);
    }
}
