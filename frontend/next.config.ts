import type { NextConfig } from "next";

const backendUrl = process.env.BACKEND_URL || 'http://127.0.0.1:8080';

const nextConfig: NextConfig = {
  // Required for Docker/K8s: produces a self-contained build under .next/standalone
  output: 'standalone',
  async rewrites() {
    return [
      {
        // 當前端發送 /api/xxx 時
        source: '/api/:path*',
        // 容器環境使用 BACKEND_URL 環境變數；本地開發預設 127.0.0.1:8080
        destination: `${backendUrl}/api/:path*`,
      },
    ];
  },
};
export default nextConfig;