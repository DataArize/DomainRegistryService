package org.acme.resource;

import com.google.api.services.dns.model.ManagedZone;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.MediaType;
import org.acme.exceptions.ManagedZoneCreationFailed;
import org.acme.service.DNSManagementService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.anyList;

@QuarkusTest
class DNSResourceTest {

    @InjectMock
    private DNSManagementService dnsManagementService;


    @Test
    void givenValidDomain_whenCreateCloudDNSManagedZoneCalled_thenReturn201() {
        Mockito.when(dnsManagementService.provisionDNSManagedZone(anyList()))
                .thenReturn(Uni.createFrom().item(List.of(new ManagedZone())));
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.singletonList("houseofllm.com."))
                .when().post("/dns/create")
                .then()
                .statusCode(201);
    }

    @Test
    void givenValidDomain_whenCreateCloudDNSManagedZoneCalled_thenReturn500() {
        Mockito.when(dnsManagementService.provisionDNSManagedZone(anyList()))
                .thenReturn(Uni.createFrom().failure(new IOException("MOCK MESSAGE")));
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.singletonList("houseofllm.com."))
                .when().post("/dns/create")
                .then()
                .statusCode(500);
    }

    @Test
    void givenValidDomain_whenCreateCloudDNSManagedZoneCalled_thenReturn409() {
        Mockito.when(dnsManagementService.provisionDNSManagedZone(anyList()))
                .thenReturn(Uni.createFrom().failure(new ManagedZoneCreationFailed("MOCK MESSAGE")));
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.singletonList("houseofllm.com."))
                .when().post("/dns/create")
                .then()
                .statusCode(409);
    }


}