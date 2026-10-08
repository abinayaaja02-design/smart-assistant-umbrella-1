-- Smart Assistant Umbrella - MySQL schema
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  mobile VARCHAR(16) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,          -- BCrypt hash, never plain text
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS devices (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  device_code VARCHAR(32) NOT NULL UNIQUE,
  sim_number VARCHAR(16),
  api_key_hash VARCHAR(64),                     -- SHA-256 of the ESP32 key
  connection_status VARCHAR(20) DEFAULT 'offline',
  gps_status VARCHAR(20) DEFAULT 'searching',
  gsm_status VARCHAR(20) DEFAULT 'no_signal',
  battery_percent INT,
  firmware_version VARCHAR(40) DEFAULT 'v1.0.0-proto',
  last_seen_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS emergency_contacts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  relationship VARCHAR(50),
  phone VARCHAR(16) NOT NULL,
  priority INT DEFAULT 1,
  is_primary BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS locations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  device_id BIGINT NOT NULL,
  latitude DOUBLE NOT NULL,
  longitude DOUBLE NOT NULL,
  accuracy_m DOUBLE,
  source VARCHAR(10) DEFAULT 'demo',
  recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
  INDEX idx_loc_device_time (device_id, recorded_at)
);
CREATE TABLE IF NOT EXISTS events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  device_id BIGINT,
  event_type VARCHAR(10) NOT NULL,               -- sos | gps | haptic | device
  subtype VARCHAR(40),
  message VARCHAR(255),
  status VARCHAR(30),
  latitude DOUBLE,
  longitude DOUBLE,
  source VARCHAR(10) DEFAULT 'demo',             -- hardware | demo | web
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_events_user_time (user_id, created_at)
);
