package com.smartumbrella.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "locations")
public class LocationRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(name = "device_id") public Long deviceId;
    public Double latitude;
    public Double longitude;
    @Column(name = "accuracy_m") public Double accuracyM;
    public String source;
    @Column(name = "recorded_at") public Instant recordedAt = Instant.now();
}
