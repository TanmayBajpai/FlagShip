package com.flagship.backend.Services;

import com.flagship.backend.DTO.TargetingRuleDTO;
import com.flagship.backend.DTO.UpdateFlagRequest;
import com.flagship.backend.Entities.FeatureFlag;
import com.flagship.backend.Entities.TargetingRule;
import com.flagship.backend.Exceptions.FlagNameTakenException;
import com.flagship.backend.Exceptions.InvalidFeatureFlag;
import com.flagship.backend.Exceptions.InvalidFlagIdException;
import com.flagship.backend.Respositories.FeatureFlagRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UpdateFlagService {

    private final FeatureFlagRepository featureFlagRepository;

    public UpdateFlagService(FeatureFlagRepository featureFlagRepository) {
        this.featureFlagRepository = featureFlagRepository;
    }

    @Transactional
    public void updateFlag(UpdateFlagRequest updateFlagRequest, UUID id) {
        Optional<FeatureFlag> optionalFeatureFlag = featureFlagRepository.findFeatureFlagById(id);

        if (optionalFeatureFlag.isEmpty()) throw new InvalidFlagIdException();
        FeatureFlag featureFlag = optionalFeatureFlag.get();

        if (updateFlagRequest.getFlagName() != null) {
            String newName = updateFlagRequest.getFlagName();
            if (!newName.equals(featureFlag.getFlagName()) &&
                    featureFlagRepository.existsByOwnerAndFlagName(featureFlag.getOwner(), newName))
                throw new FlagNameTakenException();
            featureFlag.setFlagName(newName);
        }

        if (updateFlagRequest.getDescription() != null) {
            featureFlag.setDescription(updateFlagRequest.getDescription());
        }

        if (updateFlagRequest.getEnabled() != null) {
            featureFlag.setEnabled(updateFlagRequest.getEnabled());
        }

        if (updateFlagRequest.getRolloutPercent() != null) {
            if (updateFlagRequest.getRolloutPercent() < 0 || updateFlagRequest.getRolloutPercent() > 100)
                throw new InvalidFeatureFlag();
            featureFlag.setRolloutPercent(updateFlagRequest.getRolloutPercent());
        }

        List<TargetingRuleDTO> ruleDtos = updateFlagRequest.getTargetingRules();
        if (ruleDtos != null) {
            featureFlag.getTargetingRules().clear();
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
