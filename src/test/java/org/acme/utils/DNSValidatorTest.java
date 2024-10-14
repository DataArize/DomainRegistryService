package org.acme.utils;

import com.google.api.services.dns.model.ManagedZone;
import com.google.api.services.dns.model.ManagedZoneCloudLoggingConfig;
import com.google.api.services.dns.model.ManagedZoneDnsSecConfig;
import com.google.api.services.dns.model.ResourceRecordSet;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.constants.DNS;
import org.acme.constants.DNSRecordType;
import org.acme.constants.DNSState;
import org.acme.constants.ZoneVisibility;
import org.acme.exceptions.DNSAlreadyExistsException;
import org.acme.exceptions.InvalidDomainNameException;
import org.acme.model.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

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

    @Test
    void givenValidRequest_whenInitializeResourceRecords_thenReturnValidResourceRecordSet() {
        Request request = new Request();
        request.setDomain("example.com.");
        request.setServerDNS("server.example.com.");
        ResourceRecordSet result = DNSValidator.initializeResourceRecords(request);
        assertEquals("example.com.", result.getName());
        assertEquals(DNSRecordType.MX.name(), result.getType());
        assertEquals(DNS.DNS_TTL, result.getTtl());
        assertEquals(Collections.singletonList(
                DNS.MX_RECORD.replace(DNS.SERVER_DNS_NAME, "server.example.com.")
        ), result.getRrdatas());
    }

    @Test
    void givenMXRecordType_whenCreateDNSRecords_thenReturnMXRecord() {
        Request request = new Request();
        request.setServerDNS("mail.example.com");
        List<String> records = DNSValidator.createDNSRecords(DNSRecordType.MX.name(), request);
        assertNotNull(records);
        assertEquals(1, records.size());
        assertEquals(DNS.MX_RECORD.replace(DNS.SERVER_DNS_NAME, "mail.example.com"), records.get(0));
    }

    @Test
    void givenSPFRecordType_whenCreateDNSRecords_thenReturnSPFRecord() {
        Request request = new Request();
        request.setExternalIP("192.168.1.1");
        List<String> records = DNSValidator.createDNSRecords(DNSRecordType.SPF.name(), request);
        assertNotNull(records);
        assertEquals(1, records.size());
        assertEquals(DNS.SPF_RECORD.replace(DNS.EXTERNAL_IP, "192.168.1.1"), records.get(0));
    }

    @Test
    void givenUnsupportedRecordType_whenCreateDNSRecords_thenThrowIllegalArgumentException() {
        Request request = new Request();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            DNSValidator.createDNSRecords("INVALID_TYPE", request);
        });
        assertEquals("Unsupported DNS record type: INVALID_TYPE", exception.getMessage());
    }

}