export interface FlagShipOptions {
  /** API key from the FlagShip dashboard. */
  apiKey: string;
  /** Base URL of your FlagShip server, without a trailing slash. */
  baseUrl: string;
  /** Milliseconds between background config re-fetches. Defaults to 30000. */
  pollInterval?: number;
}

/** Targeting rules as returned by GET /api/config — map of attribute key → allowed values. */
export interface TargetingRules {
  [key: string]: string[];
}

/** Raw flag config object cached by the SDK. */
export interface Flag {
  id: string;
  flagName: string;
  enabled: boolean;
  rolloutPercent: number;
  targetingRules: TargetingRules;
  seed: string;
}

/** Arbitrary user attributes matched against targeting rules. Values must be strings. */
export interface UserAttributes {
  [key: string]: string;
}

export declare class FlagShip {
  constructor(options: FlagShipOptions);

  /** Fetches flag config and starts the background polling loop. Call once before evaluating. */
  init(): Promise<void>;

  /** Re-fetches flag config from the server. Called automatically on the poll interval. */
  refresh(): Promise<void>;

  /**
   * Evaluates a flag for a user entirely in-process.
   * Returns false if the flag does not exist or the user does not qualify.
   */
  evaluate(flagName: string, userId: string, userAttributes?: UserAttributes): Promise<boolean>;

  /** Returns all flag config objects currently cached by the SDK. */
  getAllFlags(): Flag[];

  /**
   * Records a conversion event for the given user.
   * Increments flagConversions or controlConversions on every flag owned by the account.
   */
  trackSuccess(userId: string): Promise<void>;

  /** Clears the polling interval. Call when shutting down to avoid resource leaks. */
  destroy(): void;
}

export default FlagShip;
