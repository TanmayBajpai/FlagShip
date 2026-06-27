package com.flagship.backend.DTO;

import java.util.List;
import java.util.UUID;

public record FlagDTO(
        UUID id,
        String flagName,
        String description,
        boolean enabled,
        int rolloutPercent,
        List<TargetingRuleDTO> targetingRules,
        long flagConversions,
        long controlConversions
) {
}
