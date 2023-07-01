DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS customer_payment;

CREATE TABLE customer (
   id int AUTO_INCREMENT primary key,
   name VARCHAR(50) NOT NULL,
   balance int
);

CREATE TABLE customer_payment (
   payment_id uuid default random_uuid() primary key,
   order_id uuid,
   customer_id int,
   status VARCHAR(50),
   amount int,
   foreign key (customer_id) references customer(id)
);

insert into customer(name, balance)
    values
        ('sam', 100),
        ('mike', 100),
        ('john', 100);