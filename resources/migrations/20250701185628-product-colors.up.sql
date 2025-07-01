CREATE TABLE product_colors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    hex VARCHAR(7),
    description TEXT
);
