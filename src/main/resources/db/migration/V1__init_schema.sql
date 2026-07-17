CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NULL,
                       phone VARCHAR(20),
                       avatar_url VARCHAR(255),
                       role ENUM('ADMIN','PRODUCER','VENUE_OWNER','CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
                       status ENUM('ACTIVE','LOCKED') NOT NULL DEFAULT 'ACTIVE',
                       auth_provider ENUM('LOCAL','GOOGLE') NOT NULL DEFAULT 'LOCAL',
                       provider_id VARCHAR(100) NULL,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       UNIQUE KEY uq_provider (auth_provider, provider_id)
);

CREATE TABLE producer_profiles (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   user_id BIGINT NOT NULL UNIQUE,
                                   company_name VARCHAR(150) NOT NULL,
                                   contact_email VARCHAR(150),
                                   contact_phone VARCHAR(20),
                                   description TEXT,
                                   status ENUM('PENDING_VERIFICATION','VERIFIED','REJECTED') NOT NULL DEFAULT 'PENDING_VERIFICATION',
                                   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE venue_profiles (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                user_id BIGINT NOT NULL UNIQUE,
                                venue_name VARCHAR(150) NOT NULL,
                                address VARCHAR(255) NOT NULL,
                                city VARCHAR(100) NOT NULL,
                                area_sqm DOUBLE,
                                description TEXT,
                                status ENUM('PENDING_VERIFICATION','VERIFIED','REJECTED') NOT NULL DEFAULT 'PENDING_VERIFICATION',
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE rooms (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       venue_id BIGINT NOT NULL,
                       name VARCHAR(50) NOT NULL,
                       total_rows INT NOT NULL,
                       total_columns INT NOT NULL,
                       status ENUM('ACTIVE','MAINTENANCE') NOT NULL DEFAULT 'ACTIVE',
                       FOREIGN KEY (venue_id) REFERENCES venue_profiles(id),
                       UNIQUE KEY uq_room_venue (venue_id, name)
);

CREATE TABLE seat_types (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(30) NOT NULL,
                            extra_price DECIMAL(10,2) DEFAULT 0
);

CREATE TABLE seats (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       room_id BIGINT NOT NULL,
                       seat_row VARCHAR(5) NOT NULL,
                       seat_number INT NOT NULL,
                       seat_type_id BIGINT NOT NULL,
                       FOREIGN KEY (room_id) REFERENCES rooms(id),
                       FOREIGN KEY (seat_type_id) REFERENCES seat_types(id),
                       UNIQUE KEY uq_seat_position (room_id, seat_row, seat_number)
);

CREATE TABLE events (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        producer_id BIGINT NOT NULL,
                        title VARCHAR(200) NOT NULL,
                        type ENUM('MOVIE','CONCERT','WORKSHOP','THEATER','PARTY') NOT NULL,
                        description TEXT,
                        poster_url VARCHAR(255),
                        status ENUM('DRAFT','SUBMITTED','ADMIN_REVIEWING','MATCHING','PENDING_VENUE_APPROVAL',
              'VENUE_ACCEPTED','VENUE_REJECTED','CONTRACT_CONFIRMED','PUBLISHED',
              'ONGOING','COMPLETED','SETTLED','CANCELLED') NOT NULL DEFAULT 'DRAFT',
                        avg_rating DECIMAL(3,2) DEFAULT 0,
                        detail_doc_id VARCHAR(50),
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (producer_id) REFERENCES producer_profiles(id)
);

CREATE TABLE event_status_history (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      event_id BIGINT NOT NULL,
                                      from_status VARCHAR(30),
                                      to_status VARCHAR(30) NOT NULL,
                                      changed_by BIGINT,
                                      note TEXT,
                                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                      FOREIGN KEY (event_id) REFERENCES events(id),
                                      FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE TABLE event_venue_contracts (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       event_id BIGINT NOT NULL,
                                       venue_id BIGINT NOT NULL,
                                       room_id BIGINT NULL,
                                       producer_share_percent DECIMAL(5,2) NOT NULL,
                                       venue_share_percent DECIMAL(5,2) NOT NULL,
                                       admin_commission_percent DECIMAL(5,2) NOT NULL,
                                       ticket_base_price DECIMAL(10,2),
                                       status ENUM('PROPOSED','ACCEPTED','REJECTED') NOT NULL DEFAULT 'PROPOSED',
                                       reject_reason TEXT,
                                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                       FOREIGN KEY (event_id) REFERENCES events(id),
                                       FOREIGN KEY (venue_id) REFERENCES venue_profiles(id),
                                       FOREIGN KEY (room_id) REFERENCES rooms(id)
);

CREATE TABLE showtimes (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           contract_id BIGINT NOT NULL,
                           start_time DATETIME NOT NULL,
                           end_time DATETIME NOT NULL,
                           ticket_price DECIMAL(10,2) NOT NULL,
                           status ENUM('SCHEDULED','CANCELLED','FINISHED') NOT NULL DEFAULT 'SCHEDULED',
                           FOREIGN KEY (contract_id) REFERENCES event_venue_contracts(id)
);

CREATE TABLE vouchers (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          code VARCHAR(30) NOT NULL UNIQUE,
                          discount_type ENUM('PERCENT','FIXED') NOT NULL,
                          discount_value DECIMAL(10,2) NOT NULL,
                          max_discount DECIMAL(10,2),
                          min_order_value DECIMAL(10,2) DEFAULT 0,
                          quantity INT NOT NULL,
                          used_count INT DEFAULT 0,
                          valid_from DATETIME,
                          valid_to DATETIME,
                          status ENUM('ACTIVE','EXPIRED','DISABLED') NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE bookings (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          user_id BIGINT NOT NULL,
                          showtime_id BIGINT NOT NULL,
                          voucher_id BIGINT NULL,
                          booking_code VARCHAR(20) NOT NULL UNIQUE,
                          total_price DECIMAL(10,2) NOT NULL,
                          discount_amount DECIMAL(10,2) DEFAULT 0,
                          status ENUM('PENDING','CONFIRMED','CANCELLED','EXPIRED') NOT NULL DEFAULT 'PENDING',
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(id),
                          FOREIGN KEY (showtime_id) REFERENCES showtimes(id),
                          FOREIGN KEY (voucher_id) REFERENCES vouchers(id),
                          INDEX idx_booking_user (user_id),
                          INDEX idx_booking_showtime (showtime_id)
);

CREATE TABLE booking_seats (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               booking_id BIGINT NOT NULL,
                               showtime_id BIGINT NOT NULL,
                               seat_id BIGINT NOT NULL,
                               price DECIMAL(10,2) NOT NULL,
                               FOREIGN KEY (booking_id) REFERENCES bookings(id),
                               FOREIGN KEY (seat_id) REFERENCES seats(id),
                               UNIQUE KEY uq_showtime_seat (showtime_id, seat_id)
);

CREATE TABLE payments (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          booking_id BIGINT NOT NULL UNIQUE,
                          method ENUM('VNPAY','MOMO') NOT NULL,
                          amount DECIMAL(10,2) NOT NULL,
                          vnp_txn_ref VARCHAR(100) NOT NULL UNIQUE,
                          vnp_transaction_no VARCHAR(100),
                          vnp_response_code VARCHAR(10),
                          vnp_bank_code VARCHAR(20),
                          status ENUM('PENDING','SUCCESS','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
                          paid_at DATETIME,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE revenue_transactions (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      booking_id BIGINT NOT NULL,
                                      contract_id BIGINT NOT NULL,
                                      total_amount DECIMAL(10,2) NOT NULL,
                                      producer_amount DECIMAL(10,2) NOT NULL,
                                      venue_amount DECIMAL(10,2) NOT NULL,
                                      admin_amount DECIMAL(10,2) NOT NULL,
                                      status ENUM('PENDING','SETTLED','REVERSED') NOT NULL DEFAULT 'PENDING',
                                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                      FOREIGN KEY (booking_id) REFERENCES bookings(id),
                                      FOREIGN KEY (contract_id) REFERENCES event_venue_contracts(id)
);

CREATE TABLE settlements (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             partner_type ENUM('PRODUCER','VENUE') NOT NULL,
                             partner_id BIGINT NOT NULL,
                             period_from DATE NOT NULL,
                             period_to DATE NOT NULL,
                             total_amount DECIMAL(12,2) NOT NULL,
                             status ENUM('PENDING','PAID') NOT NULL DEFAULT 'PENDING',
                             paid_at DATETIME,
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);