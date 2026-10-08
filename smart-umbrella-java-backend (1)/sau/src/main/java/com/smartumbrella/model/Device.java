package com.smartumbrella.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "devices")
public class Device {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(name = "user_id") public Long userId;
    @Column(name = "device_code") public String deviceCode;
    @Column(name = "sim_number") public String simNumber;
    @Column(name = "api_key_hash") public String apiKeyHash;
    @Column(name = "connection_status") public String connectionStatus = "offline";
    @Column(name = "gps_status") public String gpsStatus = "searching";
    @Column(name = "gsm_status") public String gsmStatus = "no_signal";
    @Column(name = "battery_percent") public Integer batteryPercent;
    @Column(name = "firmware_version") public String firmwareVersion = "v1.0.0-proto";
    @Column(name = "last_seen_at") public Instant lastSeenAt;
}
