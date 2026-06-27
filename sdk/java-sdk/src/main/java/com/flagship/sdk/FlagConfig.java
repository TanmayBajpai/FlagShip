package com.flagship.sdk;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Flag configuration as returned by {@code GET /api/config}.
 * Deserialized from JSON by Jackson; do not construct manually.
 */
public class FlagConfig {
    private UUID id;
    private String flagName;
    private boolean enabled;
    private int rolloutPercent;
    private String seed;
    private Map<String, List<String>> targetingRules;

    /** Unique flag ID. */
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    /** Flag name as shown in the dashboard. */
    public String getFlagName() { return flagName; }
    public void setFlagName(String flagName) { this.flagName = flagName; }

    /** Whether the flag is active. A disabled flag always evaluates to {@code false}. */
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /** Percentage of users (0–100) who should see the flag. */
    public int getRolloutPercent() { return rolloutPercent; }
    public void setRolloutPercent(int rolloutPercent) { this.rolloutPercent = rolloutPercent; }

    /** Per-flag random seed used for sticky bucketing. */
    public String getSeed() { return seed; }
    public void setSeed(String seed) { this.seed = seed; }

    /**
     * Targeting rules: map of attribute key → allowed values.
     * A user must match every rule to qualify for bucketing.
     */
    public Map<String, List<String>> getTargetingRules() { return targetingRules; }
    public void setTargetingRules(Map<String, List<String>> targetingRules) { this.targetingRules = targetingRules; }
}
