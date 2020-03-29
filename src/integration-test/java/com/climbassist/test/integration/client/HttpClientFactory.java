package com.climbassist.test.integration.client;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class HttpClientFactory {

    public HttpClient create() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        return HttpClientBuilder.create()
                // this allows us to connect to the beta site even though it's using a self-signed certificate
                // TODO this could be improved by getting a specific domain for beta so we can use a real certificate
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(new TrustAllStrategy())
                        .build())
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
    }
}
