package org.acme.utils;

import com.google.api.services.dns.model.ManagedZone;
import com.google.api.services.dns.model.ManagedZoneDnsSecConfig;
import com.google.cloud.dns.ZoneInfo;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.modifier.Visibility;
import org.acme.constants.DNS;
import org.acme.constants.DNSState;
import org.acme.constants.Errors;
import org.acme.constants.ZoneVisibility;
import org.acme.exceptions.DNSAlreadyExistsException;
import org.acme.exceptions.InvalidDomainNameException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;


import java.util.Arrays;

@Slf4j
public class DNSValidator {

    private DNSValidator() {}

    /**
     * Looks up for NS record entries
     * @param domain "Domain name"
     * @return "Void"
     */
    public static Uni<Object> doesNameServerExists(String domain) {
        return Uni.createFrom().item(() -> {
            try {
                log.info("Checking for NS records for domain: {}", domain);
                Lookup lookup = new Lookup(domain, Type.NS);
                Record[] records = lookup.run();
                if (lookup.getResult() == Lookup.SUCCESSFUL) {
                    Arrays.asList(records).forEach(rec -> log.info("NS record: {}", rec));
                    throw new DNSAlreadyExistsException(Errors.NS_RECORD_ALREADY_EXISTS + domain);
                }
                log.info("No NS record found for domain: {}", domain);
                return null;
            } catch (TextParseException ex) {
                log.error("Invalid domain name: {}", domain, ex);
                throw new InvalidDomainNameException(Errors.INVALID_DOMAIN_PROVIDED + domain);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    public static ManagedZone initializeManagedZone(String name, String dnsName) {
        ManagedZone managedZone = new ManagedZone();
        managedZone.setName(name);
        managedZone.setDnsName(dnsName);
        managedZone.setDescription(DNS.DNS_DESCRIPTION);
        managedZone.setVisibility(ZoneVisibility.PUBLIC.getValue());
        managedZone.setDnssecConfig(initializeDnsSecConfig());
        return managedZone;
    }

    public static ManagedZoneDnsSecConfig initializeDnsSecConfig() {
        ManagedZoneDnsSecConfig dnsSecConfig = new ManagedZoneDnsSecConfig();
        dnsSecConfig.setState(DNSState.ON.getValue());
        return dnsSecConfig;

    }

}
