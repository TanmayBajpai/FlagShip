import FlagShip from './index.js';

const flagship = new FlagShip({
  apiKey: 'your-api-key-here',
  baseUrl: 'http://localhost:8080',
  pollInterval: 30000
});

await flagship.init();
console.log('Config loaded:', flagship.flags);

const result = await flagship.evaluate('dark-mode', 'user-123', { country: 'IN', plan: 'pro' });
console.log('Evaluate result:', result);

await flagship.trackSuccess('user-123');
console.log('Success tracked');

flagship.destroy();