package com.outbound.api.repository;


import com.outbound.api.domain.CampaignSuccessRate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CampaignsInfoRepository extends MongoRepository<CampaignSuccessRate, String> {
    List<CampaignSuccessRate> findAll();

    CampaignSuccessRate findBycampaignID(String campaignId);

}
