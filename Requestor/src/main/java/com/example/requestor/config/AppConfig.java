package com.example.requestor.config;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy; // Für HttpClient 5.x
// import org.apache.http.conn.ssl.TrustStrategy; // Für älteres HttpClient 4.x wäre es ähnlich
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class AppConfig {

    // Stelle sicher, dass dein BrokerService DIESE RestTemplate-Instanz verwendet.
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        // SSLContext erstellen, der allen Zertifikaten vertraut
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE) // WICHTIG: Vertraut allen Zertifikaten
                .build();

        // SSLConnectionSocketFactory mit dem benutzerdefinierten SSLContext und einem
        // NoopHostnameVerifier
        // NoopHostnameVerifier ist wichtig, wenn der Hostname im Zertifikat nicht mit
        // "localhost" übereinstimmt
        // (was bei abgelaufenen Zertifikaten aber nicht das Hauptproblem ist).
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE); // Deaktiviert Hostname-Überprüfung

        // HttpClient Connection Manager mit der benutzerdefinierten SSLSocketFactory
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        // Erstellen des HttpClient
        var httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .evictExpiredConnections() // Gute Praxis
                .build();

        // HttpComponentsClientHttpRequestFactory verwenden, um den HttpClient in
        // RestTemplate zu integrieren
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        // RestTemplate mit der benutzerdefinierten RequestFactory erstellen
        return builder
                .requestFactory(() -> requestFactory)
                .build();
    }

}
