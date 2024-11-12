package com.outbound.api.domain;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Getter
@Setter
public class Probability {
    private String morning;
    private String afternoon;
    private String evening;
    private Map<String, String> hourlyProbability;
}
