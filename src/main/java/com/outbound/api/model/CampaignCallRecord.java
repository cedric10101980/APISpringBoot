package com.outbound.api.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "campaign_call_records")
@Data
public class CampaignCallRecord {

    private String campaignName;
    private String campaignId;
    private String contactId;
    private String phoneNumber;
    private String timestamp;
    private String callDuration;
    private String completionCode;
}