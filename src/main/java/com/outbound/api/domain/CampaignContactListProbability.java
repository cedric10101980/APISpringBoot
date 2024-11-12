package com.outbound.api.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Data   // Lombok annotation to create all the getters, setters, equals, hash, and toString methods, based on the fields
@Document(collection = "campaign_contact_list_probabilities")
public class CampaignContactListProbability {

    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    private String campaignName;
    private String campaignId;
    private List<PhoneNumberProbability> phoneNumberProbabilities;

    // getters and setters
}