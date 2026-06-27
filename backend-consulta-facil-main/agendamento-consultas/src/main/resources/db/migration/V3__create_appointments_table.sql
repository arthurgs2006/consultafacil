CREATE TABLE appointments (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    patient_id      BIGINT      NOT NULL,
    professional_id BIGINT      NOT NULL,
    availability_id BIGINT      NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_appointments_availability (availability_id),
    CONSTRAINT fk_appointments_patient      FOREIGN KEY (patient_id)      REFERENCES users         (id),
    CONSTRAINT fk_appointments_professional FOREIGN KEY (professional_id) REFERENCES users         (id),
    CONSTRAINT fk_appointments_availability FOREIGN KEY (availability_id) REFERENCES availabilities (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
