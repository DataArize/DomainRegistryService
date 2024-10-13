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
import org.acme.exceptions.ManagedZoneCreationFailed;
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


    @POST
    @Path("/create")
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
                            .entity(new org.acme.model.Response("An unexpected error occurred."))
                            .build();
                });
    }
}
