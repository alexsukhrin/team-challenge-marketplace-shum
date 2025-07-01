CREATE TABLE product_materials (
    product_id INT NOT NULL,
    characteristic_id INT NOT NULL,
    value VARCHAR(255),
    PRIMARY KEY (product_id, characteristic_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (characteristic_id) REFERENCES product_characteristics(id) ON DELETE CASCADE
);
