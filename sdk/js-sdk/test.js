import FlagShip from './src/index.js';

const flagship = new FlagShip({
  apiKey: 'your-api-key-here',
  baseUrl: 'http://localhost:8080',
  pollInterval: 30000
});

await flagship.init();
console.log('Config loaded:', flagship.flags);

// evaluate a flag
const result = await flagship.evaluate('new-dashboard', 'user-123', { country: 'IND', plan: 'pro' });
console.log('Evaluate result:', result);

// track a success
await flagship.trackSuccess('user-123');
console.log('Success tracked');

console.log('All flags:', flagship.getAllFlags());

flagship.destroy();