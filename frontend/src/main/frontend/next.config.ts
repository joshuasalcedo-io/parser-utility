import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
    output: 'export',
    basePath: '',
    // Ensure assets work correctly when served from Spring Boot
    assetPrefix: '',
};

export default nextConfig;
