import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  async rewrites() {
    return [
      {
        // 當前端發送 /api/xxx 時
        source: '/api/:path*',
        // 轉發到後端 Spring Boot 的位址
        destination: 'http://127.0.0.1:8080/api/:path*', 
      },
    ];
  },
};
export default nextConfig;