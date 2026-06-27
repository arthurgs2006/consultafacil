CREATE TABLE availabilities (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    professional_id BIGINT   NOT NULL,
    date            DATE     NOT NULL,
    start_time      TIME     NOT NULL,
    end_time        TIME     NOT NULL,
    booked          BOOLEAN  NOT NULL DEFAULT FALSE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_availabilities_professional FOREIGN KEY (professional_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
