CREATE TABLE refrigerated_truck_telemetry (
    id INT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    truckID INT UNSIGNED NOT NULL,
    temperature INT NOT NULL,
    humidity INT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    ts DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX truckID_idx (truckID)
);