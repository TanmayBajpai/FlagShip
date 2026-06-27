package com.flagship.sdk;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public class Evaluator {

    public boolean evaluate(FlagConfig flag, String userId, Map<String, String> userAttributes) {
        if (!flag.isEnabled()) return false;
        if (!checkTargetingRules(flag.getTargetingRules(), userAttributes)) return false;
        return bucket(userId, flag.getFlagName(), flag.getSeed()) < flag.getRolloutPercent();
    }

    private boolean checkTargetingRules(Map<String, List<String>> rules, Map<String, String> userAttributes) {
        for (Map.Entry<String, List<String>> entry : rules.entrySet()) {
            String userValue = userAttributes.get(entry.getKey());
            if (userValue == null || !entry.getValue().contains(userValue)) return false;
        }
        return true;
    }

    private int bucket(String userId, String flagName, String seed) {
        try {
            String input = userId + "|" + flagName + "|" + seed;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            long value = 0;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | (hash[i] & 0xFFL);
            }
            return (int) Long.remainderUnsigned(value, 100);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
