CREATE TABLE blacklisted_tokens (
  token TEXT PRIMARY KEY,
  created_at TIMESTAMP DEFAULT now()
);