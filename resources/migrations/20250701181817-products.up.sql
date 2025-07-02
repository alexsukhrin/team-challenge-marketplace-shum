CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(12,2),
    user_id UUID NOT NULL REFERENCES users(id),
    quantity INT NOT NULL DEFAULT 1
);
