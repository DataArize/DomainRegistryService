package org.acme.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.MediaType;
import org.acme.exceptions.DNSAlreadyExistsException;
import org.acme.service.DomainValidationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.anyList;

@QuarkusTest
class DomainResourceTest {

    @InjectMock
    private DomainValidationService domainValidationService;

    @Test
    void givenValidDomain_whenDomainsValidateCalled_thenReturn200() {
        Mockito.when(domainValidationService.validateDomain(anyList()))
                .thenReturn(Uni.createFrom().voidItem());
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.singletonList("google.com"))
                .when().post("/domains/validate")
                .then()
                .statusCode(200);
    }

    @Test
    void givenValidDomain_whenDomainsValidateCalled_thenReturn409() {
        Mockito.when(domainValidationService.validateDomain(anyList()))
                .thenReturn(Uni.createFrom().failure(new IOException("MOCK")));
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.singletonList("-google.com"))
                .when().post("/domains/validate")
                .then()
                .statusCode(409);
    }

    @Test
    void givenValidDomainWithDNS_whenDomainsLookupCalled_thenReturn409() {
        Mockito.when(domainValidationService.validateDNSEntries(anyList()))
                .thenReturn(Uni.createFrom().failure(new DNSAlreadyExistsException("MOCK")));
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.singletonList("google.com"))
                .when().post("/domains/lookup")
                .then()
                .statusCode(409);
    }

    @Test
    void givenValidDomainWithDNS_whenDomainsLookupCalled_thenReturn500() {
        Mockito.when(domainValidationService.validateDNSEntries(anyList()))
                .thenReturn(Uni.createFrom().failure(new IOException("MOCK")));
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.singletonList("google.com"))
                .when().post("/domains/lookup")
                .then()
                .statusCode(500);
    }

    @Test
    void givenValidDomainWithDNS_whenDomainsLookupCalled_thenReturn200() {
        Mockito.when(domainValidationService.validateDomain(anyList()))
                .thenReturn(Uni.createFrom().voidItem());
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.singletonList(" "))
                .when().post("/domains/lookup")
                .then()
                .statusCode(200);
    }
}