package com.elavon.converge.processor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * A wrapper SSLSocketFactory that forces every SSLSocket
 * that is created from this factory to have TLSv1.2 enabled.
 */
public class TLSSocketFactory extends SSLSocketFactory {

    private static final String TLSv_1_2 = "TLSv1.2";
    private static final String[] ENABLED_PROTOCOLS = new String[]{TLSv_1_2};

    //The real thing
    private final SSLSocketFactory sslSocketFactory;

    public TLSSocketFactory() {
        try {
            final SSLContext sslContext = SSLContext.getInstance(TLSv_1_2);
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
    public Socket createSocket(final Socket s, final String host, final int port, final boolean autoClose) throws IOException {
        final SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(s, host, port, autoClose);
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        final SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort) throws IOException {
        final SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port, localHost, localPort);
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        final SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddress, final int localPort) throws IOException {
        final SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(address, port, localAddress, localPort);
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;
    }

    @Override
    public Socket createSocket() throws IOException {
        final SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket();
        socket.setEnabledProtocols(ENABLED_PROTOCOLS);
        return socket;
    }
}
