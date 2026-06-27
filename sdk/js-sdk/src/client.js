export async function fetchConfig(baseUrl, apiKey) {
  const res = await fetch(`${baseUrl}/api/config`, {
    headers: { 'X-API-Key': apiKey }
  });
  if (!res.ok) throw new Error(`Failed to fetch config: ${res.status}`);
  return res.json();
}

export async function trackSuccess(baseUrl, apiKey, userId) {
  await fetch(`${baseUrl}/api/success?userId=${userId}`, {
    method: 'POST',
    headers: { 'X-API-Key': apiKey }
  });
}