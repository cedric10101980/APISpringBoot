package com.outbound.api.repository;

import com.outbound.api.model.CallRecord;
import com.outbound.api.model.CampaignCallRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CampaignRepository extends MongoRepository<CampaignCallRecord, String> {

    List<CampaignCallRecord> findAll();
    //List<CampaignCallRecord> findByCampaignName(String campaignName);
    List<CampaignCallRecord> findAllByCampaignName(String campaignName);

    List<CampaignCallRecord> findAllByCampaignId(String campaignId);

    void deleteAllByCampaignName(String campaignName);
    void deleteAllByCampaignId(String campaignId);

    @Query("{ 'campaignId' : ?0 , 'callRecords' : { $elemMatch: { 'phoneNumber' : ?1 } } }")
    List<CallRecord> findCallRecordsByCampaignIdAndPhoneNumber(String campaignId, String phoneNumber);
}