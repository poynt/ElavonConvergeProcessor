package com.elavon.converge.config;

public class Config {

    public enum Environment {
        TEST, LIVE
    }

    private Credential credential;
    private ConvergeClient convergeClient;
    private Transaction transaction;
    private Log log;

    public ConvergeClient getConvergeClient() {
        return convergeClient;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public void setConvergeClient(ConvergeClient convergeClient) {
        this.convergeClient = convergeClient;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public static class Credential {

        private String merchantId;
        private String userId;
        private String pin;

        public String getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getPin() {
            return pin;
        }

        public void setPin(String pin) {
            this.pin = pin;
        }
    }

    public static class ConvergeClient {

        private String host;
        private Integer connectTimeoutMs;
        private Integer readTimeoutMs;
        private Integer writeTimeoutMs;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(Integer connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public Integer getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(Integer readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public Integer getWriteTimeoutMs() {
            return writeTimeoutMs;
        }

        public void setWriteTimeoutMs(Integer writeTimeoutMs) {
            this.writeTimeoutMs = writeTimeoutMs;
        }
    }

    public static class Transaction {

        private Integer maxRetryCount;

        public Integer getMaxRetryCount() {
            return maxRetryCount;
        }

        public void setMaxRetryCount(Integer maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
        }
    }

    public static class Log {

        private Boolean enableHttpTracing;

        public Boolean getEnableHttpTracing() {
            return enableHttpTracing;
        }

        public void setEnableHttpTracing(Boolean enableHttpTracing) {
            this.enableHttpTracing = enableHttpTracing;
        }
    }
}
