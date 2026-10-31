ALTER TABLE users
    ADD COLUMN employee_id VARCHAR(50) NULL,
    ADD COLUMN department VARCHAR(100) NULL,
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE users
SET employee_id = CONCAT('EMP-', id)
WHERE employee_id IS NULL;

UPDATE users
SET department = 'General'
WHERE department IS NULL;

ALTER TABLE users
    MODIFY COLUMN employee_id VARCHAR(50) NOT NULL,
    MODIFY COLUMN department VARCHAR(100) NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT uk_users_employee_id UNIQUE (employee_id);