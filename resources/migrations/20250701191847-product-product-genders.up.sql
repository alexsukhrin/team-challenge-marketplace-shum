CREATE TABLE product_product_genders (
    product_id INT NOT NULL,
    gender_id INT NOT NULL,
    PRIMARY KEY (product_id, gender_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (gender_id) REFERENCES product_genders(id) ON DELETE CASCADE
);