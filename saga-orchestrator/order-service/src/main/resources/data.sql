DROP TABLE IF EXISTS order_workflow_action;
DROP TABLE IF EXISTS purchase_order;

CREATE TABLE purchase_order (
   order_id uuid default random_uuid() primary key,
   customer_id int,
   product_id int,
   quantity int,
   unit_price int,
   amount int,
   status VARCHAR(100),
   delivery_date TIMESTAMP
);

CREATE TABLE order_workflow_action (
   id uuid default random_uuid() primary key,
   order_id uuid,
   action VARCHAR(100),
   created_at TIMESTAMP,
   foreign key (order_id) references purchase_order(order_id)
);
