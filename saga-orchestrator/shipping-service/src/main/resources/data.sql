DROP TABLE IF EXISTS shipment;

CREATE TABLE shipment (
   id uuid default random_uuid() primary key,
   order_id uuid,
   product_id int,
   customer_id int,
   quantity int,
   status VARCHAR(50),
   delivery_date TIMESTAMP
);
