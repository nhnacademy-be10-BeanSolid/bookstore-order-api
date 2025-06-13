CREATE TABLE wrapping (
                          wrapping_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          price INT NOT NULL,
                          is_active BOOLEAN NOT NULL
);
-- (다른 테이블도 여기에)