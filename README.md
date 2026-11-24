# Clipora

A simple Java + PostgreSQL clipboard app. Paste text, get a six-digit code, and retrieve the text on another device within 15 minutes.

The frontend uses three files in `src/main/webapp/`: `index.html` for the page, `style.css` for styling, and `script.js` for behavior. There is no frontend framework or npm setup.

## How the code works

1. The HTML page sends text to `POST api/clipboards`.
2. `ClipboardServlet.java` checks the text and calls `ClipboardDAO.java`.
3. The DAO saves it in PostgreSQL with a random code and expiry time.
4. `GET api/clipboards?code=123456` retrieves unexpired text.

The app does not enforce a character limit. Practical limits still depend on server resources and database capacity. Anyone with a code can retrieve its text; this is a learning app, not a secret-sharing service. Expired text cannot be retrieved, and expired rows are deleted on the next save. Text already copied or displayed on a device cannot be revoked. The app does not include authentication or rate limiting.

## Run locally

You need Java 21 or later, Maven, PostgreSQL, and Apache Tomcat 10.1. Tomcat 10.1 supports the Servlet 6.0 API used here: https://tomcat.apache.org/whichversion.html

1. Create a PostgreSQL database named `clipora`, or use your existing database.
2. Open that database in pgAdmin (or your provider's SQL editor) and run `schema.sql`. It does not drop an existing table. If you already have a `clipboards` table, check that its columns match the script and that `code` has a unique constraint.
3. Keep your existing `.env` file. For a new setup, copy `.env.example` to `.env` and fill in your database details. `DB_URL` must start with `jdbc:postgresql://`, not `postgres://`.
4. Build from this project folder:

   ```bash
   mvn clean package
   ```

5. Copy `target/clipora.war` into your Tomcat installation's `webapps` folder.
6. Set `DB_URL`, `DB_USER` and `DB_PASSWORD` in the environment of the Tomcat process, then start Tomcat. Alternatively, place `.env` in Tomcat's working directory. A `.env` in this project is only read when Tomcat starts with this project as its working directory. Restart Tomcat after changing settings.
7. Open `http://localhost:8080/clipora/`. Do not open the HTML file directly, because it needs the Java server.

Keep `.env` private. Database settings are never needed in the HTML file.

### Run with Docker instead of installing Tomcat

After setting up the database and `.env`:

```bash
docker build -t clipora .
docker run --rm --name clipora -p 8080:8080 --env-file .env clipora
```

Open `http://localhost:8080/`. Docker deploys the same WAR at the root path. A database URL containing `localhost` points inside the container; use a database hostname reachable from Docker. A hosted PostgreSQL database works with both local and hosted runs.

## Deploy on Render

The included Dockerfile builds Java 21 code and runs it on Tomcat 10.1. Choose a **Web Service** for this app.

1. Create a GitHub repository and upload this project's source files, including `pom.xml`, `src`, `Dockerfile`, `.dockerignore` and `schema.sql`. Exclude `.env` and `target`. The included `.gitignore` excludes these when using Git.
2. Use an existing PostgreSQL database or create one with your provider. If using Render PostgreSQL, choose the same region as your web service. Check the provider's current pricing and retention before selecting a plan.
3. Run `schema.sql` in the chosen database using its SQL editor, pgAdmin, or `psql`. Use the provider's external connection details from your computer if its internal hostname is only reachable from deployed services.
4. In Render, select **New → Web Service**, connect the GitHub repository, and choose the **Docker** runtime. Leave Docker Command empty so the Dockerfile starts Tomcat.
5. Add these environment variables in the service settings:

   | Variable | Value |
   | --- | --- |
   | `DB_URL` | `jdbc:postgresql://HOST:5432/DATABASE?sslmode=require&connectTimeout=10&socketTimeout=15` |
   | `DB_USER` | Your database username |
   | `DB_PASSWORD` | Your database password |
   | `PORT` | `8080` |

   Replace `HOST` and `DATABASE` with your provider's values; use the provider's port if it differs. Use its required SSL settings. Keep the username and password in their separate variables rather than pasting a full `postgresql://user:password@host/database` URL into `DB_URL`.

6. Set the health check path to `/api/health`. This checks that Tomcat and the application are running; it does not check the database.
7. Deploy and wait for the build and startup to finish. Open the HTTPS URL Render provides.
8. Share a short message and retrieve it from another browser or device. The copy buttons work on HTTPS or localhost.

Render documentation: [Docker deployment](https://render.com/docs/docker), [web services and ports](https://render.com/docs/web-services), [environment variables](https://render.com/docs/configure-environment-variables).

## Quick checks after starting

- Save text and retrieve it using the generated code. Spaces, line breaks and Unicode should stay unchanged.
- Copy the code and the retrieved text.
- Submit a nonexistent six-digit code: the app should report that it is missing or expired.
- After 15 minutes, retrieving a code should report that it is missing or expired.
- `GET /api/health` should return a running message (add `/clipora` to the path for a local WAR named `clipora.war`).

## Troubleshooting

- **Could not save/retrieve text:** Check Tomcat or Render logs. Verify database credentials, database access rules and that you ran `schema.sql` in the correct database.
- **404:** Use `/clipora/` for the local `clipora.war` deployment, or `/` for the Docker deployment.
- **Build cannot download dependencies:** Maven needs internet access on its first build.
- **Copy unavailable:** Use HTTPS or localhost, or select and copy the text manually.
- **Unsupported Java class version:** Run Tomcat with Java 21 or later.
