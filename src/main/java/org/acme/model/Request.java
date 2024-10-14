package org.acme.model;

import lombok.Data;

@Data
public class Request {

    private String domain;
    private String serverDNS;
    private String externalIP;
}
