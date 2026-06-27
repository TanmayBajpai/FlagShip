# flagship-java-sdk

Official Java SDK for [FlagShip](https://github.com/TanmayBajpai/Flagship) — a self-hosted feature flag platform.

Flags are fetched from your FlagShip server once on startup and cached locally. All evaluation happens in-process with zero network latency, using the same deterministic bucketing algorithm as the server.

## Requirements

- Java 17+

## Installation

**Maven:**
```xml
<dependency>
  <groupId>io.github.tanmaybajpai</groupId>
  <artifactId>flagship-java-sdk</artifactId>
  <version>1.0.0</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'io.github.tanmaybajpai:flagship-java-sdk:1.0.0'
```

## Quick Start

```java
import com.flagship.sdk.FlagShip;

FlagShip flagship = new FlagShip(
    "your-api-key",               // from the FlagShip dashboard
    "https://your-server.com",    // your FlagShip server URL
    30_000                        // poll interval in milliseconds
);

flagship.init();

boolean enabled = flagship.evaluate("dark-mode", "user-123", Map.of(
    "country", "US",
    "plan",    "pro"
));

if (enabled) {
    // show the new feature
}

// record a conversion event
flagship.trackSuccess("user-123");

// clean up on shutdown
flagship.destroy();
```

## API

### `new FlagShip(apiKey, baseUrl, pollIntervalMs)`

| Parameter | Type | Description |
|---|---|---|
| `apiKey` | `String` | API key from your FlagShip dashboard |
| `baseUrl` | `String` | Base URL of your FlagShip server (no trailing slash) |
| `pollIntervalMs` | `long` | Milliseconds between background config re-fetches |

---

### `init()` — `void`

Fetches flag configuration from the server and starts the background polling loop. Must be called once before evaluating any flags. Throws if the initial fetch fails.

---

### `evaluate(flagName, userId, userAttributes)` — `boolean`

Evaluates a flag for a user entirely in-process. Returns `false` if the flag does not exist, is disabled, the user fails a targeting rule, or falls outside the rollout percentage.

```java
// with attributes
boolean on = flagship.evaluate("checkout-v2", "user-456", Map.of("plan", "pro"));

// without attributes
boolean on = flagship.evaluate("maintenance-mode", "user-456");
```

---

### `trackSuccess(userId)` — `void`

Records a conversion event for the given user. The server increments `flagConversions` or `controlConversions` on every flag owned by the account, based on which bucket the user falls into. Call this at a meaningful conversion point — e.g. purchase completed, user signed up.

---

### `getAllFlags()` — `List<FlagConfig>`

Returns all flag config objects currently held in the local cache.

---

### `getFlag(flagName)` — `FlagConfig`

Returns the config for a single flag by name, or `null` if not found.

---

### `refresh()` — `void`

Manually re-fetches flag configuration from the server. The polling loop calls this automatically; call it on demand if you need an immediate update.

---

### `isReady()` — `boolean`

Returns `true` if the client has successfully loaded at least one flag from the server.

---

### `destroy()` — `void`

Shuts down the background polling thread. Call this when your application shuts down to avoid resource leaks.

```java
Runtime.getRuntime().addShutdownHook(new Thread(flagship::destroy));
```

---

## Targeting Rules

Flags can have targeting rules that restrict which users are eligible for bucketing. A user must satisfy **all rules** on a flag to be considered for rollout.

Each rule defines an attribute key and a list of allowed values. The value in the user's attribute map for that key must appear in the allowed list.

```java
// This flag requires plan=pro AND country in [US, CA]
flagship.evaluate("new-billing", "user-789", Map.of(
    "plan",    "pro",
    "country", "CA"
));
```

If the user's attribute map is missing a key that a rule requires, the user fails that rule and the flag evaluates to `false`.

---

## How Bucketing Works

FlagShip uses **sticky deterministic bucketing** — the same user always lands in the same bucket for a given flag.

1. Build input string: `userId + "|" + flagName + "|" + seed`
2. SHA-256 hash the string
3. Take the first 8 bytes as an unsigned 64-bit integer
4. `bucket = Long.remainderUnsigned(value, 100)`
5. Return `bucket < rolloutPercent`

The `seed` is a random value stored per-flag on the server. Resetting a flag's users (via the dashboard) regenerates the seed, reshuffling all bucket assignments without changing the rollout percentage.

---

## Publishing to Maven Central

To publish a release yourself, you need:

1. A [Sonatype Central Portal](https://central.sonatype.com/) account with the `io.github.tanmaybajpai` namespace verified
2. A GPG key configured locally (`gpg --gen-key`) and uploaded to a keyserver
3. Your Central Portal credentials in `~/.m2/settings.xml`:

```xml
<servers>
  <server>
    <id>central</id>
    <username>your-central-token-username</username>
    <password>your-central-token-password</password>
  </server>
</servers>
```

Then run:

```bash
mvn deploy -P release
```

---

## License

MIT
