-- One logical database per service, co-hosted on a single Postgres container for local dev.
CREATE DATABASE user_db;
CREATE DATABASE inventory_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE notification_db;
