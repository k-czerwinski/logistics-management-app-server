INSERT INTO "company" ("id", "name", "domain")
VALUES (1, 'TechCorp', 'c1'),
       (2, 'RetailWorld', 'c2');

INSERT INTO "user" ("id", "company", "username", "first_name","last_name", "password", "role", "temporary_password")
VALUES (1, 1, 'a', 'John', 'Smith', crypt('p', gen_salt('bf')), 1, FALSE),
       (2, 1, 'b', 'John', 'Smith', crypt('p', gen_salt('bf')), 2, FALSE),
       (3, 2, 'a', 'John', 'Smith', crypt('p', gen_salt('bf')), 1, FALSE),
       (4, 2, 'b', 'John', 'Smith', crypt('p', gen_salt('bf')), 2, FALSE),
       (5, 2, 'c', 'John', 'Smith', crypt('p', gen_salt('bf')), 3, FALSE);

INSERT INTO "product" ("id", "company", "price", "name", "description")
VALUES (1, 1, 19.99, 'Widget A', 'High-quality widget'),
       (2, 1, 29.99, 'Gadget B', 'Advanced gadget'),
       (3, 1, 39.99, 'Tool C', 'Durable tool'),
       (4, 1, 49.99, 'Device D', 'Innovative device'),
       (5, 1, 59.99, 'Instrument E', 'Precision instrument'),
       (6, 2, 14.99, 'Accessory F', 'Stylish accessory'),
       (7, 2, 24.99, 'Apparel G', 'Comfortable apparel'),
       (8, 2, 34.99, 'Footwear H', 'Durable footwear'),
       (9, 2, 44.99, 'Bag I', 'Spacious bag'),
       (10, 2, 54.99, 'Watch J', 'Elegant watch');

INSERT INTO "order" ("id", "company", "client", "name", "placed_on", "send_on", "expected_delivery", "delivered_on", "courier", "total_price")
VALUES
    (1, 1, 1, 'Order 1', NOW() - interval '4 day', NOW() - interval '3 days', NOW() - interval '2 days', NOW() - interval '2 days', 2, 125.10),
    (2, 1, 1, 'Order 2', NOW() - interval '5 day', NOW() - interval '4 days', NOW() - interval '3 days', NOW() - interval '3 days', 2, 121.20),
    (3, 1, 1, 'Order 3', NOW() - interval '6 day', NOW() - interval '5 days', NOW() - interval '4 days', NOW() - interval '4 days', 2, 2137.00),
    (4, 1, 1, 'Order 4', NOW() - interval '7 day', NOW() - interval '6 days', NOW() - interval '5 days', NOW() - interval '5 days', 2, 115.30),
    (5, 1, 1, 'Order 5', NOW() - interval '1 day', NOW() - interval '1 day', NOW() + interval '1 days', NULL, 2, 222.20),
    (6, 1, 1, 'Order 6', NOW() - interval '3 day', NOW() - interval '2 days', NOW(), NULL, 2, 1255.10),
    (7, 1, 1, 'Order 7', NOW() - interval '4 day', NOW() - interval '3 days', NOW() - interval '2 days', NULL, 2, 125.10),
    (8, 1, 1, 'Order 8', NOW() - interval '4 day', NULL, NULL, NULL, 2, 435.56),
    (9, 1, 1, 'Order 9', NOW() - interval '3 day', NULL, NULL, NULL, 2, 123.45),
    (10, 1, 1, 'Order 10', NOW() - interval '2 day', NULL, NULL, NULL, 2, 440.00);

DO
$$
DECLARE
order_counter INT := 11;
BEGIN
FOR i IN 2..5 LOOP
        FOR j IN 1..3 LOOP
            INSERT INTO "order" ("id", "company", "client", "name", "send_on", "placed_on", "delivered_on", "expected_delivery", "courier", "total_price")
            VALUES (order_counter, CASE WHEN i < 4 THEN 1 ELSE 2 END, i, 'Order ' || order_counter, NOW() - interval '1 day' * order_counter, NOW() - interval '2 days' * order_counter, NULL, NOW() + interval '3 days', 2, 150 + order_counter * 10);
            order_counter:= order_counter + 1;
END LOOP;
END LOOP;
END $$;

INSERT INTO "order_product" ("order", "product", "quantity")
VALUES (1, 1, 2),
       (1, 2, 1),
       (2, 3, 1),
       (3, 4, 1),
       (4, 5, 1),
       (5, 6, 2),
       (6, 7, 1),
       (7, 8, 1),
       (8, 9, 1),
       (9, 10, 1),
       (10, 1, 3),
       (11, 2, 2),
       (12, 3, 1),
       (13, 4, 2),
       (14, 5, 1),
       (15, 6, 2),
       (16, 7, 3),
       (17, 8, 2),
       (18, 9, 1),
       (19, 10, 2),
       (20, 1, 1);
