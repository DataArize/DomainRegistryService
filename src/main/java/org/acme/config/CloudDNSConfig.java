package org.acme.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.dns.Dns;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.acme.constants.DNS;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@ApplicationScoped
public class CloudDNSConfig {

    @ConfigProperty(name = "quarkus.application.name")
    private String applicationName;

    @Produces
    @Singleton
    @Named("cloudDnsClient")
    public Dns cloudDNSClient() throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(List.of(DNS.DNS_SCOPE));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        return new Dns.Builder(httpTransport, gsonFactory, requestInitializer)
                .setApplicationName(applicationName)
                .build();
    }


}
