package com.flagship.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FlagShip SDK client. Fetches flag configuration from the server on startup,
 * caches it locally, and evaluates flags in-process with zero network latency.
 *
 * <pre>{@code
 * FlagShip flagship = new FlagShip("your-api-key", "https://your-server.com", 30_000);
 * flagship.init();
 *
 * boolean enabled = flagship.evaluate("dark-mode", "user-123", Map.of("plan", "pro"));
 * flagship.trackSuccess("user-123");
 * flagship.destroy();
 * }</pre>
 */
public class FlagShip {

    private final ConfigClient client;
    private final Evaluator evaluator;
    private final long pollInterval;
    private Map<String, FlagConfig> flags;
    private ScheduledExecutorService scheduler;

    /**
     * Creates a new FlagShip client.
     *
     * @param apiKey         API key from the FlagShip dashboard
     * @param baseUrl        base URL of your FlagShip server, without a trailing slash
     * @param pollIntervalMs milliseconds between background config re-fetches
     */
    public FlagShip(String apiKey, String baseUrl, long pollIntervalMs) {
        this.client = new ConfigClient(baseUrl, apiKey);
        this.evaluator = new Evaluator();
        this.pollInterval = pollIntervalMs;
        this.flags = new ConcurrentHashMap<>();
    }

    /**
     * Fetches flag configuration from the server and starts the background polling loop.
     * Must be called once before evaluating any flags.
     *
     * @throws Exception if the initial config fetch fails
     */
    public void init() throws Exception {
        refresh();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::safeRefresh, pollInterval, pollInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * Re-fetches flag configuration from the server. Called automatically on the poll interval;
     * call manually if you need an immediate update.
     *
     * @throws Exception if the request fails
     */
    public void refresh() throws Exception {
        List<FlagConfig> configs = client.fetchConfig();
        Map<String, FlagConfig> updated = new ConcurrentHashMap<>();
        for (FlagConfig config : configs) updated.put(config.getFlagName(), config);
        this.flags = updated;
    }

    private void safeRefresh() {
        try { refresh(); } catch (Exception e) {
            System.err.println("[FlagShip] Failed to refresh config: " + e.getMessage());
        }
    }

    /**
     * Evaluates a flag for a user entirely in-process.
     * Returns {@code false} if the flag does not exist, is disabled, the user fails
     * a targeting rule, or falls outside the rollout percentage.
     *
     * @param flagName       the flag's name as shown in the dashboard
     * @param userId         a stable identifier for the user (e.g. database ID)
     * @param userAttributes key-value attributes matched against targeting rules
     * @return {@code true} if the flag is enabled for this user
     */
    public boolean evaluate(String flagName, String userId, Map<String, String> userAttributes) {
        FlagConfig flag = flags.get(flagName);
        if (flag == null) return false;
        return evaluator.evaluate(flag, userId, userAttributes);
    }

    /**
     * Evaluates a flag for a user with no targeting attributes.
     *
     * @param flagName the flag's name as shown in the dashboard
     * @param userId   a stable identifier for the user
     * @return {@code true} if the flag is enabled for this user
     */
    public boolean evaluate(String flagName, String userId) {
        return evaluate(flagName, userId, Map.of());
    }

    /**
     * Records a conversion event for the given user. The server increments
     * {@code flagConversions} or {@code controlConversions} on every flag owned by
     * the account, based on which bucket the user falls into.
     *
     * @param userId a stable identifier for the user
     */
    public void trackSuccess(String userId) {
        try { client.trackSuccess(userId); } catch (Exception e) {
            System.err.println("[FlagShip] Failed to track success: " + e.getMessage());
        }
    }

    /**
     * Returns all flag config objects currently held in the local cache.
     *
     * @return list of all cached flags
     */
    public List<FlagConfig> getAllFlags() {
        return new ArrayList<>(flags.values());
    }

    /**
     * Returns the config for a single flag by name, or {@code null} if not found.
     *
     * @param flagName the flag's name
     * @return the flag config, or {@code null}
     */
    public FlagConfig getFlag(String flagName) {
        return flags.get(flagName);
    }

    /**
     * Returns {@code true} if the client has successfully loaded at least one flag
     * from the server.
     *
     * @return whether the client is ready to evaluate flags
     */
    public boolean isReady() {
        return !flags.isEmpty();
    }

    /**
     * Shuts down the background polling thread. Call this when your application
     * shuts down to avoid resource leaks.
     */
    public void destroy() {
        if (scheduler != null) scheduler.shutdown();
    }
}
