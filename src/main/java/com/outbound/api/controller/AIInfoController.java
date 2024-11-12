package com.outbound.api.controller;

import com.outbound.api.domain.CampaignContactListProbability;
import com.outbound.api.domain.CampaignSuccessRate;
import com.outbound.api.domain.PhoneNumberProbability;
import com.outbound.api.dto.DBCampaignSaveResponse;
import com.outbound.api.service.AIInfoDBService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AIInfoController {
    private final AIInfoDBService aiInfoDBService;

    @PostMapping("/phone-number-probabilities")
    @Tag(name = "Probabilities", description = "APIs related to PhoneNumberProbabilities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "PhoneNumberProbability created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid PhoneNumberProbability input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Saves the PhoneNumberProbability in DB", description = "This operation creates/updates a PhoneNumberProbability document and saves it to MongoDB")
    public ResponseEntity<DBCampaignSaveResponse> createPhoneNumberProbability(@RequestBody CampaignContactListProbability contactListProbability) {
        DBCampaignSaveResponse response = aiInfoDBService.saveAll(contactListProbability);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/phone-number-probabilities/{campaignName}")
    @Tag(name = "Probabilities", description = "APIs related to PhoneNumberProbabilities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PhoneNumberProbability found"),
            @ApiResponse(responseCode = "404", description = "PhoneNumberProbability not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Get all PhoneNumberProbabilities by campaign name", description = "This operation retrieves all PhoneNumberProbabilities by campaign name from MongoDB")
    public ResponseEntity<CampaignContactListProbability> getPhoneNumberProbabilitiesByCampaignName(@PathVariable String campaignName) {
        CampaignContactListProbability response = aiInfoDBService.getByCampaignName(campaignName);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/phone-number-probabilities/campaign/{campaignId}")
    @Tag(name = "Probabilities", description = "APIs related to PhoneNumberProbabilities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PhoneNumberProbability found"),
            @ApiResponse(responseCode = "404", description = "PhoneNumberProbability not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Get all PhoneNumberProbabilities by campaign id", description = "This operation retrieves all PhoneNumberProbabilities by campaign id from MongoDB")
    public ResponseEntity<CampaignContactListProbability> getPhoneNumberProbabilitiesByCampaignId(@PathVariable String campaignId) {
        CampaignContactListProbability response = aiInfoDBService.getByCampaignId(campaignId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/phone-number-probabilities/{campaignName}/{contactID}")
    @Tag(name = "Probabilities", description = "APIs related to PhoneNumberProbabilities")
    @Operation(summary = "Get CampaignContactListProbability by campaign name and contact id",
            description = "This operation retrieves a CampaignContactListProbability by campaign name and contact id using Chat GPT API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CampaignContactListProbability found"),
            @ApiResponse(responseCode = "404", description = "CampaignContactListProbability not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PhoneNumberProbability> getByPhoneNumberAndCampaignName(@PathVariable String campaignName, @PathVariable String contactID) {
        PhoneNumberProbability phoneNumberProbability = aiInfoDBService.getByPhoneNumberProbabilities(campaignName, contactID);
        return new ResponseEntity<>(phoneNumberProbability, HttpStatus.OK);
    }

    // add a delete by campaign name api
    @DeleteMapping("/phone-number-probabilities/{campaignName}")
    @Tag(name = "Probabilities", description = "APIs related to PhoneNumberProbabilities")
    @Operation(summary = "Delete all PhoneNumberProbabilities by campaign name",
            description = "This operation deletes all PhoneNumberProbabilities by campaign name from MongoDB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PhoneNumberProbabilities deleted"),
            @ApiResponse(responseCode = "404", description = "PhoneNumberProbabilities not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> deleteByCampaignName(@PathVariable String campaignName) {
        String response = aiInfoDBService.deleteAllByCampaignName(campaignName);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/campaigns-success-rates")
    @Tag(name = "Success Rates", description = "APIs related to CampaignSuccessRate")
    @Operation(summary = "Upload a list of CampaignCallRecord Success rate info", description = "This operation creates a new CampaignsInfo and saves it to MongoDB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "CampaignsInfo created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid CampaignsInfo input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CampaignSuccessRate>> createCampaignsInfo(@RequestBody List<CampaignSuccessRate> campaignsInfo) {
        List<CampaignSuccessRate> savedCampaignsInfo = aiInfoDBService.saveCampaignsInfo(campaignsInfo);
        return new ResponseEntity<>(savedCampaignsInfo, HttpStatus.CREATED);
    }

    @GetMapping("/campaigns-success-rates")
    @Tag(name = "Success Rates", description = "APIs related to CampaignSuccessRate")
    @Operation(summary = "Get all campaigns success rate", description = "This operation retrieves all campaigns success rates from MongoDB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CampaignsInfo found"),
            @ApiResponse(responseCode = "404", description = "CampaignsInfo not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CampaignSuccessRate>> getAllCampaigns() {
        List<CampaignSuccessRate> campaignsInfo = aiInfoDBService.getAllCampaignsInfo();
        return new ResponseEntity<>(campaignsInfo, HttpStatus.OK);
    }

    @GetMapping("/campaigns-success-rates/{campaignId}")
    @Tag(name = "Success Rates", description = "APIs related to CampaignSuccessRate")
    @Operation(summary = "Get success rate for a campaign by id", description = "This operation retrieves the success rate of a campaign by its id from MongoDB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CampaignSuccessRate found"),
            @ApiResponse(responseCode = "404", description = "CampaignSuccessRate not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CampaignSuccessRate> getCampaignSuccessRateById(@PathVariable String campaignId) {
        CampaignSuccessRate campaignSuccessRate = aiInfoDBService.findById(campaignId);
        return new ResponseEntity<>(campaignSuccessRate, HttpStatus.OK);
    }

    @DeleteMapping("/campaigns-success-rates")
    @Tag(name = "Success Rates", description = "APIs related to CampaignSuccessRate")
    @Operation(summary = "Delete all CampaignSuccessRate", description = "This operation deletes all CampaignSuccessRate from MongoDB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CampaignSuccessRate deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteAllCampaignsInfo() {
        aiInfoDBService.deleteAllCampaignsInfo();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}