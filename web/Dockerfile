# Build (NEXT_PUBLIC_* embutidas no bundle; sobrescreva com --build-arg no deploy)
FROM node:22-alpine AS builder

WORKDIR /app

ARG NEXT_PUBLIC_CATALOG_API_URL=http://localhost:8080
ARG NEXT_PUBLIC_INVENTORY_API_URL=http://localhost:8081
ARG NEXT_PUBLIC_RENTAL_API_URL=http://localhost:8082
ARG NEXT_PUBLIC_SEARCH_API_URL=http://localhost:8083

ENV NEXT_PUBLIC_CATALOG_API_URL=$NEXT_PUBLIC_CATALOG_API_URL
ENV NEXT_PUBLIC_INVENTORY_API_URL=$NEXT_PUBLIC_INVENTORY_API_URL
ENV NEXT_PUBLIC_RENTAL_API_URL=$NEXT_PUBLIC_RENTAL_API_URL
ENV NEXT_PUBLIC_SEARCH_API_URL=$NEXT_PUBLIC_SEARCH_API_URL

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
RUN npm run build

# Run
FROM node:22-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs
EXPOSE 3000
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"
CMD ["node", "server.js"]
