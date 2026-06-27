package com.flagship.sdk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorTest {

    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new Evaluator();
    }

    private FlagConfig flag(String name, String seed, boolean enabled, int rolloutPercent,
                            Map<String, List<String>> rules) {
        FlagConfig f = new FlagConfig();
        f.setFlagName(name);
        f.setSeed(seed);
        f.setEnabled(enabled);
        f.setRolloutPercent(rolloutPercent);
        f.setTargetingRules(rules != null ? rules : new HashMap<>());
        return f;
    }

    // ── Enabled / rollout ────────────────────────────────────────────────────

    @Test
    void disabledFlagReturnsFalse() {
        FlagConfig f = flag("f", "seed", false, 100, null);
        assertFalse(evaluator.evaluate(f, "user-1", Map.of()));
    }

    @Test
    void zeroRolloutAlwaysFalse() {
        FlagConfig f = flag("f", "seed", true, 0, null);
        assertFalse(evaluator.evaluate(f, "user-1", Map.of()));
        assertFalse(evaluator.evaluate(f, "user-2", Map.of()));
        assertFalse(evaluator.evaluate(f, "user-3", Map.of()));
    }

    @Test
    void fullRolloutNoRulesAlwaysTrue() {
        FlagConfig f = flag("f", "seed", true, 100, null);
        assertTrue(evaluator.evaluate(f, "user-1", Map.of()));
        assertTrue(evaluator.evaluate(f, "user-2", Map.of()));
        assertTrue(evaluator.evaluate(f, "user-3", Map.of()));
    }

    // ── Determinism ──────────────────────────────────────────────────────────

    @Test
    void sameUserAlwaysGetsSameResult() {
        FlagConfig f = flag("f", "seed123", true, 50, null);
        boolean first = evaluator.evaluate(f, "user-abc", Map.of());
        for (int i = 0; i < 20; i++) {
            assertEquals(first, evaluator.evaluate(f, "user-abc", Map.of()));
        }
    }

    @Test
    void differentSeedReshufflesBuckets() {
        FlagConfig a = flag("f", "seed-a", true, 50, null);
        FlagConfig b = flag("f", "seed-b", true, 50, null);
        boolean anyDifference = false;
        for (int i = 0; i < 50; i++) {
            if (evaluator.evaluate(a, "user-" + i, Map.of()) !=
                evaluator.evaluate(b, "user-" + i, Map.of())) {
                anyDifference = true;
                break;
            }
        }
        assertTrue(anyDifference, "Different seeds must produce different bucketing");
    }

    // ── Rollout distribution ─────────────────────────────────────────────────

    @Test
    void rolloutDistributionIsRoughlyCorrect() {
        FlagConfig f = flag("f", "seed", true, 50, null);
        int trueCount = 0;
        for (int i = 0; i < 200; i++) {
            if (evaluator.evaluate(f, "user-" + i, Map.of())) trueCount++;
        }
        // 50% rollout over 200 users — expect 70–130 (3-sigma tolerance)
        assertTrue(trueCount > 70 && trueCount < 130,
                "Expected ~50% enabled, got " + trueCount + "/200");
    }

    // ── Targeting rules ──────────────────────────────────────────────────────

    @Test
    void userPassingRuleIsEligible() {
        FlagConfig f = flag("f", "seed", true, 100,
                Map.of("country", List.of("US", "CA")));
        assertTrue(evaluator.evaluate(f, "u", Map.of("country", "US")));
        assertTrue(evaluator.evaluate(f, "u", Map.of("country", "CA")));
    }

    @Test
    void userFailingRuleIsNotEligible() {
        FlagConfig f = flag("f", "seed", true, 100,
                Map.of("country", List.of("US", "CA")));
        assertFalse(evaluator.evaluate(f, "u", Map.of("country", "IN")));
    }

    @Test
    void missingAttributeFailsRule() {
        FlagConfig f = flag("f", "seed", true, 100,
                Map.of("country", List.of("US")));
        assertFalse(evaluator.evaluate(f, "u", Map.of()));
        assertFalse(evaluator.evaluate(f, "u", Map.of("plan", "pro")));
    }

    @Test
    void allRulesMustPass() {
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("country", List.of("US"));
        rules.put("plan", List.of("pro", "enterprise"));
        FlagConfig f = flag("f", "seed", true, 100, rules);

        assertTrue(evaluator.evaluate(f, "u", Map.of("country", "US", "plan", "pro")));
        assertFalse(evaluator.evaluate(f, "u", Map.of("country", "US", "plan", "free")));
        assertFalse(evaluator.evaluate(f, "u", Map.of("country", "IN", "plan", "pro")));
    }

    // ── Bucketing algorithm correctness ──────────────────────────────────────

    /**
     * Independently computes the expected bucket using the same algorithm as the server
     * and verifies the Evaluator produces a matching result at the rollout boundary.
     */
    @Test
    void bucketingMatchesServerAlgorithm() throws Exception {
        String userId = "user-1";
        String flagName = "test-flag";
        String seed = "abc";

        String input = userId + "|" + flagName + "|" + seed;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        long value = 0;
        for (int i = 0; i < 8; i++) value = (value << 8) | (hash[i] & 0xFFL);
        int expectedBucket = (int) Long.remainderUnsigned(value, 100);

        FlagConfig inFlag = flag(flagName, seed, true, expectedBucket + 1, null);
        assertTrue(evaluator.evaluate(inFlag, userId, Map.of()),
                "User should be IN flag when rolloutPercent > their bucket");

        FlagConfig outFlag = flag(flagName, seed, true, expectedBucket, null);
        assertFalse(evaluator.evaluate(outFlag, userId, Map.of()),
                "User should be OUT of flag when rolloutPercent == their bucket");
    }

    /**
     * With 100% rollout the bucket must always be 0–99.
     * Catches any negative-value bug from signed arithmetic.
     */
    @Test
    void bucketIsAlwaysInRange() {
        FlagConfig f = flag("f", "seed", true, 100, null);
        for (int i = 0; i < 500; i++) {
            assertTrue(evaluator.evaluate(f, "user-" + i, Map.of()),
                    "Bucket out of range for user-" + i);
        }
    }
}
