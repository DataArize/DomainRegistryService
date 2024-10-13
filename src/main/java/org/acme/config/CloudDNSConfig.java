package org.acme.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.dns.Dns;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
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
        GoogleCredential credential = GoogleCredential.getApplicationDefault();
        if(credential.createScopedRequired()) {
            credential = credential.createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        }
        return new Dns.Builder(httpTransport, gsonFactory, credential)
                .setApplicationName(applicationName)
                .build();
    }


}
