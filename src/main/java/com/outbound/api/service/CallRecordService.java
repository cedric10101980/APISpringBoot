package com.outbound.api.service;


import com.outbound.api.model.CallRecord;
import com.outbound.api.model.CampaignCallRecord;
import com.outbound.api.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CallRecordService {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void insertCallRecord(CampaignCallRecord campaignCallRecord) {
        campaignRepository.save(campaignCallRecord);
        // Retrieve the existing CampaignCallRecord document
        /*Optional<CampaignCallRecord> existingCampaignCallRecord = campaignRepository.findCallRecordsByCampaignIdAndPhoneNumber(campaignCallRecord.getCampaignId(), campaignCallRecord.getPhoneNumber());

        if (existingCampaignCallRecord.isPresent()) {
            // If the document exists, add the new CallRecord to the existing list
            //existingCampaignCallRecord.get().getCallRecords().addAll(campaignCallRecord.getCallRecords());
            // Save the updated document back to the database
            campaignRepository.save(existingCampaignCallRecord.get());
        } else {
            // If the document does not exist, save the new CampaignCallRecord
            campaignRepository.save(campaignCallRecord);
        }*/
    }

    public List<CampaignCallRecord> getAllCallRecords(String campaignId, String contactId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("campaignId").is(campaignId));
        List<CampaignCallRecord> campaignCallRecords = mongoTemplate.find(query, CampaignCallRecord.class);

        List<CampaignCallRecord> matchingCallRecords = new ArrayList<>();
        for (CampaignCallRecord campaignCallRecord : campaignCallRecords) {
            //for (CallRecord callRecord : campaignCallRecord.getCallRecords()) {
                if (campaignCallRecord.getContactId().equals(contactId)) {
                    matchingCallRecords.add(campaignCallRecord);
                }
            //}
        }

        return matchingCallRecords;
    }

    public List<CallRecord> getCallRecordsByCampaignIdAndPhoneNumber(String campaignId, String phoneNumber) {
        // Retrieve all CallRecord objects by campaignId and phoneNumber
        return campaignRepository.findCallRecordsByCampaignIdAndPhoneNumber(campaignId, phoneNumber);
    }

    public List<CampaignCallRecord> getAllCallRecords() {
        return campaignRepository.findAll();
    }

    public List<CampaignCallRecord> getCallRecordsByCampaignName(String campaignName) {
        return campaignRepository.findAllByCampaignName(campaignName);
    }

    public List<CampaignCallRecord> getCallRecordsByCampaignId(String campaignId) {
        return campaignRepository.findAllByCampaignId(campaignId);
    }

    public void deleteCallRecordsByCampaignName(String campaignName) {
        campaignRepository.deleteAllByCampaignName(campaignName);
    }

    public void deleteCallRecordsByCampaignId(String campaignId) {
        campaignRepository.deleteAllByCampaignId(campaignId);
    }
}
