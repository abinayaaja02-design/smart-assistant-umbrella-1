package com.smartumbrella.service;

import com.smartumbrella.model.*;
import com.smartumbrella.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Single place where umbrella data is stored. Used by BOTH the real hardware
 * controller and the demo simulator, so swapping mock data for real data needs no other change.
 */
@Service
public class IngestService {
    public static final Map<String, String> HAPTIC_LABELS = Map.of(
        "obstacle", "Obstacle detected", "left", "Turn left", "right", "Turn right",
        "stop", "Stop", "danger", "Danger ahead", "emergency", "Emergency");

    private final DeviceRepository devices;
    private final LocationRepository locations;
    private final AlertEventRepository events;
    private final EmergencyContactRepository contacts;

    public IngestService(DeviceRepository d, LocationRepository l, AlertEventRepository e, EmergencyContactRepository c) {
        devices = d; locations = l; events = e; contacts = c;
    }

    private void touch(Device d) {
        d.connectionStatus = "online"; d.lastSeenAt = Instant.now(); devices.save(d);
    }

    private AlertEvent event(Device d, String type, String sub, String msg, String status, Double lat, Double lng, String src) {
        AlertEvent e = new AlertEvent();
        e.userId = d.userId; e.deviceId = d.id; e.eventType = type; e.subtype = sub; e.message = msg;
        e.status = status; e.latitude = lat; e.longitude = lng; e.source = src;
        return events.save(e);
    }

    @Transactional
    public void location(Device d, double lat, double lng, Double acc, String src) {
        LocationRecord r = new LocationRecord();
        r.deviceId = d.id; r.latitude = lat; r.longitude = lng; r.accuracyM = acc; r.source = src;
        locations.save(r);
        event(d, "gps", "location_update", "GPS location updated", "recorded", lat, lng, src);
        d.gpsStatus = "fixed"; touch(d);
    }

    @Transactional
    public Map<String, Object> sos(Device d, Double lat, Double lng, String note, String src) {
        if (lat == null || lng == null) {
            var last = locations.findFirstByDeviceIdOrderByRecordedAtDesc(d.id);
            if (last.isPresent()) { lat = last.get().latitude; lng = last.get().longitude; }
        }
        List<EmergencyContact> cs = contacts.findByUserIdOrderByIsPrimaryDescPriorityAsc(d.userId);
        AlertEvent e = event(d, "sos", "umbrella_button", note == null ? "SOS alert activated" : note,
            cs.isEmpty() ? "no_contacts" : "contacts_notified", lat, lng, src);
        touch(d);
        return Map.of("eventId", e.id, "notify", cs.stream().map(c -> c.phone).toList());
    }

    @Transactional
    public void haptic(Device d, String type, Double lat, Double lng, String src) {
        String label = HAPTIC_LABELS.getOrDefault(type, type);
        event(d, "haptic", type, label + " — Haptic vibration activated", "vibration_activated", lat, lng, src);
        touch(d);
    }

    @Transactional
    public void heartbeat(Device d, Integer battery, String gsm, String gps, String firmware) {
        if (battery != null) d.batteryPercent = battery;
        if (gsm != null) d.gsmStatus = gsm;
        if (gps != null) d.gpsStatus = gps;
        if (firmware != null) d.firmwareVersion = firmware;
        touch(d);
    }
}
