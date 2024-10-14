package org.acme.constants;

public final class DNS {
    private DNS() {}

    public static final String DNS_DESCRIPTION = "Creating DNS entries for email server";
    public static final String ENVIRONMENT = "ENVIRONMENT";
    public static final String PROJECT = "PROJECT";
    public static final String TYPE = "TYPE";
    public static final String ORG = "ORG";
    public static final String EMAIL_SERVER = "EMAIL_SERVER_";
    public static final String DNS_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    public static final Integer DNS_TTL = 3600;
    public static final String MX_RECORD = "10 #SERVER_DNS_NAME";
    public static final String SPF_RECORD = "\"v=spf1 ip4:#EXTERNAL_IP -all\"";
    public static final String SERVER_DNS_NAME = "#SERVER_DNS_NAME";
    public static final String EXTERNAL_IP = "#EXTERNAL_IP";
}
