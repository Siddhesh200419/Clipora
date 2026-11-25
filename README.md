# Clipora

Clipora lets you share text between devices using a six-digit code. Text expires after 15 minutes.

**Live app:** [https://clipora-wvwh.onrender.com/](https://clipora-wvwh.onrender.com/)

## What it does

Clipora is a small web app for temporarily moving text between devices. For example, you can paste a note on one device, then open it on another using a six-digit code. No account is needed. The text expires after 15 minutes.

## How it works

1. Enter text in the app and submit it.
2. Clipora stores the text in PostgreSQL and returns a six-digit code.
3. Open Clipora on another device and enter the code to retrieve the text, as long as it has not expired.

The browser interface uses HTML, CSS, and JavaScript. A Java servlet handles requests, and PostgreSQL stores the text and expiry time. The app uses Java 21, Maven, and Tomcat 10.1, and can be run with Docker.

## Important

Anyone with a code can read its text. The app has no authentication or rate limiting, so do not use it to share sensitive information.

## Run with Docker

You need Docker and a PostgreSQL database.

1. Run `schema.sql` on your database.
2. Create a `.env` file from `.env.example` and enter your database details:

   ```env
   DB_URL=jdbc:postgresql://HOST:5432/DATABASE?sslmode=require
   DB_USER=your_database_user
   DB_PASSWORD=your_database_password
   ```

3. Build and start the app:

   ```bash
   docker build -t clipora .
   docker run --rm --name clipora -p 8080:8080 --env-file .env clipora
   ```

4. Open [http://localhost:8080/](http://localhost:8080/).

Use a database hostname reachable from Docker. `localhost` in `DB_URL` refers to the app container, not your computer. Keep `.env` private; do not commit it.

## Deploy on Render

Connect the GitHub repository and create a Web Service using the Docker runtime. Render builds the app from the included `Dockerfile`.

Add these environment variables in the service settings:

- `DB_URL`: your PostgreSQL JDBC URL
- `DB_USER`: your database username
- `DB_PASSWORD`: your database password
- `PORT`: `8080`

Run `schema.sql` on the database before using the app. Set the health check path to `/api/health`. After deployment, open the service URL provided by Render.
