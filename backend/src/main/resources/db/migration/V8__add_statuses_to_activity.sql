ALTER TABLE activities
    ADD COLUMN old_status VARCHAR(30) NULL,
    ADD COLUMN new_status VARCHAR(30) NULL;
