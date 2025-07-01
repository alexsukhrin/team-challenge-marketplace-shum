CREATE TABLE product_product_colors (
    product_id INT NOT NULL,
    color_id INT NOT NULL,
    PRIMARY KEY (product_id, color_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (color_id) REFERENCES product_colors(id) ON DELETE CASCADE
);