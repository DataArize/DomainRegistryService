package org.acme.constants;

public final class Errors {
    private Errors() {}

    public static final String NO_DOMAINS_PROVIDED = "No domains provided";
    public static final String INVALID_DOMAIN_PROVIDED = "Invalid domain provided, Domain Name: ";
    public static final String NS_RECORD_ALREADY_EXISTS = "NS records already exist for domain: ";
    public static final String UNEXPECTED_ERROR_OCCURRED = "An unexpected error occurred, Exception: ";
    public static final String UNABLE_TO_CREATE_MANAGED_ZONE = "Unable to create Manged Zone, Exception: ";
    public static final String UNABLE_TO_CREATE_DNS_ENTRIES = "Unable to create DNS records for Managed Zone: ";
    public static final String EXCEPTION = ", Exception: ";
}
