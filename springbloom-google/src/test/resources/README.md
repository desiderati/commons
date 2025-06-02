## Google Calendar API Setup for Tests

To run the tests, follow the steps below to configure the Google Calendar API:

1. Go to the [Google Cloud Console – OAuth Clients](https://console.cloud.google.com/auth/clients).
2. Generate a new **OAuth client secret** for your project.
3. Download the resulting JSON credentials file.
4. Rename the file to: `google-calendar-api-secret.json`.
5. Place the file in the root directory of your project or wherever your application expects it.

> ⚠️ Make sure the file is not committed to version control by adding it to your `.gitignore`.
