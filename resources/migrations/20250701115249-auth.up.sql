CREATE TABLE auth (
  user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  password_hash TEXT NOT NULL,
  verification_token TEXT,
  password_reset_token TEXT,
  password_reset_token_expires_at TIMESTAMP
);