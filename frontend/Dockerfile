# ────────────────────────────────────────────────
# Stage 1: Install dependencies
# ────────────────────────────────────────────────
FROM node:20-alpine AS deps

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci

# ────────────────────────────────────────────────
# Stage 2: Build Next.js app
# ────────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Backend URL used for server-side rewrites (resolved at runtime via env)
# Default falls back to localhost for local builds
ENV BACKEND_URL=http://backend:8080

RUN npm run build

# ────────────────────────────────────────────────
# Stage 3: Minimal production runner (standalone)
# ────────────────────────────────────────────────
FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production

# standalone output bundles only what's needed
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000

CMD ["node", "server.js"]
