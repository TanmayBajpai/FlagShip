package com.flagship.backend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class TargetingRuleDTO {
    private String key;
    private List<String> values;
    private boolean enabled = true;
}
