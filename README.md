# 🚀 FlagShip - Feature Flag Management Platform

A full-stack feature flag management platform built on an **SDK-first architecture**. Developers register an account, create feature flags with targeting rules, then integrate the FlagShip JS SDK into their application. The SDK fetches all flag configuration once on startup, caches it locally, and evaluates flags in-process — no per-request network calls.

---

## ✨ Features
- 🔑 User authentication (register / login)
- 🏷️ Full CRUD for feature flags — name, description, rollout percentage
- 🎯 Generic attribute-based targeting rules (match any key-value pair, e.g. `plan`, `country`, `deviceType`)
- 🧮 Deterministic sticky bucketing — same user always gets the same result
- 🔄 Per-flag user reset — regenerates the bucketing seed to reshuffle assignments
- 📊 Conversion analytics — track flag vs. control conversions and measure lift
- 🔐 API key authentication for SDK and external integrations
- 📦 [Official JS SDK](./JS-SDK) for in-process, zero-latency flag evaluation

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite 7, React Router DOM 7 |
| Backend | Spring Boot 3.5.5, Java 17 |
| Database | MariaDB |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security (session-based dashboard + API key for SDK) |
| Build | Maven |

---

## 📦 Installation & Setup

### Prerequisites
- Java 17+
- Node.js 18+
- MariaDB running on `localhost:3306`

Create a database and user:
```sql
CREATE DATABASE featureflagsdb;
CREATE USER 'flagship-user'@'localhost' IDENTIFIED BY 'flagship-password';
GRANT ALL PRIVILEGES ON featureflagsdb.* TO 'flagship-user'@'localhost';
```

### Running the backend
```bash
./mvnw spring-boot:run
```

The backend serves the built React app as static files on port 8080. You do not need to run the frontend separately in production.

### Frontend dev server (optional — hot reload only)
```bash
cd Frontend
npm install
npm run dev   # starts on :5173, proxies API calls to :8080
```

### Building the frontend
```bash
cd Frontend && npm run build
# copy dist/* to src/main/resources/static/
```

---

## 📖 SDK Usage

The recommended integration is via the [JS SDK](./JS-SDK):

```bash
npm install flagship-sdk
```

```js
import FlagShip from 'flagship-sdk';

const flagship = new FlagShip({
  apiKey: 'your-api-key',             // from the FlagShip dashboard
  baseUrl: 'https://your-server.com',
});

await flagship.init();

const enabled = await flagship.evaluate('new-checkout', 'user-123', {
  plan: 'pro',
  country: 'US'
});

// record a conversion event
await flagship.trackSuccess('user-123');
```

See [`JS-SDK/README.md`](./JS-SDK/README.md) for the full SDK reference.

---

## 📡 REST API (direct integration)

All SDK endpoints require the `X-API-Key` header.

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/config` | Returns all flag configs for the account |
| `POST` | `/api/success?userId=X` | Records a conversion event for a user |

Dashboard endpoints (session cookie required) are documented in [`CLAUDE.md`](.claude/CLAUDE.md).

---

## 🧮 How Sticky Bucketing Works

1. Build input string: `userId + "|" + flagName + "|" + seed`
2. SHA-256 hash the string
3. Take the first 8 bytes as an unsigned 64-bit integer
4. `bucket = value % 100`
5. If `bucket < rolloutPercent` → flag is enabled for this user

The `seed` is a random value stored per-flag. Resetting a flag's users (via the dashboard) regenerates the seed, reshuffling all bucket assignments without changing the rollout percentage. The same algorithm runs in the JS SDK and in the server's legacy evaluate endpoint.

---

## 📊 Conversion Analytics

Calling `POST /api/success?userId=X` checks every flag owned by the account and increments `flagConversions` (user is in the flag bucket) or `controlConversions` (user is in control) on each one.

The dashboard displays **lift**, computed as:

```
flagNorm    = flagConversions    / rolloutPercent
controlNorm = controlConversions / (100 - rolloutPercent)
lift        = (flagNorm - controlNorm) / controlNorm × 100
```

---

## 📷 Screenshots

### Login Page
<img width="1920" height="978" alt="Screenshot_20250920_201250" src="https://github.com/user-attachments/assets/0b56c26f-700c-4fd0-bdca-087b1c0152a3" />

### Dashboard
<img width="1901" height="980" alt="Screenshot_20250920_201325" src="https://github.com/user-attachments/assets/e13016a1-0ff1-41f3-b565-385cf27d77e4" />

### Creating a Feature Flag
<img width="1901" height="977" alt="Screenshot_20250920_201347" src="https://github.com/user-attachments/assets/947d5533-af61-4dd7-b29d-4dfd33ed5df1" />
