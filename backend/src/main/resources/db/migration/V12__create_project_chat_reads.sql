CREATE TABLE project_chat_reads (
  id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  last_read_message_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT uk_project_chat_reads_project_user UNIQUE (project_id, user_id),
  CONSTRAINT fk_project_chat_reads_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
  CONSTRAINT fk_project_chat_reads_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
