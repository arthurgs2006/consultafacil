CREATE TABLE users (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    username  VARCHAR(50)  NOT NULL,
    email     VARCHAR(255) NOT NULL,
    password  VARCHAR(255) NOT NULL,
    role      VARCHAR(20)  NOT NULL DEFAULT 'PATIENT',
    specialty VARCHAR(100),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
