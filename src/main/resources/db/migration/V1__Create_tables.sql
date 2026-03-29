-- Create tables
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255),
    name VARCHAR(255),
    password VARCHAR(255),
    verified BOOLEAN
);

CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE TABLE packages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    country VARCHAR(255),
    description VARCHAR(255),
    price FLOAT,
    credits INTEGER
);

CREATE TABLE user_package (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    package_id BIGINT,
    remaining_credits INTEGER,
    expiry_date TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (package_id) REFERENCES packages(id)
);

CREATE TABLE class_schedule (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    country VARCHAR(255),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    capacity INTEGER,
    required_credits INTEGER
);

CREATE TABLE booking (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    class_schedule_id BIGINT,
    status VARCHAR(255),
    booking_time TIMESTAMP,
    checked_in BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (class_schedule_id) REFERENCES class_schedule(id)
);

CREATE TABLE waitlist (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    class_schedule_id BIGINT,
    position INTEGER,
    status VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (class_schedule_id) REFERENCES class_schedule(id)
);