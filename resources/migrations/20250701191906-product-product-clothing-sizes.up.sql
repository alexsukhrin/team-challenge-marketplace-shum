CREATE TABLE product_product_clothing_sizes (
    product_id INT NOT NULL,
    clothing_size_id INT NOT NULL,
    PRIMARY KEY (product_id, clothing_size_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (clothing_size_id) REFERENCES product_clothing_sizes(id) ON DELETE CASCADE
);