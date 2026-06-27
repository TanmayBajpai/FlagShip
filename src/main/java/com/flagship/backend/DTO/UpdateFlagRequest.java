package com.flagship.backend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class UpdateFlagRequest {
    private String flagName;
    private String description;
    private Boolean enabled;
    private Integer rolloutPercent;
    private List<TargetingRuleDTO> targetingRules;
}
