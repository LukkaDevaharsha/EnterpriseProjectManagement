# Gmail verification setup

New employee registrations send a 6-digit verification code through Gmail SMTP. Configure these variables for the backend process:

- `GMAIL_USERNAME`: the Gmail address used to send verification messages.
- `GMAIL_APP_PASSWORD`: a Google App Password created for that account. Do not use the normal Gmail password.

For local development, copy `backend/.env.example` to `backend/.env`, replace both example values, and start the backend with `backend` as the working directory. Spring loads that local properties file automatically. The `.env` file is ignored by Git. Do not commit your Gmail App Password.

The backend uses `smtp.gmail.com` on port `587` with STARTTLS. Keep both values in the runtime environment or a secrets manager; do not commit them. Restart the backend after configuring them.

Codes expire after 15 minutes, allow at most five attempts, and can be resent once per minute. Existing accounts are marked verified during the database migration. New accounts cannot sign in until verification succeeds.

Registration passwords must be 10 to 72 characters and contain uppercase and lowercase letters, a number, and a symbol. Whitespace is not accepted.
