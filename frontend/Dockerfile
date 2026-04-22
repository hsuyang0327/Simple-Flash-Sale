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

# Backend URL used for server-side rewrites (baked into routes-manifest at build time)
# docker-compose.app.yml passes this as a build arg
ARG BACKEND_URL=http://flash-sale-backend:8080
ENV BACKEND_URL=$BACKEND_URL

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
