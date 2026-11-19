import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.midaad.app',
  appName: 'midaad-ai',
  webDir: 'public',
  server: {
    url: 'https://midaad-ai.vercel.app/',
    cleartext: true
  }
};

export default config;
