-- 1. Create Sequences for SEQUENCE-based IDs
-------------------------------

-- For Clients entity (GenerationType.SEQUENCE)
CREATE SEQUENCE clients_id_seq
    START WITH 1
    INCREMENT BY 1;

-- For IPPool entity (GenerationType.SEQUENCE)
CREATE SEQUENCE ip_pool_id_seq
    START WITH 1
    INCREMENT BY 1;

-- For Plans entity (GenerationType.SEQUENCE)
CREATE SEQUENCE plans_id_seq
    START WITH 1
    INCREMENT BY 1;

-- For PPPoETransaction entity (GenerationType.SEQUENCE) -> table: pppoe_payments
CREATE SEQUENCE pppoe_payments_id_seq
    START WITH 1
    INCREMENT BY 1;

-- For TransactionsInfo entity (GenerationType.SEQUENCE) -> table: mpesa_transactions
CREATE SEQUENCE transactions_info_id_seq
    START WITH 1
    INCREMENT BY 1;

-- For Voucher entity (GenerationType.SEQUENCE)
CREATE SEQUENCE voucher_id_seq
    START WITH 1
    INCREMENT BY 1;

-------------------------------
-- 2. Create Tables with proper foreign keys and defaults
-------------------------------

-- 2.1 Create "routers" table first (GenerationType.IDENTITY uses bigserial)
CREATE TABLE routers (
                         id BIGSERIAL PRIMARY KEY,
                         router_name VARCHAR(255) NOT NULL UNIQUE,
                         router_ip_address VARCHAR(255) NOT NULL UNIQUE,
                         username VARCHAR(255) NOT NULL,
                         password VARCHAR(255),
                         router_interface VARCHAR(255),
                         description TEXT
);

-- 2.2 Create "bandwidth_limits" table (GenerationType.IDENTITY -> bigserial)
CREATE TABLE bandwidth_limits (
                                  id BIGSERIAL PRIMARY KEY,
                                  name VARCHAR(255) NOT NULL UNIQUE,
                                  upload_speed INTEGER NOT NULL,
                                  download_speed INTEGER NOT NULL,
                                  upload_unit VARCHAR(50) NOT NULL,
                                  download_unit VARCHAR(50) NOT NULL,
                                  router_id BIGINT,
                                  CONSTRAINT fk_bandwidth_router FOREIGN KEY (router_id)
                                      REFERENCES routers (id)
                                      ON DELETE SET NULL
);

-- 2.3 Create "ip_pool" table (uses explicit sequence)
CREATE TABLE ip_pool (
                         id BIGINT NOT NULL DEFAULT nextval('ip_pool_id_seq'),
                         pool_name VARCHAR(255) NOT NULL UNIQUE,
                         ip_range VARCHAR(255),
                         router_id BIGINT,
                         CONSTRAINT pk_ip_pool PRIMARY KEY (id),
                         CONSTRAINT fk_ip_pool_router FOREIGN KEY (router_id)
                             REFERENCES routers (id)
                             ON DELETE SET NULL
);

-- 2.4 Create "plans" table (uses explicit sequence)
CREATE TABLE plans (
                       id BIGINT NOT NULL DEFAULT nextval('plans_id_seq'),
                       plan_name VARCHAR(255),
                       plan_validity INTEGER,
                       price INTEGER,
                       service_type VARCHAR(50),
                       bandwidth_id BIGINT,
                       ip_pool_id BIGINT,
                       router_id BIGINT,
                       CONSTRAINT pk_plans PRIMARY KEY (id),
                       CONSTRAINT fk_plans_bandwidth FOREIGN KEY (bandwidth_id)
                           REFERENCES bandwidth_limits (id)
                           ON DELETE SET NULL,
                       CONSTRAINT fk_plans_ip_pool FOREIGN KEY (ip_pool_id)
                           REFERENCES ip_pool (id)
                           ON DELETE SET NULL,
                       CONSTRAINT fk_plans_router FOREIGN KEY (router_id)
                           REFERENCES routers (id)
                           ON DELETE SET NULL
);

