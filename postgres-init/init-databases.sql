-- Each service owns its own database (database-per-service), even though
-- both run on a single Postgres instance for this demo.
CREATE USER orders WITH PASSWORD 'orders';
CREATE DATABASE orders_db OWNER orders;

CREATE USER inventory WITH PASSWORD 'inventory';
CREATE DATABASE inventory_db OWNER inventory;
