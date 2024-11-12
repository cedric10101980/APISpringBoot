package com.outbound.api.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "call_record")
public class CallRecord {
    private String phoneNumber;
    private String timestamp;
    private String callDuration;
    private String completionCode;
}
