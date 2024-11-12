package com.outbound.api.service;

import com.outbound.api.domain.CampaignContactListProbability;
import com.outbound.api.domain.CampaignSuccessRate;
import com.outbound.api.domain.CannedPrompt;
import com.outbound.api.domain.PhoneNumberProbability;
import com.outbound.api.dto.DBCampaignSaveResponse;
import com.outbound.api.repository.CampaignContactListProbabilityRepository;
import com.outbound.api.repository.CampaignsInfoRepository;
import com.outbound.api.repository.PromptDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AIInfoDBService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AIInfoDBService.class);
    private final CampaignContactListProbabilityRepository campaignContactListProbability;
    private final CampaignsInfoRepository campaignsInfoRepository;
    private final PromptDataRepository promptDataRepository;
    private final LLMQueryService llmQueryService;

    @Autowired
    private MongoTemplate mongoTemplate;

    public DBCampaignSaveResponse saveAll(CampaignContactListProbability contactListProbability) {
        try {
            deleteAllByCampaignName(contactListProbability.getCampaignName());
            CampaignContactListProbability savedCampaignContactListProbability = campaignContactListProbability.save(contactListProbability);

            DBCampaignSaveResponse response = new DBCampaignSaveResponse();
            response.setCampaignId(savedCampaignContactListProbability.getId());
            response.setCampaignName(savedCampaignContactListProbability.getCampaignName());

            LOGGER.info("CampaignContactListProbability with ID {} and name  {} successfully saved", response.getCampaignId(), response.getCampaignName());

            return response;
        } catch (MongoException e) {
            LOGGER.error("Error saving PhoneNumberProbability", e);
            throw e;
        }
    }

    public List<CampaignSuccessRate> insertCampaignSuccessRate(String jsonData) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<CampaignSuccessRate> campaignSuccessRateList = objectMapper.readValue(jsonData, new TypeReference<List<CampaignSuccessRate>>(){});
        List<CampaignSuccessRate> campaignSuccessRates = campaignsInfoRepository.saveAll(campaignSuccessRateList);
        return campaignSuccessRates;
    }

    public CampaignContactListProbability getByCampaignName(String campaignName) {
        Optional<CampaignContactListProbability> optionalCampaign = Optional.ofNullable(campaignContactListProbability.findByCampaignName(campaignName));

        if (optionalCampaign.isPresent()) {
            return optionalCampaign.get();
        } else {
            CannedPrompt probabilityPrompt = promptDataRepository.findByType("ADMIN_PROBABILITY");
            String promptQuery = probabilityPrompt.getPrompt();
            promptQuery = promptQuery.replace("$$CAMPAIGN_NAME$$", campaignName);
            Mono<ResponseEntity<String>> probabilityResult = llmQueryService.query(promptQuery, "gpt-4-0125-preview");
            probabilityResult.subscribe(responseEntity -> {
            try {
                String responseBody = responseEntity.getBody();
                String json = responseBody.substring(1, responseBody.length() - 1);
                json = json.replace("\\", "");
                ObjectMapper objectMapper = new ObjectMapper();
                CampaignContactListProbability campaignContactListProbability = objectMapper.readValue(json, CampaignContactListProbability.class);
                DBCampaignSaveResponse response = saveAll(campaignContactListProbability);
                LOGGER.info("Probability Insertion Done for CampaignName : " + response.getCampaignName());

                CannedPrompt successRatePrompt = promptDataRepository.findByType("ADMIN_SUCCESS_RATE");
                String nextPromptQuery = successRatePrompt.getPrompt();
                ObjectMapper objectMapper1 = new ObjectMapper();
                String campaignProbabilityJson = objectMapper1.writeValueAsString(campaignContactListProbability);
                nextPromptQuery = nextPromptQuery.replace("$$CAMPAIGN_PROBABILITY$$", campaignProbabilityJson);
                Mono<ResponseEntity<String>> successResult = llmQueryService.query(nextPromptQuery, "gpt-4-0125-preview");
                successResult.subscribe(successEntity->{
                    try {
                        String successResponseBody = successEntity.getBody();
                        String successJson = successResponseBody.substring(1, successResponseBody.length() - 1);
                        successJson = successJson.replace("\\", "");
                        List<CampaignSuccessRate> campaignSuccessRates = insertCampaignSuccessRate(successJson);
                        LOGGER.info("Success Rate Insertion Done for CampaignName : " + campaignSuccessRates.get(0).getCampaignName());
                    } catch (Exception e) {
                        LOGGER.error("Error: " + e.getMessage());
                    }
                }, error -> {
                    LOGGER.error("Error: " + error.getMessage());
                });
            } catch (JsonProcessingException e) {
                LOGGER.error("Error: " + e.getMessage());
            }
            }, error -> LOGGER.error("Error: " + error.getMessage()));
            throw new RuntimeException("CampaignContactListProbability with name " + campaignName + " not found");
        }
    }

    public CampaignContactListProbability getByCampaignId(String campaignId) {
        Optional<CampaignContactListProbability> optionalCampaign = Optional.ofNullable(campaignContactListProbability.findByCampaignId(campaignId));

        if (optionalCampaign.isPresent()) {
            return optionalCampaign.get();
        } else {
            LOGGER.error("CampaignContactListProbability with id {} not found", campaignId);
            throw new RuntimeException("CampaignContactListProbability with id " + campaignId + " not found");
        }
    }

    public String deleteByCampaignName(String campaignName) {
        CampaignContactListProbability campaign = getByCampaignName(campaignName);
        campaignContactListProbability.delete(campaign);
        LOGGER.info("CampaignContactListProbability with name {} successfully deleted", campaignName);
        return campaignName;
    }

    public String deleteAllByCampaignName(String campaignName) {
        campaignContactListProbability.deleteByCampaignName(campaignName);
        LOGGER.info("All CampaignContactListProbability entities with name {} successfully deleted", campaignName);
        return campaignName;
    }

    public List<CampaignContactListProbability> getAllCampaignContactListProbabilities() {
        List<CampaignContactListProbability> allCampaigns = campaignContactListProbability.findAll();
        LOGGER.info("Retrieved all CampaignContactListProbabilities");
        return allCampaigns;
    }

    public PhoneNumberProbability getByPhoneNumberProbabilities(String campaignName, String contactId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("campaignName").is(campaignName)
                .andOperator(Criteria.where("phoneNumberProbabilities.contactID").is(contactId)));
        query.fields().include("phoneNumberProbabilities.$");

        CampaignContactListProbability campaignContactListProbability = mongoTemplate.findOne(query, CampaignContactListProbability.class);

        if (campaignContactListProbability == null || campaignContactListProbability.getPhoneNumberProbabilities() == null) {
            LOGGER.error("PhoneNumberProbability with contact id {} not found", contactId);
            throw new RuntimeException("PhoneNumberProbability with contact id " + contactId + " not found");
        }

        // Assuming each campaign has unique phone numbers, we can directly return the first element
        return campaignContactListProbability.getPhoneNumberProbabilities().get(0);
    }

    public List<CampaignSuccessRate> saveCampaignsInfo(List<CampaignSuccessRate> campaignsInfoList) {
        return campaignsInfoRepository.saveAll(campaignsInfoList);
    }

    public List<CampaignSuccessRate> getAllCampaignsInfo() {
        return campaignsInfoRepository.findAll();
    }

    public CampaignSuccessRate findById(String campaignId) {
        return campaignsInfoRepository.findBycampaignID(campaignId);
    }

    public void deleteAllCampaignsInfo() {
        campaignsInfoRepository.deleteAll();
    }
}
