package com.elavon.converge.config;

public class AIDConfig {
    /* mode 00-modify, 01-overwrite, 02-push*/
    private byte mode;
    private byte cardInterface;
    private byte[] aid;
    private byte[] tlvs;

    public AIDConfig(byte mode, byte cardInterface, byte[] aid, byte[] tlvs) {
        this.mode = mode;
        this.cardInterface = cardInterface;
        this.aid = aid;
        this.tlvs = tlvs;
    }

    public byte[] getAid() {
        return aid;
    }

    public void setAid(byte[] aid) {
        this.aid = aid;
    }

    public byte getCardInterface() {
        return cardInterface;
    }

    public void setCardInterface(byte cardInterface) {
        this.cardInterface = cardInterface;
    }

    public byte getMode() {
        return mode;
    }

    public void setMode(byte mode) {
        this.mode = mode;
    }

    public byte[] getTlvs() {
        return tlvs;
    }

    public void setTlvs(byte[] tlvs) {
        this.tlvs = tlvs;
    }
}
