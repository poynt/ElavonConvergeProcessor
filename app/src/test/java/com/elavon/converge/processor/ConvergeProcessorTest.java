package com.elavon.converge.processor;

import com.elavon.converge.BaseTest;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.MockObjectFactory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class ConvergeProcessorTest extends BaseTest {

    private ConvergeClient convergeClient;

    @Before
    public void initialize() {
        convergeClient = new ConvergeClient();
    }

    @Test
    public void callSyncElavonTransactionRequest() throws Exception {
        // SETUP
        ElavonTransactionRequest req = MockObjectFactory.getElavonTransactionRequest();

        // TEST
        final ElavonTransactionResponse response = convergeClient.callSync(req, ElavonTransactionResponse.class);

        // VERIFY
        Assert.assertTrue(response.getApprovalCode().length() > 0);
        print("approval code: " + response.getApprovalCode());
    }
}
