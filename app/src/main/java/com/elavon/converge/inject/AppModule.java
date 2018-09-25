package com.elavon.converge.inject;

import android.content.Context;
import android.util.Log;

import com.elavon.converge.config.Config;
import com.elavon.converge.config.ConfigLoader;
import com.elavon.converge.core.TransactionManager;
import com.elavon.converge.exception.AppInitException;
import com.elavon.converge.model.mapper.CashMapper;
import com.elavon.converge.model.mapper.ConvergeMapper;
import com.elavon.converge.model.mapper.EmvMapper;
import com.elavon.converge.model.mapper.KeyedEbtMapper;
import com.elavon.converge.model.mapper.KeyedGiftcardMapper;
import com.elavon.converge.model.mapper.KeyedMapper;
import com.elavon.converge.model.mapper.MsrDebitMapper;
import com.elavon.converge.model.mapper.MsrEbtMapper;
import com.elavon.converge.model.mapper.MsrGiftcardMapper;
import com.elavon.converge.model.mapper.MsrMapper;
import com.elavon.converge.processor.ConvergeClient;
import com.elavon.converge.processor.ConvergeService;
import com.elavon.converge.processor.TLSSocketFactory;
import com.elavon.converge.xml.XmlMapper;

import java.security.KeyStore;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class AppModule {

    private static final String TAG = AppModule.class.getSimpleName();

    private final Context context;
    private final Config config;

    public AppModule(final Context context) {
        this.context = context;
        this.config = new ConfigLoader(context.getResources(), Config.Environment.LIVE).load();
    }

    public AppModule(final Context context, final Config config) {
        this.context = context;
        this.config = config;
    }

    @Provides
    @Singleton
    public XmlMapper provideXmlMapper() {
        return new XmlMapper();
    }

    @Provides
    @Singleton
    public MsrMapper provideMsrMapper() {
        return new MsrMapper();
    }

    @Provides
    @Singleton
    public MsrDebitMapper provideMsrDebitMapper() {
        return new MsrDebitMapper();
    }

    @Provides
    @Singleton
    public MsrEbtMapper provideMsrEbtMapper() {
        return new MsrEbtMapper();
    }

    @Provides
    @Singleton
    public MsrGiftcardMapper provideMsrGiftcardMapper() {
        return new MsrGiftcardMapper();
    }

    @Provides
    @Singleton
    public EmvMapper provideEmvMapper() {
        return new EmvMapper();
    }

    @Provides
    @Singleton
    public KeyedMapper provideKeyedMapper() {
        return new KeyedMapper();
    }

    @Provides
    @Singleton
    public KeyedEbtMapper provideKeyedEbtMapper() {
        return new KeyedEbtMapper();
    }

    @Provides
    @Singleton
    public KeyedGiftcardMapper provideKeyedGiftcardMapper() {
        return new KeyedGiftcardMapper();
    }

    @Provides
    @Singleton
    public CashMapper provideCashMapper() {
        return new CashMapper();
    }

    @Provides
    @Singleton
    public ConvergeMapper provideConvergeMapper(
            final MsrMapper msrMapper,
            final MsrDebitMapper msrDebitMapper,
            final MsrEbtMapper msrEbtMapper,
            final MsrGiftcardMapper msrGiftcardMapper,
            final EmvMapper emvMapper,
            final KeyedMapper keyedMapper,
            final KeyedEbtMapper keyedEbtMapper,
            final KeyedGiftcardMapper keyedGiftcardMapper,
            final CashMapper cashMapper) {
        return new ConvergeMapper(msrMapper, msrDebitMapper, msrEbtMapper, msrGiftcardMapper,
                emvMapper, keyedMapper, keyedEbtMapper, keyedGiftcardMapper, cashMapper);
    }

    @Provides
    @Singleton
    public ConvergeClient provideConvergeClient(final XmlMapper xmlMapper) {

        final ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_2).build();
        final TrustManagerFactory tmf;
        try {
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
        } catch (final Exception e) {
            throw new AppInitException("Failed to initialize converge client", e);
        }

        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(config.getConvergeClient().getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getConvergeClient().getWriteTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getConvergeClient().getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .connectionSpecs(Collections.singletonList(spec))
                .sslSocketFactory(new TLSSocketFactory(), (X509TrustManager) tmf.getTrustManagers()[0]);

        // add logging interceptor
        if (Boolean.TRUE.equals(config.getLog().getEnableHttpTracing())) {
            final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.i("OkHttp", message);
                }
            });
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(logging);
        }

        /*
         * If Config is TEST -> credentials will be updated from credentials.json or credentials file from raw folder,
         * If Config is LIVE -> credentials will be downloaded from cloud and updated in shared pref as well as in ConvergeClient.
         */
        try {
            ConvergeClient convergeClient = null;
            if(config != null && config.getCredential() != null) {
                convergeClient = new ConvergeClient(
                        config.getCredential().getMerchantId(),
                        config.getCredential().getUserId(),
                        config.getCredential().getPin(),
                        config.getConvergeClient().getHost(),
                        clientBuilder.build(),
                        xmlMapper);
                Log.d(TAG, "build converge client with pin data" + convergeClient.toString());
            } else {
                convergeClient = new ConvergeClient(
                        "",
                        "",
                        "",
                        config.getConvergeClient().getHost(),clientBuilder.build(),
                        xmlMapper);
                Log.d(TAG, "build converge client without pin data" + convergeClient.toString());
            }
            return convergeClient;
        } catch (Exception e) {
            throw new AppInitException("Failed to initialize converge client", e);
        }
    }

    @Provides
    @Singleton
    public ConvergeService provideConvergeService(final ConvergeMapper convergeMapper, final ConvergeClient convergeClient) {
        return new ConvergeService(convergeMapper, convergeClient, config.getTransaction().getMaxRetryCount());
    }

    @Provides
    @Singleton
    public TransactionManager provideTransactionManager(final ConvergeService convergeService, final ConvergeMapper convergeMapper) {
        return new TransactionManager(context, convergeService, convergeMapper);
    }
}
