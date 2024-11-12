package com.outbound.api.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequest {
    private String name;
    private String emailAddress;
    private String campaignName;
    private String bestTimeToCall;

    // getters and setters...
}