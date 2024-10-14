package org.acme.constants;

public enum DNSRecordType {
    MX("MX", "MX records specify the mail server responsible for receiving emails."),
    SPF("TXT", "This is a TXT record used for SPF, which helps specify which mail servers are allowed to send email for your domain."),
    DKIM("TXT", "DKIM uses cryptographic signatures to verify the authenticity of the sender’s domain."),
    DMARC( "TXT", "DMARC records allow you to specify what happens to emails that fail SPF or DKIM validation."),
    A("A", "An A record points the mail.houseofllm.com subdomain to the mail server's IP address."),
    CNAME("CNAME", "A CNAME record aliases one domain to another. ");

    private final String value;
    private final String description;

    DNSRecordType(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
