CREATE TABLE product_photos (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    url VARCHAR(512) NOT NULL,
    position INT,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
