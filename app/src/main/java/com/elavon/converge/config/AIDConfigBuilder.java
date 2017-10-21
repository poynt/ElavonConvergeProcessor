package com.elavon.converge.config;

public class AIDConfigBuilder {
    private byte mode;
    private byte cardInterface;
    private byte[] aid;
    private byte[] tlvs;

    public AIDConfigBuilder setMode(byte mode) {
        this.mode = mode;
        return this;
    }

    public AIDConfigBuilder setCardInterface(byte cardInterface) {
        this.cardInterface = cardInterface;
        return this;
    }

    public AIDConfigBuilder setAid(byte[] aid) {
        this.aid = aid;
        return this;
    }

    public AIDConfigBuilder setTlvs(byte[] tlvs) {
        this.tlvs = tlvs;
        return this;
    }

    public AIDConfig createAIDConfig() {
        return new AIDConfig(mode, cardInterface, aid, tlvs);
    }
}