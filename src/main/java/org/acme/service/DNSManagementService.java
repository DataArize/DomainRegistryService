package org.acme.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dns.Dns;
import com.google.api.services.dns.model.ManagedZone;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import jakarta.ws.rs.Path;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.acme.constants.Dummy;
import org.acme.constants.Errors;
import org.acme.exceptions.InvalidDomainNameException;
import org.acme.exceptions.ManagedZoneCreationFailed;
import org.acme.utils.DNSValidator;
import org.acme.utils.DomainValidator;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@ApplicationScoped
public class DNSManagementService {

    @ConfigProperty(name = "gcp.project.id")
    private String projectId;

    private final Dns cloudDnsClient;

    @Inject
    @Named("cloudDnsClient")
    public DNSManagementService(Dns cloudDnsClient) {
        this.cloudDnsClient = cloudDnsClient;
    }

    public Uni<List<ManagedZone>> provisionDNSManagedZone(List<String> domains) {
        return Multi.createFrom().items(domains.stream())
                .onItem().transformToUniAndConcatenate(domain -> {
                    ManagedZone managedZone = DNSValidator
                            .initializeManagedZone(Dummy.DUMMY_NAME, domain);
                    return Uni.createFrom().item(() -> {
                        try {
                            Dns.ManagedZones.Create request = cloudDnsClient.managedZones().create(projectId, managedZone);
                            ManagedZone managedZoneResponse = request.execute();
                            log.info("Managed Zone Created for domain {}", domain);
                            return managedZoneResponse; // Return the response here
                        } catch (IOException ex) {
                            log.error("Unable to create Managed Zone for domain '{}', Exception: {}", domain, ex.getMessage());
                            throw new ManagedZoneCreationFailed(Errors.UNABLE_TO_CREATE_MANAGED_ZONE + ex.getMessage());
                        }
                    });
                })
                .collect().asList();
    }

}
