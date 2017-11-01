package com.elavon.converge.config;

public class Config {

    public enum Environment {
        TEST, LIVE
    }

    private ConvergeClient convergeClient;
    private Transaction transaction;
    private Log log;

    public ConvergeClient getConvergeClient() {
        return convergeClient;
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

        private Boolean enableHttpClient;

        public Boolean getEnableHttpClient() {
            return enableHttpClient;
        }

        public void setEnableHttpClient(Boolean enableHttpClient) {
            this.enableHttpClient = enableHttpClient;
        }
    }
}
