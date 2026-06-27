function checkTargetingRules(targetingRules, userAttributes) {
  for (const [key, values] of Object.entries(targetingRules)) {
    const userValue = userAttributes[key];
    if (!userValue || !values.includes(userValue)) return false;
  }
  return true;
}

async function bucket(userId, flagName, seed) {
  const input = `${userId}|${flagName}|${seed}`;
  const encoded = new TextEncoder().encode(input);
  const hashBuffer = await globalThis.crypto.subtle.digest('SHA-256', encoded);
  const bytes = new Uint8Array(hashBuffer);
  let value = 0n;
  for (let i = 0; i < 8; i++) value = (value << 8n) | BigInt(bytes[i]);
  return Number(value % 100n);
}

export async function evaluate(flag, userId, userAttributes = {}) {
  if (!flag.enabled) return false;
  if (!checkTargetingRules(flag.targetingRules, userAttributes)) return false;
  return (await bucket(userId, flag.flagName, flag.seed)) < flag.rolloutPercent;
}