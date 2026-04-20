# HealthCare API — production image for Railway / Docker
FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
RUN npm run build

FROM node:20-alpine AS production

WORKDIR /app
ENV NODE_ENV=production

COPY package.json package-lock.json* ./
RUN npm ci --omit=dev && npm cache clean --force

COPY --from=builder /app/dist ./dist

# Persisted uploads (mount a volume on Railway or use object storage later)
RUN mkdir -p uploads

EXPOSE 3000
ENV PORT=3000

CMD ["node", "dist/main.js"]
