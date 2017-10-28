package com.elavon.converge.processor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * A wrapper SSLSocketFactory that forces every SSLSocket
 * that is created from this factory to have TLSv1.2 enabled.
 *
 * Created by vchau on 8/30/15.
 */
public class TLSv1_2_SSLSocketFactory extends SSLSocketFactory {
    private static final String TLSv_1_2 = "TLSv1.2";
    private static final String[] ENABLED_PROTOCOLS = new String[] { TLSv_1_2 };

    //The real thing
    private SSLSocketFactory sslSocketFactory;

    public TLSv1_2_SSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance(TLSv_1_2);
            sslContext.init(null, null, null); //<= use defaults
            this.sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize TLSv1_2OnlySSLSocketFactory.");
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return sslSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sslSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(s, host, port, autoClose);
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;

    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port, localHost,
                localPort);
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;

    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;

    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(address, port, localAddress,
                localPort);
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;

    }

    @Override
    public Socket createSocket() throws IOException {
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket();
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;
    }
}

