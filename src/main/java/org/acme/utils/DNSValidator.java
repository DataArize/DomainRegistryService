package org.acme.utils;

import com.google.api.services.dns.model.ManagedZone;
import com.google.api.services.dns.model.ManagedZoneCloudLoggingConfig;
import com.google.api.services.dns.model.ManagedZoneDnsSecConfig;
import com.google.cloud.dns.ZoneInfo;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.modifier.Visibility;
import org.acme.constants.*;
import org.acme.exceptions.DNSAlreadyExistsException;
import org.acme.exceptions.InvalidDomainNameException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * Utility function to create a ManagedZone Object
     * @param name "Managed Zone name"
     * @param dnsName "DNS name"
     * @param activeProfile "Active profile"
     * @return "ManagedZone"
     */
    public static ManagedZone initializeManagedZone(String name, String dnsName,
                                                    String activeProfile) {
        ManagedZone managedZone = new ManagedZone();
        managedZone.setName(name);
        managedZone.setDnsName(dnsName);
        managedZone.setDescription(DNS.DNS_DESCRIPTION);
        managedZone.setVisibility(ZoneVisibility.PUBLIC.getValue());
        managedZone.setDnssecConfig(initializeDnsSecConfig());
        managedZone.setCloudLoggingConfig(cloudLoggingConfig());
        return managedZone;
    }

    /**
     * Utility Function to enable dnsSecConfig
     * @return "ManagedZoneDnsSecConfig"
     */
    public static ManagedZoneDnsSecConfig initializeDnsSecConfig() {
        ManagedZoneDnsSecConfig dnsSecConfig = new ManagedZoneDnsSecConfig();
        dnsSecConfig.setState(DNSState.ON.getValue());
        return dnsSecConfig;
    }

    /**
     * Utility function to enable cloud logging
     * @return "ManagedZoneCloudLoggingConfig"
     */
    public static ManagedZoneCloudLoggingConfig cloudLoggingConfig() {
        ManagedZoneCloudLoggingConfig cloudLoggingConfig = new ManagedZoneCloudLoggingConfig();
        cloudLoggingConfig.setEnableLogging(true);
        return  cloudLoggingConfig;
    }

    /**
     * Utility function to generate the base domain
     * @param domain "domain name"
     * @return "Project label"
     */
    public static String getBaseDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            throw new InvalidDomainNameException(Errors.NO_DOMAINS_PROVIDED);
        }
        int dotIndex = domain.indexOf('.');
        if (dotIndex == -1) {
            return DNS.EMAIL_SERVER + domain;
        }
        return DNS.EMAIL_SERVER + domain.substring(0, dotIndex);
    }

}
