CREATE TABLE product_materials (
    product_id INT NOT NULL,
    material_id INT NOT NULL,
    PRIMARY KEY (product_id, material_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES materials(id) ON DELETE CASCADE
);