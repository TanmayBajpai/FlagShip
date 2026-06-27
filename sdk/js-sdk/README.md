# @tanmaybajpai/flagship-js-sdk

Official JavaScript SDK for [FlagShip](https://github.com/TanmayBajpai/Flagship) — a self-hosted feature flag platform.

Flags are fetched from your FlagShip server once on startup and cached locally. All evaluation happens in-process with zero network latency, using the same deterministic bucketing algorithm as the server.

## Installation

```bash
npm install @tanmaybajpai/flagship-js-sdk
```

## Quick Start

```js
import FlagShip from '@tanmaybajpai/flagship-js-sdk';

const flagship = new FlagShip({
  apiKey: 'your-api-key',               // from the FlagShip dashboard
  baseUrl: 'https://your-server.com',   // your FlagShip server URL
  pollInterval: 30000                   // re-fetch config every 30s (optional)
});

await flagship.init();

const enabled = await flagship.evaluate('dark-mode', 'user-123', {
  country: 'US',
  plan: 'pro'
});

if (enabled) {
  // show the new feature
}

// record a conversion event
await flagship.trackSuccess('user-123');

// clean up when shutting down
flagship.destroy();
```

## API

### `new FlagShip(options)`

| Option | Type | Required | Default | Description |
|---|---|---|---|---|
| `apiKey` | `string` | Yes | — | API key from your FlagShip dashboard |
| `baseUrl` | `string` | Yes | — | Base URL of your FlagShip server (no trailing slash) |
| `pollInterval` | `number` | No | `30000` | Milliseconds between background config re-fetches |

---

### `flagship.init()` → `Promise<void>`

Fetches flag configuration from the server and starts the background polling loop. Must be called once before evaluating any flags.

```js
await flagship.init();
```

---

### `flagship.evaluate(flagName, userId, userAttributes?)` → `Promise<boolean>`

Evaluates a flag for a given user entirely in-process. Returns `false` if the flag does not exist, is disabled, the user fails a targeting rule, or falls outside the rollout percentage.

```js
const enabled = await flagship.evaluate('checkout-v2', 'user-456', {
  plan: 'pro',
  country: 'US'
});
```

| Parameter | Type | Description |
|---|---|---|
| `flagName` | `string` | The flag's name as shown in the dashboard |
| `userId` | `string` | A stable identifier for the user (e.g. database ID) |
| `userAttributes` | `object` | Key-value string pairs matched against targeting rules |

---

### `flagship.trackSuccess(userId)` → `Promise<void>`

Records a conversion event for a user. The server increments `flagConversions` or `controlConversions` on every flag owned by the account, based on which bucket the user falls into. This powers the lift analytics shown in the dashboard.

Call this at a meaningful conversion point — for example when a user completes a purchase, signs up, or activates a core feature.

```js
await flagship.trackSuccess('user-456');
```

---

### `flagship.getAllFlags()` → `Flag[]`

Returns all flag config objects currently held in the local cache. Useful for debugging or building a flag explorer.

```js
const flags = flagship.getAllFlags();
console.log(flags.map(f => f.flagName));
```

---

### `flagship.refresh()` → `Promise<void>`

Manually re-fetches flag configuration from the server. The polling loop calls this automatically on the configured interval, but you can trigger it on demand if needed.

---

### `flagship.destroy()`

Clears the background polling interval. Call this when shutting down your application or in test teardowns.

```js
process.on('SIGTERM', () => flagship.destroy());
```

---

## Targeting Rules

Flags can have targeting rules that restrict which users are eligible for bucketing. A user must satisfy **all enabled rules** on a flag to be considered for rollout.

Each rule defines an attribute `key` and a list of `allowedValues`. The value in the user's attributes for that key must be present in the allowed list.

```js
// This flag has two rules: plan must be 'pro' AND country must be 'US' or 'CA'
const enabled = await flagship.evaluate('new-billing', 'user-789', {
  plan: 'pro',
  country: 'CA'
});
```

If the user's attributes are missing a key that a rule checks, the user fails that rule and the flag returns `false` regardless of rollout percentage.

---

## How Bucketing Works

FlagShip uses **sticky deterministic bucketing** — the same user always lands in the same bucket for a given flag, so evaluation is consistent across calls and deployments.

1. Build the input string: `userId + "|" + flagName + "|" + seed`
2. SHA-256 hash the string
3. Take the first 8 bytes as an unsigned 64-bit integer
4. `bucket = value % 100`
5. Return `bucket < rolloutPercent`

The `seed` is a random value stored per-flag on the server. Resetting a flag's users (via the dashboard) regenerates the seed, reshuffling all bucket assignments without changing the rollout percentage.

---

## Requirements

- **Node.js 18+** or a modern browser with [Web Crypto API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Crypto_API) support
- **ES Modules** — this package is published as ESM (`"type": "module"`)

If you need CommonJS compatibility, use a dynamic import:

```js
const { default: FlagShip } = await import('@tanmaybajpai/flagship-js-sdk');
```

---

## License

MIT
