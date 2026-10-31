ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN verification_code_hash VARCHAR(100) NULL,
    ADD COLUMN verification_expires_at TIMESTAMP NULL,
    ADD COLUMN verification_sent_at TIMESTAMP NULL,
    ADD COLUMN verification_attempts INT NOT NULL DEFAULT 0;
