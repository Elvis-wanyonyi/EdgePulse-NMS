CREATE TABLE bandwith_plans (
                                id SERIAL PRIMARY KEY,
                                name VARCHAR(255),
                                upload VARCHAR(255),
                                download VARCHAR(255)
);

CREATE SEQUENCE clients_info_seq START 1 INCREMENT 1;

CREATE TABLE clients_info (
                              id BIGINT PRIMARY KEY DEFAULT nextval('clients_info_seq'),
                              created_on TIMESTAMP,
                              expires_on TIMESTAMP,
                              plan VARCHAR(255),
                              username VARCHAR(255),
                              ip_address VARCHAR(255),
                              router VARCHAR(255),
                              mpesa_receipt_number VARCHAR(255),
                              phone_number VARCHAR(20),
                              amount INT,
                              login_by VARCHAR(50)
);

CREATE SEQUENCE plans_seq START 1 INCREMENT 1;

CREATE TABLE plans (
                       id BIGINT PRIMARY KEY DEFAULT nextval('plans_seq'),
                       router_name VARCHAR(255),
                       package_name VARCHAR(255),
                       bandwidth_limit VARCHAR(255),
                       data_limit VARCHAR(255),
                       plan_validity INT,
                       plan_duration VARCHAR(50),
                       price INT
);

CREATE TABLE payment_session (
                                 id SERIAL PRIMARY KEY,
                                 temp_request_id VARCHAR(255),
                                 checkout_request_id VARCHAR(255),
                                 ip VARCHAR(255),
                                 mac VARCHAR(255),
                                 package_type VARCHAR(255),
                                 router_name VARCHAR(255),
                                 phone_number VARCHAR(20),
                                 amount VARCHAR(255),
                                 status VARCHAR(50)
);

CREATE TABLE routers (
                         id SERIAL PRIMARY KEY,
                         router_name VARCHAR(255) NOT NULL UNIQUE,
                         router_ip_address VARCHAR(255),
                         username VARCHAR(255) NOT NULL,
                         password VARCHAR(255),
                         dns_name VARCHAR(255),
                         description TEXT
);

CREATE SEQUENCE mpesa_transactions_seq START 1 INCREMENT 1;

CREATE TABLE mpesa_transactions (
                                    id BIGINT PRIMARY KEY DEFAULT nextval('mpesa_transactions_seq'),
                                    mac_address VARCHAR(255),
                                    phone_number VARCHAR(20) NOT NULL,
                                    mpesa_code VARCHAR(255) NOT NULL UNIQUE,
                                    amount VARCHAR(255) NOT NULL,
                                    date TIMESTAMP,
                                    status VARCHAR(50)
);

CREATE SEQUENCE voucher_seq START 1 INCREMENT 1;

CREATE TABLE voucher (
                         id BIGINT PRIMARY KEY DEFAULT nextval('voucher_seq'),
                         voucher_code VARCHAR(255),
                         package_type VARCHAR(255),
                         status VARCHAR(50),
                         created_at TIMESTAMP,
                         redeemed_by VARCHAR(255),
                         ip_address VARCHAR(255),
                         expiry_date TIMESTAMP,
                         router_name VARCHAR(255)
);
