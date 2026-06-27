package com.flagship.backend.Services;

import com.flagship.backend.DTO.CreateFlagRequest;
import com.flagship.backend.DTO.TargetingRuleDTO;
import com.flagship.backend.Entities.FeatureFlag;
import com.flagship.backend.Entities.TargetingRule;
import com.flagship.backend.Exceptions.FlagNameTakenException;
import com.flagship.backend.Exceptions.InvalidFeatureFlag;
import com.flagship.backend.Respositories.FeatureFlagRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class CreateFlagService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final FeatureFlagRepository featureFlagRepository;

    public CreateFlagService(FeatureFlagRepository featureFlagRepository) {
        this.featureFlagRepository = featureFlagRepository;
    }

    public void createFlag(CreateFlagRequest createFlagRequest, String username) {
        int rolloutPercent = createFlagRequest.getRolloutPercent();
        if (rolloutPercent > 100 || rolloutPercent < 0) throw new InvalidFeatureFlag();
        if (featureFlagRepository.existsByOwnerAndFlagName(username, createFlagRequest.getFlagName()))
            throw new FlagNameTakenException();

        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        String seed = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        FeatureFlag featureFlag = new FeatureFlag();
        featureFlag.setFlagName(createFlagRequest.getFlagName());
        featureFlag.setDescription(createFlagRequest.getDescription());
        featureFlag.setEnabled(createFlagRequest.isEnabled());
        featureFlag.setRolloutPercent(rolloutPercent);
        featureFlag.setOwner(username);
        featureFlag.setSeed(seed);
        featureFlag.setFlagConversions(0);
        featureFlag.setControlConversions(0);

        List<TargetingRuleDTO> ruleDtos = createFlagRequest.getTargetingRules();
        if (ruleDtos != null) {
            for (TargetingRuleDTO dto : ruleDtos) {
                if (dto.getKey() == null || dto.getValues() == null || dto.getValues().isEmpty()) continue;
                TargetingRule rule = new TargetingRule();
                rule.setRuleKey(dto.getKey());
                rule.setEnabled(dto.isEnabled());
                rule.getAllowedValues().addAll(dto.getValues());
                rule.setFlag(featureFlag);
                featureFlag.getTargetingRules().add(rule);
            }
        }

        featureFlagRepository.save(featureFlag);
    }
}
