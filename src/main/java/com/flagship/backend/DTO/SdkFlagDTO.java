package com.flagship.backend.DTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SdkFlagDTO(
        UUID id,
        String flagName,
        boolean enabled,
        int rolloutPercent,
        Map<String, List<String>> targetingRules,
        String seed
) {
}
