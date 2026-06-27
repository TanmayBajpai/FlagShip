package com.flagship.backend.Services;

import com.flagship.backend.Entities.FeatureFlag;
import com.flagship.backend.Entities.TargetingRule;
import com.flagship.backend.Respositories.FeatureFlagRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EvaluateUserService {

    private final FeatureFlagRepository featureFlagRepository;

    public EvaluateUserService(FeatureFlagRepository featureFlagRepository) {
        this.featureFlagRepository = featureFlagRepository;
    }

    public boolean evaluate(UUID id, String userID, Map<String, String> userAttributes) {
        Optional<FeatureFlag> optionalFeatureFlag = featureFlagRepository.findFeatureFlagById(id);
        if (optionalFeatureFlag.isEmpty()) return false;

        FeatureFlag featureFlag = optionalFeatureFlag.get();
        if (!featureFlag.isEnabled()) return false;

        List<TargetingRule> rules = featureFlag.getTargetingRules();
        if (userAttributes != null && !rules.isEmpty()) {
            for (TargetingRule rule : rules) {
                if (!rule.isEnabled()) continue;
                String userValue = userAttributes.get(rule.getRuleKey());
                if (userValue == null || !rule.getAllowedValues().contains(userValue)) return false;
            }
        }

        return bucket(userID, featureFlag);
    }

    public boolean evaluateBucketOnly(FeatureFlag featureFlag, String userID) {
        if (!featureFlag.isEnabled()) return false;
        return bucket(userID, featureFlag);
    }

    private boolean bucket(String userID, FeatureFlag featureFlag) {
        String input = userID + "|" + featureFlag.getFlagName() + "|" + featureFlag.getSeed();
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] bytes = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (bytes[i] & 0xffL);
        }
        int b = (int) (Long.remainderUnsigned(value, 100));
        return b < featureFlag.getRolloutPercent();
    }
}
