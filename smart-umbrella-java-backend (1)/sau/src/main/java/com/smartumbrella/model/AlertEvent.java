package com.smartumbrella.model;

import jakarta.persistence.*;
import java.time.Instant;

/** One row per SOS / GPS / haptic / device-connection event. Powers the Alert History page. */
@Entity @Table(name = "events")
public class AlertEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(name = "user_id") public Long userId;
    @Column(name = "device_id") public Long deviceId;
    @Column(name = "event_type") public String eventType;
    public String subtype;
    public String message;
    public String status;
    public Double latitude;
    public Double longitude;
    public String source;
    @Column(name = "created_at") public Instant createdAt = Instant.now();
}
