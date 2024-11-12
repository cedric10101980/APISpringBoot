package com.outbound.api.repository;

import com.outbound.api.domain.CampaignContactListProbability;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CampaignContactListProbabilityRepository extends MongoRepository<CampaignContactListProbability, String> {
    CampaignContactListProbability findByCampaignName(String campaignName);
    void deleteByCampaignName(String campaignName);
    CampaignContactListProbability findByCampaignId(String campaignId);
}