-- 2.5 Create "clients" table (uses explicit sequence)
CREATE TABLE clients (
                         id BIGINT NOT NULL DEFAULT nextval('clients_id_seq'),
                         account VARCHAR(255),
                         full_name VARCHAR(255),
                         email VARCHAR(255),
                         address VARCHAR(255),
                         phone VARCHAR(50),
                         payment INTEGER,
                         mpesa_ref VARCHAR(255),
                         balance INTEGER,
                         username VARCHAR(255),
                         password VARCHAR(255),
                         active_period VARCHAR(50),
                         type VARCHAR(50),
                         created_on TIMESTAMP,
                         expires_on TIMESTAMP,
                         login_by VARCHAR(50),
                         status VARCHAR(50),
                         plan_id BIGINT,
                         router_id BIGINT,
                         CONSTRAINT pk_clients PRIMARY KEY (id),
                         CONSTRAINT fk_clients_plan FOREIGN KEY (plan_id)
                             REFERENCES plans (id)
                             ON DELETE SET NULL,
                         CONSTRAINT fk_clients_router FOREIGN KEY (router_id)
                             REFERENCES routers (id)
                             ON DELETE SET NULL
);

-- 2.6 Create "payment_session" table (GenerationType.IDENTITY uses bigserial)
CREATE TABLE payment_session (
                                 id BIGSERIAL PRIMARY KEY,
                                 temp_request_id VARCHAR(255),
                                 checkout_request_id VARCHAR(255),
                                 ip VARCHAR(50),
                                 mac VARCHAR(50),
                                 package_type VARCHAR(50),
                                 router_name VARCHAR(255),
                                 phone_number VARCHAR(50),
                                 amount VARCHAR(50),
                                 status VARCHAR(50),
                                 short_code_type VARCHAR(50),
                                 short_code VARCHAR(50)
);

-- 2.7 Create "pppoe_payments" table (uses explicit sequence)
CREATE TABLE pppoe_payments (
                                id BIGINT NOT NULL DEFAULT nextval('pppoe_payments_id_seq'),
                                account VARCHAR(255),
                                phone_number VARCHAR(50) NOT NULL,
                                mpesa_code VARCHAR(255) NOT NULL,
                                amount VARCHAR(50) NOT NULL,
                                date TIMESTAMP,
                                status VARCHAR(50),
                                router VARCHAR(255),
                                CONSTRAINT pk_pppoe_payments PRIMARY KEY (id)
);

-- 2.8 Create "mpesa_transactions" table (uses explicit sequence)
CREATE TABLE mpesa_transactions (
                                    id BIGINT NOT NULL DEFAULT nextval('transactions_info_id_seq'),
                                    mac_address VARCHAR(255),
                                    phone_number VARCHAR(50) NOT NULL,
                                    code VARCHAR(255) NOT NULL,
                                    amount VARCHAR(50) NOT NULL,
                                    date TIMESTAMP,
                                    status VARCHAR(50),
                                    CONSTRAINT pk_mpesa_transactions PRIMARY KEY (id)
);

-- 2.9 Create "voucher" table (uses explicit sequence)
CREATE TABLE voucher (
                         id BIGINT NOT NULL DEFAULT nextval('voucher_id_seq'),
                         voucher_code VARCHAR(255),
                         status VARCHAR(50),
                         created_at TIMESTAMP,
                         redeemed_by VARCHAR(255),
                         ip_address VARCHAR(50),
                         expiry_date TIMESTAMP,
                         plan_id BIGINT,
                         router_id BIGINT,
                         CONSTRAINT pk_voucher PRIMARY KEY (id),
                         CONSTRAINT fk_voucher_plan FOREIGN KEY (plan_id)
                             REFERENCES plans (id)
                             ON DELETE SET NULL,
                         CONSTRAINT fk_voucher_router FOREIGN KEY (router_id)
                             REFERENCES routers (id)
                             ON DELETE SET NULL
);
