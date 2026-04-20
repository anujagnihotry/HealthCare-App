# Deploy HealthCare API on Railway + GoDaddy (`healthsuite.in`)

This guide deploys the **NestJS backend** (`backend/`). The Android app is built separately; point it at your public API URL (see the end).

## 1. Prerequisites

- GitHub repo connected to Railway (push this project).
- GoDaddy domain: **healthsuite.in** (DNS access).

## 2. Create a Railway project

1. Open [railway.app](https://railway.app) → **New Project** → **Deploy from GitHub repo** → select this repository.
2. Railway may auto-create a service. **Delete** the empty/default service if it is not what you want, then:
   - **New** → **Database** → **Add PostgreSQL** (wait until it shows “Active”).
   - **New** → **Empty service** → connect the same repo, or use the auto-created service and configure it below.

## 3. Configure the API service

1. Open the **API** service (not Postgres) → **Settings**:
   - **Root Directory**: `backend`
   - **Builder**: **Dockerfile** (path `Dockerfile` inside `backend/` is used automatically when root is `backend`).
2. **Variables** (same service → **Variables** → **Add**):

   | Name | Value |
   |------|--------|
   | `NODE_ENV` | `production` |
   | `JWT_SECRET` | Long random string (e.g. `openssl rand -hex 32`) |
   | `DATABASE_URL` | Click **Reference** → Postgres → `DATABASE_URL` (Railway injects the connection string). |
   | `TYPEORM_SYNC` | `true` **only for the first successful deploy** to create tables, then set to `false` and redeploy. |
   | `CORS_ORIGIN` | Optional. Comma-separated browser origins, e.g. `https://healthsuite.in,https://www.healthsuite.in` |

   **Do not** commit real secrets; set them only in Railway.

3. **Deploy** → wait for build + deploy to go green. **Generate Domain** (Railway gives something like `xxx.up.railway.app`) and open:

   `https://<your-railway-domain>/api/docs`

   If Swagger loads, the API is up.

## 4. Custom domain (API on a subdomain)

Using **`api.healthsuite.in`** for the API keeps the apex domain free for a website or redirect.

### On Railway

1. API service → **Settings** → **Networking** → **Custom domain** → add **`api.healthsuite.in`**.
2. Railway shows a **target** (often a **CNAME** like `xxxx.up.railway.app` or similar). Copy it exactly.

### On GoDaddy

1. GoDaddy → **My Products** → **DNS** for **healthsuite.in**.
2. **Add** a record:
   - **Type**: `CNAME`
   - **Name**: `api`
   - **Value**: the hostname Railway gave (no `https://`, no path).
   - **TTL**: 600 or default.

3. Wait for DNS (often 5–30 minutes; can be up to 48 hours).

4. In Railway, wait until the custom domain shows **Verified** / **Active**.

5. Test: `https://api.healthsuite.in/api/docs`

### Apex domain `healthsuite.in` (optional)

- **Option A — Forwarding (simplest):** GoDaddy **Forwarding** → `https://healthsuite.in` → `https://www.healthsuite.in` (or your marketing URL).
- **Option B — Point apex to Railway:** Some providers support **ANAME/ALIAS** at apex; GoDaddy’s UI varies. If Railway gives you an **A record** IP for the apex, add an **A** `@` → that IP (check current Railway docs for apex).

For a mobile-only API, **`api.healthsuite.in` + CNAME** is usually enough.

## 5. CORS and mobile app

- **Android** `BuildConfig.BASE_URL` should be your public API base, e.g.  
  `https://api.healthsuite.in/api/`  
  (trailing slash as you already use in the project.)
- Set **`CORS_ORIGIN`** if a **web** front-end on another origin calls this API with credentials.

## 6. Uploads and persistence

Uploaded files are stored under **`uploads/`** in the container filesystem. On Railway, that is **ephemeral** unless you add a **volume** (Railway **Volumes** in service settings) mounted at `/app/uploads`, or move files to **S3 / Cloudflare R2** later.

## 7. First-time database schema

- With **`TYPEORM_SYNC=true`** once, TypeORM creates tables from entities (good for first boot).
- For long-term production, prefer **TypeORM migrations** and keep **`TYPEORM_SYNC=false`**.

## 8. Seed data (optional)

Run locally against the production DB **only if you understand the risk**, or run a one-off Railway **cron / shell** job:

```bash
cd backend && npm ci && npm run seed
```

(Ensure `DATABASE_URL` / DB env vars match production; the seed script wipes data — see `seed.ts`.)

## Troubleshooting

| Issue | Check |
|--------|--------|
| DB connection failed | `DATABASE_URL` referenced from Postgres service; `DB_SSL` not blocking — production + URL enables SSL in code. |
| 502 / crash on start | Railway **Deploy Logs**; confirm `JWT_SECRET` is set. |
| CORS in browser | Set `CORS_ORIGIN` to your exact web origin(s), including `https://`. |

---

Files added for deployment: `backend/Dockerfile`, `backend/.dockerignore`, `backend/.env.example`, and `backend/src/config/database.config.ts` updates for `DATABASE_URL` + SSL.
