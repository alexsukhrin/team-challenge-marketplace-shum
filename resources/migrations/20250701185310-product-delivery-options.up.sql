CREATE TABLE product_delivery_options (
    product_id INT NOT NULL,
    delivery_option_id INT NOT NULL,
    PRIMARY KEY (product_id, delivery_option_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (delivery_option_id) REFERENCES delivery_options(id) ON DELETE CASCADE
);
