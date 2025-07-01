CREATE TABLE product_payment_options (
    product_id INT NOT NULL,
    payment_option_id INT NOT NULL,
    PRIMARY KEY (product_id, payment_option_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_option_id) REFERENCES payment_options(id) ON DELETE CASCADE
);
