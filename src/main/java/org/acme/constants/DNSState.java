package org.acme.constants;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum DNSState {
    OFF("off", "DNSSEC is disabled; the zone is not signed."),
    ON("on", "DNSSEC is enabled; the zone is signed and fully managed."),
    TRANSFER("transfer", "DNSSEC is enabled, but in a 'transfer' mode.");

    private final String value;
    private final String description;

    DNSState(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
