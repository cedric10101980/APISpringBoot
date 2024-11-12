package com.outbound.api.controller;

import com.outbound.api.domain.CampaignContactListProbability;
import com.outbound.api.model.CampaignCallRecord;
import com.outbound.api.repository.CampaignContactListProbabilityRepository;
import com.outbound.api.service.AIInfoDBService;
import com.outbound.api.service.CallRecordService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@CrossOrigin
@RestController
@Tag(name = "Call Records", description = "Stores and retrieves call records for campaigns")
@RequestMapping("/callRecords")
public class CallRecordController {

    private static final Logger LOGGER = LogManager.getLogger(CallRecordController.class);

    @Autowired
    private CallRecordService callRecordService;

    private AIInfoDBService aiInfoDBService;

    @Autowired
    private CampaignContactListProbabilityRepository campaignContactListProbability;

    @PostMapping
    @Operation(summary = "Insert a new call record",
            description = "This operation inserts a new campaign call record into the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "CampaignCallRecord inserted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> insertCampaign(@RequestBody CampaignCallRecord campaignCallRecord) {
        callRecordService.insertCallRecord(campaignCallRecord);
        return new ResponseEntity<>("Campaign Call Record inserted successfully", HttpStatus.CREATED);
    }

    @Hidden
    @GetMapping("/campaign/{campaignId}/phoneNumber/{contactId}")
    @Operation(summary = "Get all call records by campaign ID and phone number",
            description = "This operation retrieves all call records associated with a specific campaign ID and phone number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "CampaignCallRecord not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CampaignCallRecord>> getCallRecordsByCampaignIdAndPhoneNumber(@PathVariable String campaignId, @PathVariable String contactId) {
        List<CampaignCallRecord> callRecords = callRecordService.getAllCallRecords(campaignId, contactId);
        if (callRecords.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(callRecords, HttpStatus.OK);
        }
    }

    @GetMapping
    @Operation(summary = "Get all call records",
            description = "This operation retrieves all call records from the database.")
    public ResponseEntity<List<CampaignCallRecord>> getAllCampaigns() {
        List<CampaignCallRecord> campaignCallRecords = callRecordService.getAllCallRecords();
        return new ResponseEntity<>(campaignCallRecords, HttpStatus.OK);
    }

    @PostMapping(value = "/uploadCSV", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Process CSV file with contact call records",
            description = "This operation processes a CSV file and inserts into the database. The CSV file should have the following columns: PhoneNumber, TimeStamp, CallDuration, CompletionCode. The file should be named as <campaignName>__<campaignId>.csv. For example, if the campaign name is 'campaign1' and campaign id is '123', the file should be named 'campaign1__123.csv'. The file should be uploaded as a form-data part with the key 'file'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input, file is empty or not a CSV file"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<Map<String, String>>> uploadCSV(@RequestPart("file") Mono<FilePart> file) {
        return file.flatMap(part -> {
            // Check if the file is not empty
            if (part == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "The file is empty");
                return Mono.just(new ResponseEntity<>(response, HttpStatus.BAD_REQUEST));
            }

            // Check if the file is a CSV file
            if (!part.filename().toLowerCase().endsWith(".csv")) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "The file is not a CSV file");
                return Mono.just(new ResponseEntity<>(response, HttpStatus.BAD_REQUEST));
            }

            String fileName = part.filename();
            String[] parts = fileName.split("__");
            String campaignName = parts[0];
            String campaignId = parts[1].split("\\.")[0]; // remove the file extension

            return DataBufferUtils.join(part.content()).flatMap(dataBuffer -> {
                try {
                    InputStream inputStream = dataBuffer.asInputStream();
                    Reader in = new InputStreamReader(inputStream);
                    Iterable<CSVRecord> records;
                    try {
                        records = CSVFormat.DEFAULT
                                .withHeader("ContactID", "PhoneNumber", "TimeStamp", "CallDuration", "CompletionCode")
                                .withFirstRecordAsHeader()
                                .parse(in);
                    } catch (Exception e) {
                        Map<String, String> response = new HashMap<>();
                        response.put("message", "Failed to upload CSV file: " + e.getMessage());
                        return Mono.just(new ResponseEntity<>(response, HttpStatus.BAD_REQUEST));
                    }

                    callRecordService.deleteCallRecordsByCampaignId(campaignId);

                    CampaignCallRecord campaignCallRecord = new CampaignCallRecord();
                    campaignCallRecord.setCampaignName(campaignName);
                    campaignCallRecord.setCampaignId(campaignId);

                    for (CSVRecord record : records) {
                        String contactId = record.get("ContactID");
                        String phoneNumber = record.get("PhoneNumber");
                        String timeStamp = record.get("TimeStamp");
                        String callDuration = record.get("CallDuration");
                        String completionCode = record.get("CompletionCode");

                        //CallRecord callRecord = new CallRecord();
                        // Set other fields of campaignCallRecord as necessary
                        campaignCallRecord.setContactId(contactId);
                        campaignCallRecord.setPhoneNumber(phoneNumber);
                        campaignCallRecord.setTimestamp(timeStamp);
                        campaignCallRecord.setCallDuration(String.valueOf(Integer.parseInt(callDuration)));
                        campaignCallRecord.setCompletionCode(completionCode);
                        callRecordService.insertCallRecord(campaignCallRecord);
                        //campaignCallRecord.addCallRecord(callRecord);
                    }

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "CSV File processed successfully");
                    response.put("fileName", fileName);
                    //response.put("recordsProcessed", String.valueOf(campaignCallRecord.getCallRecords().size())); // assuming `records` is the list of processed records

                    return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
                } catch (Exception e) {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Failed to upload CSV file: " + e.getMessage());
                    return Mono.just(new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR));
                }
            });
        });
    }

    @DeleteMapping("/{campaignName}")
    @Operation(summary = "Delete all call records by campaign name",
            description = "This operation deletes all call records associated with the given campaign name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> deleteAllByCampaignName(@PathVariable String campaignName) {
        callRecordService.deleteCallRecordsByCampaignName(campaignName);
        return new ResponseEntity<>("All call records for campaign " + campaignName + " deleted successfully", HttpStatus.OK);
    }

    @Hidden
    @GetMapping("/{campaignName}")
    public ResponseEntity<CampaignContactListProbability> getCampaignsByCampaignName(@PathVariable String campaignName) {
        List<CampaignCallRecord> campaignCallRecords = callRecordService.getCallRecordsByCampaignName(campaignName);

        List<Map<String, Object>> output = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("Phone Number", "Phone number of customer");
        Map<String, Object> probability = new HashMap<>();
        probability.put("morning", "morning %");
        probability.put("afternoon", "afternoon %");
        probability.put("evening", "evening %");
        probability.put("hourly_probablity", "[array in hours]");
        record.put(" Probablity", Arrays.asList(probability));
        output.add(record);

        StringBuilder dynamicText = new StringBuilder();
        dynamicText.append("(Phone Number TimeStamps CallDuration ");
        for (CampaignCallRecord campaignCallRecord : campaignCallRecords) {
           // for (CallRecord callRecord : campaignCallRecord.getCallRecords()) {
                {
                    dynamicText.append("\"").append(campaignCallRecord.getPhoneNumber()).append("\" \"")
                            .append(campaignCallRecord.getTimestamp()).append("\" \"")
                            .append(campaignCallRecord.getCallDuration()).append("\" ");
                }
                dynamicText.append(")");
            //}
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {

            String jsonOutput = objectMapper.writeValueAsString(output);
            String jsonOutputWithoutQuotes = jsonOutput.replace("\"", "");
            String dynamicTextWithoutQuotes = dynamicText.toString().replace("\"", "");

            String text = "I am sending the historical (data) for 3 Contacts for 3 days. Please help us to generate the probability in % of calling the Contacts in the morning, afternoon , evening and in hourly form . Do not include any explanations, only provide a  RFC8259 compliant JSON response  following this format without deviation " + jsonOutputWithoutQuotes + dynamicTextWithoutQuotes;
            LOGGER.info(text);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error converting campaign data to JSON", e);
        }

        CampaignContactListProbability optionalCampaign = campaignContactListProbability.findByCampaignName(campaignName);

        return new ResponseEntity<>(optionalCampaign, HttpStatus.OK);
    }
}
