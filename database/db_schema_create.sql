CREATE TABLE "company" (
                           "id" int PRIMARY KEY NOT NULL,
                           "name" char(30) NOT NULL
);

CREATE TABLE "logo" (
                        "id" int PRIMARY KEY NOT NULL,
                        "image" bytea NOT NULL
);

CREATE TABLE "user" (
                        "id" int PRIMARY KEY NOT NULL,
                        "company" int NOT NULL,
                        "username" char(40) NOT NULL,
                        "password" char(100) NOT NULL,
                        "role" int NOT NULL,
                        "temporary_password" bool NOT NULL
);

CREATE TABLE "product" (
                           "id" int PRIMARY KEY NOT NULL,
                           "company" int NOT NULL,
                           "price" decimal NOT NULL,
                           "name" char(20) NOT NULL,
                           "description" char(150)
);

CREATE TABLE "order" (
                         "id" int PRIMARY KEY NOT NULL,
                         "company" int NOT NULL,
                         "client" int NOT NULL,
                         "name" char(20),
                         "send_on" timestamp NOT NULL,
                         "placed_on" timestamp NOT NULL,
                         "delivered_on" timestamp,
                         "expected_delivery" timestamp,
                         "delivery_man" int,
                         "total_price" decimal NOT NULL
);

CREATE TABLE "order_product" (
                                 "order" int,
                                 "product" int,
                                 "quantity" int NOT NULL,
                                 PRIMARY KEY ("order", "product")
);

ALTER TABLE "logo" ADD FOREIGN KEY ("id") REFERENCES "company" ("id");

ALTER TABLE "user" ADD FOREIGN KEY ("company") REFERENCES "company" ("id");

ALTER TABLE "product" ADD FOREIGN KEY ("company") REFERENCES "company" ("id");

ALTER TABLE "order" ADD FOREIGN KEY ("company") REFERENCES "company" ("id");

ALTER TABLE "order" ADD FOREIGN KEY ("client") REFERENCES "user" ("id");

ALTER TABLE "order" ADD FOREIGN KEY ("delivery_man") REFERENCES "user" ("id");

ALTER TABLE "order_product" ADD FOREIGN KEY ("order") REFERENCES "order" ("id");

ALTER TABLE "order_product" ADD FOREIGN KEY ("product") REFERENCES "product" ("id");
