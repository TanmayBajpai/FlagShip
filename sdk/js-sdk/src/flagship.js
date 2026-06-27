import { evaluate } from './evaluator.js';
import { fetchConfig, trackSuccess } from './client.js';

export class FlagShip {
  constructor({ apiKey, baseUrl, pollInterval = 30000 }) {
    this.apiKey = apiKey;
    this.baseUrl = baseUrl;
    this.pollInterval = pollInterval;
    this.flags = new Map();
    this.poller = null;
  }

  async init() {
    const flags = await fetchConfig(this.baseUrl, this.apiKey);
    this.flags = new Map(flags.map(f => [f.flagName, f]));
    this.poller = setInterval(() => this.refresh(), this.pollInterval);
  }

  async refresh() {
    try {
      const flags = await fetchConfig(this.baseUrl, this.apiKey);
      this.flags = new Map(flags.map(f => [f.flagName, f]));
    } catch (err) {
      console.error('[FlagShip] Failed to refresh config:', err.message);
    }
  }

  async evaluate(flagName, userId, userAttributes = {}) {
    const flag = this.flags.get(flagName);
    if (!flag) return false;
    return evaluate(flag, userId, userAttributes);
  }

  getAllFlags() {
    return Array.from(this.flags.values());
  }

  async trackSuccess(userId) {
    try {
      await trackSuccess(this.baseUrl, this.apiKey, userId);
    } catch (err) {
      console.error('[FlagShip] Failed to track success:', err.message);
    }
  }

  destroy() {
    if (this.poller) clearInterval(this.poller);
  }
}