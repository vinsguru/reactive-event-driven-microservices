DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS order_inventory;

CREATE TABLE product (
   id int AUTO_INCREMENT primary key,
   description VARCHAR(50),
   available_quantity int
);

CREATE TABLE order_inventory (
   inventory_id uuid default random_uuid() primary key,
   order_id uuid,
   product_id int,
   status VARCHAR(50),
   quantity int,
   foreign key (product_id) references product(id)
);

insert into product(description, available_quantity)
    values
        ('book', 10),
        ('pen', 10),
        ('rug', 10);