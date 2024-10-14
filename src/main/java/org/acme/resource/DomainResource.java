package org.acme.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.acme.constants.Errors;
import org.acme.constants.Success;
import org.acme.exceptions.DNSAlreadyExistsException;
import org.acme.service.DomainValidationService;

import java.util.List;

@Slf4j
@Path("/domains")
public class DomainResource {


    private final DomainValidationService domainValidationService;

    @Inject
    public DomainResource(DomainValidationService domainValidationService) {
        this.domainValidationService = domainValidationService;
    }

    /**
     * version: 1.0.0
     * API used to get list of domains as input
     * and validate the domain name format
     * @param domains "List of domains"
     * @return "Domains Received"
     */
    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> validateDomainFormat(List<String> domains) {
        log.info("Received request to validate domains: {}", domains);
        return domainValidationService.validateDomain(domains).onItem()
                .transform(success -> Response.status(Response.Status.OK)
                        .entity(new org.acme.model.Response(Success.DOMAINS_RECEIVED_AND_PROCESSED))
                        .build())
                .onFailure()
                .recoverWithItem(ex -> {
                    log.error("Invalid Domain name format: {}", ex.getMessage());
                    return Response.status(Response.Status.CONFLICT)
                            .entity(new org.acme.model.Response(ex.getMessage())).build();
                });

    }

    /**
     * API used to lookup DNS entries
     * Note: We do not need any domain which
     * already has a name server. This API throws
     * an error if NS record already exists for
     * the domain
     * @param domains "List of domains"
     * @return "List of domains"
     */
    @POST
    @Path("/lookup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> validateDNSEntries(List<String> domains) {
        return domainValidationService.validateDNSEntries(domains)
                .onItem().transform(success -> Response.status(Response.Status.OK)
                        .entity(new org.acme.model.Response(Success.NO_DNS_ENTRIES_EXISTS)).build())
                .onFailure(DNSAlreadyExistsException.class)
                .recoverWithItem(ex -> {
                    log.error("DNS entries already exist: {}", ex.getMessage());
                    return Response.status(Response.Status.CONFLICT)
                            .entity(new org.acme.model.Response(ex.getMessage())).build();
                })
                .onFailure().recoverWithItem(ex -> {
                    log.error("Unexpected error: {}", ex.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new org.acme.model.Response(Errors.UNEXPECTED_ERROR_OCCURRED + ex.getMessage())).build();
                });
    }
}
