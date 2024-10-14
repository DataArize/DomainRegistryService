package org.acme.utils;

import com.google.api.services.dns.model.ManagedZone;
import com.google.api.services.dns.model.ManagedZoneCloudLoggingConfig;
import com.google.api.services.dns.model.ManagedZoneDnsSecConfig;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.constants.DNS;
import org.acme.constants.DNSState;
import org.acme.constants.ZoneVisibility;
import org.acme.exceptions.DNSAlreadyExistsException;
import org.acme.exceptions.InvalidDomainNameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.wildfly.common.Assert.assertNotNull;

@QuarkusTest
class DNSValidatorTest {


    private static final String VALID_NAME = "example-zone";
    private static final String VALID_DNS_NAME = "example.com";
    private static final String ACTIVE_PROFILE = "production";

    @ParameterizedTest
    @ValueSource(strings = {
            "google.com",
//            "sub.example.com",
            "example-domain.com",
            "example.co.uk",
            "example123.com",
//            "a.com",
    })
    void givenValidDomainWithDNS_whenDoesNameServerExistsCalled_thenThrowException(String domain) {
        assertThrows(DNSAlreadyExistsException.class, () -> DNSValidator.doesNameServerExists(domain).await().indefinitely());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ""
    })
    void givenValidDomainWithoutDNS_whenDoesNameServerExistsCalled_thenThrowException(String domain) {
        assertThrows(InvalidDomainNameException.class, () -> DNSValidator.doesNameServerExists(domain).await().indefinitely());

    }

    @ParameterizedTest
    @ValueSource(strings = {
            " "
    })
    void givenValidDomainWithoutDNS_whenDoesNameServerExistsCalled_thenReturnNothing(String domain) {
        assertNull(DNSValidator.doesNameServerExists(domain).await().indefinitely());
    }


    @Test
    void testInitializeManagedZone() {
        ManagedZone managedZone = DNSValidator.initializeManagedZone(VALID_NAME, VALID_DNS_NAME, ACTIVE_PROFILE);
        assertNotNull(managedZone);
        assertEquals(VALID_NAME, managedZone.getName());
        assertEquals(VALID_DNS_NAME, managedZone.getDnsName());
        assertEquals(DNS.DNS_DESCRIPTION, managedZone.getDescription());
        assertEquals(ZoneVisibility.PUBLIC.getValue(), managedZone.getVisibility());
        ManagedZoneDnsSecConfig dnsSecConfig = managedZone.getDnssecConfig();
        assertNotNull(dnsSecConfig);
        assertEquals(DNSState.ON.getValue(), dnsSecConfig.getState());
        ManagedZoneCloudLoggingConfig cloudLoggingConfig = managedZone.getCloudLoggingConfig();
        assertNotNull(cloudLoggingConfig);
        assertTrue(cloudLoggingConfig.getEnableLogging());
    }

    @Test
    void testInitializeDnsSecConfig() {
        ManagedZoneDnsSecConfig dnsSecConfig = DNSValidator.initializeDnsSecConfig();
        assertNotNull(dnsSecConfig);
        assertEquals(DNSState.ON.getValue(), dnsSecConfig.getState());
    }

    @Test
    void testCloudLoggingConfig() {
        ManagedZoneCloudLoggingConfig cloudLoggingConfig = DNSValidator.cloudLoggingConfig();
        assertNotNull(cloudLoggingConfig);
        assertTrue(cloudLoggingConfig.getEnableLogging());
    }

    @Test
    void testGetBaseDomain_ValidDomain() {
        String domain = "sub.example.com";
        String expectedBaseDomain = DNS.EMAIL_SERVER + "sub";
        String baseDomain = DNSValidator.getBaseDomain(domain);
        assertEquals(expectedBaseDomain, baseDomain);
    }

    @Test
    void testGetBaseDomain_NoDomainProvided() {
        assertThrows(InvalidDomainNameException.class, () -> {
            DNSValidator.getBaseDomain("");
        });

        assertThrows(InvalidDomainNameException.class, () -> {
            DNSValidator.getBaseDomain(null);
        });
    }

    @Test
    void testGetBaseDomain_SingleLabelDomain() {
        String domain = "example";
        String expectedBaseDomain = DNS.EMAIL_SERVER + "example";

        String baseDomain = DNSValidator.getBaseDomain(domain);
        assertEquals(expectedBaseDomain, baseDomain);
    }

}