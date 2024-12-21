-- 1. Routers Table
CREATE TABLE Routers (
                         id BIGSERIAL PRIMARY KEY,
                         router_name VARCHAR(255) UNIQUE NOT NULL,
                         router_ip_address VARCHAR(255),
                         username VARCHAR(255) NOT NULL,
                         password VARCHAR(255),
                         description TEXT
);

-- 2. Bandwidth Limits Table
CREATE TABLE Bandwidth_Limits (
                                  id BIGSERIAL PRIMARY KEY,
                                  name VARCHAR(255) UNIQUE NOT NULL,
                                  upload_speed INTEGER NOT NULL,
                                  download_speed INTEGER NOT NULL,
                                  upload_unit VARCHAR(50) NOT NULL,
                                  download_unit VARCHAR(50) NOT NULL,
                                  router_id BIGINT,
                                  CONSTRAINT fk_bandwidth_router FOREIGN KEY (router_id) REFERENCES Routers(id) ON DELETE CASCADE
);

-- 3. IP Pool Table
CREATE TABLE IP_Pool (
                         id BIGSERIAL PRIMARY KEY,
                         pool_name VARCHAR(255) UNIQUE NOT NULL,
                         ip_range VARCHAR(255),
                         router_id BIGINT,
                         CONSTRAINT fk_ip_pool_router FOREIGN KEY (router_id) REFERENCES Routers(id) ON DELETE CASCADE
);

-- 4. Package Plans Table
CREATE TABLE Package_Plans (
                               id BIGSERIAL PRIMARY KEY,
                               package_plan VARCHAR(255) UNIQUE NOT NULL,
                               data_limit VARCHAR(255),
                               plan_validity INTEGER,
                               plan_duration VARCHAR(50),
                               price INTEGER,
                               bandwidth_id BIGINT,
                               router_id BIGINT,
                               CONSTRAINT fk_package_bandwidth FOREIGN KEY (bandwidth_id) REFERENCES Bandwidth_Limits(id) ON DELETE CASCADE,
                               CONSTRAINT fk_package_router FOREIGN KEY (router_id) REFERENCES Routers(id) ON DELETE CASCADE
);

-- 5. Hotspot Clients Table
CREATE TABLE Clients_Info (
                              id BIGSERIAL PRIMARY KEY,
                              created_on TIMESTAMP,
                              expires_on TIMESTAMP,
                              plan VARCHAR(255),
                              username VARCHAR(255),
                              ip_address VARCHAR(255),
                              router_id BIGINT NOT NULL,
                              mpesa_receipt_number VARCHAR(255),
                              phone_number VARCHAR(255),
                              amount INTEGER,
                              login_by VARCHAR(50),
                              CONSTRAINT fk_clients_router FOREIGN KEY (router_id) REFERENCES Routers(id) ON DELETE CASCADE
);

-- 6. PPPoE Clients Table
CREATE TABLE PPPoE_Clients (
                               id BIGSERIAL PRIMARY KEY,
                               account VARCHAR(255),
                               name VARCHAR(255),
                               phone VARCHAR(255),
                               plan VARCHAR(255),
                               payment VARCHAR(255),
                               balance VARCHAR(255),
                               status VARCHAR(255),
                               username VARCHAR(255),
                               password VARCHAR(255),
                               router_id BIGINT,
                               CONSTRAINT fk_pppoe_clients_router FOREIGN KEY (router_id) REFERENCES Routers(id) ON DELETE CASCADE
                               CONSTRAINT fk_pppoe_clients_plans FOREIGN KEY (plan_id) REFERENCES PPPoE_Plans(id) ON DELETE CASCADE
);

-- 7. PPPoE Plans Table
CREATE TABLE PPPoE_Plans (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(255),
                             plan_validity VARCHAR(255),
                             bandwidth_id BIGINT,
                             ip_pool_id BIGINT,
                             router_id BIGINT,
                             CONSTRAINT fk_pppoe_plans_bandwidth FOREIGN KEY (bandwidth_id) REFERENCES Bandwidth_Limits(id) ON DELETE CASCADE,
                             CONSTRAINT fk_pppoe_plans_ip_pool FOREIGN KEY (ip_pool_id) REFERENCES IP_Pool(id) ON DELETE CASCADE,
                             CONSTRAINT fk_pppoe_plans_router FOREIGN KEY (router_id) REFERENCES Routers(id) ON DELETE CASCADE
);

-- 8. Payment Session Table
CREATE TABLE Payment_Session (
                                 id BIGSERIAL PRIMARY KEY,
                                 temp_request_id VARCHAR(255),
                                 checkout_request_id VARCHAR(255),
                                 ip VARCHAR(255),
                                 mac VARCHAR(255),
                                 package_type VARCHAR(255),
                                 router_id BIGINT,
                                 phone_number VARCHAR(255),
                                 amount VARCHAR(255),
                                 status VARCHAR(50),
                                 CONSTRAINT fk_payment_session_router FOREIGN KEY (router_id) REFERENCES Routers(id) ON DELETE CASCADE
);

-- 9. Transactions Info Table
CREATE TABLE Mpesa_Transactions (
                                    id BIGSERIAL PRIMARY KEY,
                                    mac_address VARCHAR(255),
                                    phone_number VARCHAR(255) NOT NULL,
                                    mpesa_code VARCHAR(255) UNIQUE NOT NULL,
                                    amount VARCHAR(255) NOT NULL,
                                    date TIMESTAMP,
                                    status VARCHAR(50)
);

-- 10. Voucher Table
CREATE TABLE Voucher (
                         id BIGSERIAL PRIMARY KEY,
                         voucher_code VARCHAR(255),
                         status VARCHAR(50),
                         created_at TIMESTAMP,
                         redeemed_by VARCHAR(255),
                         ip_address VARCHAR(255),
                         expiry_date TIMESTAMP,
                         plan_id BIGINT,
                         router_id BIGINT,
                         CONSTRAINT fk_voucher_plan FOREIGN KEY (plan_id) REFERENCES Package_Plans(id) ON DELETE CASCADE,
                         CONSTRAINT fk_voucher_router FOREIGN KEY (router_id) REFERENCES Routers(id) ON DELETE CASCADE
);


-- Sequence Definitions for Manual Handling
CREATE SEQUENCE routers_seq START 1 INCREMENT 1;
CREATE SEQUENCE bandwidth_limits_seq START 1 INCREMENT 1;
CREATE SEQUENCE ip_pool_seq START 1 INCREMENT 1;
CREATE SEQUENCE package_plans_seq START 1 INCREMENT 1;
CREATE SEQUENCE clients_info_seq START 1 INCREMENT 1;
CREATE SEQUENCE pppoe_clients_seq START 1 INCREMENT 1;
CREATE SEQUENCE pppoe_plans_seq START 1 INCREMENT 1;
CREATE SEQUENCE payment_session_seq START 1 INCREMENT 1;
CREATE SEQUENCE mpesa_transactions_seq START 1 INCREMENT 1;
CREATE SEQUENCE voucher_seq START 1 INCREMENT 1;
