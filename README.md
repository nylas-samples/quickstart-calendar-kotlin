# How to run

1. Compile the project

```bash
mvn package
```

2. Run the project

```bash
java -cp $KOTLIN/LIB/kotlin-stdlib.jar -jar target/quickstart-calendar-kotlin-1.0-SNAPSHOT-jar-with-dependencies.jar
```

3. In the Nylas dashboard, create a new application and set the hosted auth callback URL to `http://localhost:4567/oauth/exchange`

4. env variables

```env
NYLAS_CLIENT_ID=
NYLAS_API_KEY=
NYLAS_API_URI=https://api.us.nylas.com
EMAIL=<RECIPIENT_EMAIL_ADDRESS_HERE>
```

5. Open your browser and go to `http://localhost:4567/nylas/auth` and log in and end user account

6. After authenticating an end user account, you can visit the following URLs to get a feel for some of what you can do with the Nylas Email API.

```text
http://localhost:4567/nylas/primary-calendar
http://localhost:4567/nylas/list-events
http://localhost:4567/nylas/create-event

```
