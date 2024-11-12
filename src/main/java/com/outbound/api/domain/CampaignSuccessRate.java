package com.outbound.api.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "campaign_success_rates")
public class CampaignSuccessRate {
    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    private String campaignID;
    private String campaignName;
    private SuccessRate successRate;
}
