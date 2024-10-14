package org.acme.service;

import com.google.api.services.dns.Dns;
import com.google.api.services.dns.model.ManagedZone;
import com.google.api.services.dns.model.ResourceRecordSet;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.acme.exceptions.ManagedZoneCreationFailed;
import org.acme.exceptions.UnableToAddDNSEntries;
import org.acme.model.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

@QuarkusTest
class DNSManagementServiceTest {

    private Request request;
    private Dns cloudDnsClient;
    private Dns.ManagedZones managedZones;
    private Dns.ResourceRecordSets resourceRecordSets;
    private Dns.ManagedZones.Create createRequest;
    private Dns.ResourceRecordSets.Create recordSetRequest;
    private DNSManagementService dnsManagementService;

    @BeforeEach()
    void setUp() throws IOException {
        request = new Request();
        request.setDomain("MOCK DOMAIN");
        request.setServerDNS("MOCK SERVER DNS");
        request.setExternalIP("MOCK EXTERNAL IP");
        cloudDnsClient = mock(Dns.class);
        dnsManagementService = new DNSManagementService("MOCK", "MOCK",cloudDnsClient);
        createRequest = mock(Dns.ManagedZones.Create.class);
        recordSetRequest = mock(Dns.ResourceRecordSets.Create.class);
        managedZones = mock(Dns.ManagedZones.class);
        resourceRecordSets = mock(Dns.ResourceRecordSets.class);
        Mockito.when(cloudDnsClient.managedZones()).thenReturn(managedZones);
        Mockito.when(cloudDnsClient.resourceRecordSets()).thenReturn(resourceRecordSets);
        Mockito.when(resourceRecordSets.create(anyString(), anyString(), any(ResourceRecordSet.class)))
                        .thenReturn(recordSetRequest);
        Mockito.when(managedZones.create(anyString(), any(ManagedZone.class)))
                .thenReturn(createRequest);
    }

    @Test
    void givenValidDomain_whenProvisionDNSManagedZoneCalled_thenReturnManagedZone() throws IOException {
        ManagedZone managedZoneResponse = new ManagedZone();
        managedZoneResponse.setDnsName("MOCK");
        Mockito.when(createRequest.execute()).thenReturn(managedZoneResponse);
        Uni<List<ManagedZone>> listUni = dnsManagementService.provisionDNSManagedZone(Collections.singletonList("houseofllm.com"));
        List<ManagedZone> managedZoneList = listUni.await().indefinitely();
        assertEquals(managedZoneResponse.getDnsName(), managedZoneList.get(0).getDnsName());
    }

    @Test
    void givenValidDomain_whenProvisionDNSManagedZoneCalled_thenThrowException() throws IOException {
        Mockito.when(createRequest.execute()).thenThrow(new IOException("Network Error"));
        assertThrows(ManagedZoneCreationFailed.class, () -> dnsManagementService
                .provisionDNSManagedZone(Collections.singletonList("houseofllm.com"))
                .await().indefinitely());
    }

    @Test
    void givenValidDomain_whenManageDnsEntryServiceCalled_thenReturnManagedZone() throws IOException {
        ResourceRecordSet recordSet = new ResourceRecordSet();
        recordSet.setName("MOCK");
        Mockito.when(recordSetRequest.execute()).thenReturn(recordSet);
        Uni<List<ResourceRecordSet>> listUni = dnsManagementService.manageDnsEntryService(Collections.singletonList(request), "MOCK");
        List<ResourceRecordSet> recordSets = listUni.await().indefinitely();
        assertEquals(recordSet.getName(), recordSets.get(0).getName());
    }

    @Test
    void givenValidDomain_whenManageDnsEntryServiceCalled_thenThrowException() throws IOException {
        Mockito.when(recordSetRequest.execute()).thenThrow(new IOException("Network Error"));
        assertThrows(UnableToAddDNSEntries.class, () -> dnsManagementService
                .manageDnsEntryService(Collections.singletonList(request), "MOCK")
                .await().indefinitely());
    }
}