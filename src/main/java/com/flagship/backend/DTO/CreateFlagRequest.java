package com.flagship.backend.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateFlagRequest {
    @NotBlank
    private String flagName;
    private String description;
    private boolean enabled;
    private int rolloutPercent;
    private List<TargetingRuleDTO> targetingRules;
}
