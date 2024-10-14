package org.acme.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.acme.constants.Errors;
import org.acme.constants.Success;
import org.acme.exceptions.DNSAlreadyExistsException;
import org.acme.exceptions.ManagedZoneCreationFailed;
import org.acme.exceptions.UnableToAddDNSEntries;
import org.acme.model.Request;
import org.acme.service.DNSManagementService;

import java.util.List;

@Slf4j
@Path("/dns")
public class DNSResource {

    private final DNSManagementService dnsManagementService;

    @Inject
    public DNSResource(DNSManagementService dnsManagementService) {
        this.dnsManagementService = dnsManagementService;
    }


    /**
     * API used to create Managed Zone
     * in Cloud DNS
     * @param domains "List of domains"
     * @return "List<ManagedZone>"
     */
    @POST
    @Path("/zones")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> createCloudDNSManagedZone(List<String> domains) {
        return dnsManagementService.provisionDNSManagedZone(domains)
                .onItem().transform(managedZones -> Response.status(Response.Status.CREATED)
                            .entity(managedZones)
                            .build())
                .onFailure(ManagedZoneCreationFailed.class)
                .recoverWithItem(ex -> {
                    log.error("Unable to create Managed Zone: {}", ex.getMessage());
                    return Response.status(Response.Status.CONFLICT)
                            .entity(new org.acme.model.Response(ex.getMessage()))
                            .build();
                })
                .onFailure().recoverWithItem(ex -> {
                    log.error("Unexpected error occurred while creating Managed Zone: {}", ex.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new org.acme.model.Response(Errors.UNEXPECTED_ERROR_OCCURRED + ex.getMessage()))
                            .build();
                });
    }

    @POST
    @Path("/zones/{zoneId}/records")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> addDnsRecordToZone(@PathParam("zoneId") String zoneId, List<Request> dnsRequests) {
        return dnsManagementService.manageDnsEntryService(dnsRequests, zoneId)
                .onItem().transform(dnsRequest -> Response.status(Response.Status.CREATED)
                        .entity(dnsRequest)
                        .build())
                .onFailure(UnableToAddDNSEntries.class)
                .recoverWithItem(ex -> {
                    log.error("Unable to create DNS entries for Managed Zone, Exception: {}", ex.getMessage());
                    return Response.status(Response.Status.CONFLICT)
                            .entity(new org.acme.model.Response(ex.getMessage()))
                            .build();
                })
                .onFailure().recoverWithItem(ex -> {
                    log.error("Unexpected error occurred while adding DNS records, Exception: {}", ex.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new org.acme.model.Response(Errors.UNEXPECTED_ERROR_OCCURRED + ex.getMessage()))
                            .build();
                });
    }
}
