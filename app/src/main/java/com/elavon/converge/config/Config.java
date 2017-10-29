package com.elavon.converge.config;

public class Config {

    public enum Environment {
        TEST, LIVE
    }

    private ConvergeClient convergeClient;

    public ConvergeClient getConvergeClient() {
        return convergeClient;
    }

    public void setConvergeClient(ConvergeClient convergeClient) {
        this.convergeClient = convergeClient;
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
}
