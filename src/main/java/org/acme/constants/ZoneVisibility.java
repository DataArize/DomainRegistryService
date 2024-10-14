package org.acme.constants;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ZoneVisibility {
    PUBLIC("public", "Indicates that records in this zone can be queried from the public internet."),
    PRIVATE("private", "Indicates that records in this zone cannot be queried from the public internet. Access to private zones depends on the zone configuration.");

    private final String value;
    private final String description;

    ZoneVisibility(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
