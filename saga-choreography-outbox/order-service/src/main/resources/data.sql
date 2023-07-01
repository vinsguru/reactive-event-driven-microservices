DROP TABLE IF EXISTS order_payment;
DROP TABLE IF EXISTS order_inventory;
DROP TABLE IF EXISTS purchase_order;
DROP TABLE IF EXISTS order_outbox;

CREATE TABLE purchase_order (
    order_id uuid default random_uuid() primary key,
    customer_id int,
    product_id int,
    quantity int,
    unit_price int,
    amount int,
    status VARCHAR(50),
    delivery_date TIMESTAMP,
    version int
);

CREATE TABLE order_payment (
    id int AUTO_INCREMENT primary key,
    order_id uuid unique,
    payment_id uuid,
    success boolean,
    message VARCHAR(50),
    status VARCHAR(50),
    foreign key (order_id) references purchase_order(order_id)
);

CREATE TABLE order_inventory (
    id int AUTO_INCREMENT primary key,
    order_id uuid unique,
    inventory_id uuid,
    success boolean,
    status VARCHAR(50),
    message VARCHAR(50),
    foreign key (order_id) references purchase_order(order_id)
);

CREATE TABLE order_outbox (
    id bigint AUTO_INCREMENT primary key,
    message binary(10000),
    status VARCHAR(50),
    created_at timestamp
);