package com.smartumbrella.controller;

import com.smartumbrella.dto.Dtos.*;
import com.smartumbrella.model.Device;
import com.smartumbrella.repository.*;
import com.smartumbrella.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashMap;
import java.util.Map;

/**
 * Hardware REST API. Flow:
 * Umbrella -> ESP32 -> GSM/SIM -> Internet -> THIS CONTROLLER -> MySQL -> Web dashboard.
 * POST endpoints are called by the ESP32 with headers X-Device-Id and X-Device-Key.
 * GET endpoints are called by the logged-in web user (JWT) and only return their own umbrella.
 */
@RestController
@RequestMapping("/api/device")
public class HardwareController {
    private final DeviceAuthService auth; private final IngestService ingest;
    private final DeviceRepository devices; private final LocationRepository locations;

    public HardwareController(DeviceAuthService a, IngestService i, DeviceRepository d, LocationRepository l) {
        auth = a; ingest = i; devices = d; locations = l;
    }

    private Device mine(Long userId) {
        return devices.findByUserId(userId).stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No umbrella registered"));
    }

    /** POST /api/device/register (JWT) - pair umbrella, returns secret key ONCE for the ESP32. */
    @PostMapping("/register")
    public Map<String, Object> register(@AuthenticationPrincipal Long userId, @Valid @RequestBody DeviceRegisterRequest r) {
        String code = r.deviceId().toUpperCase();
        if (devices.findByDeviceCode(code).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Already registered");
        Device d = new Device();
        d.userId = userId; d.deviceCode = code; d.simNumber = r.simNumber();
        String key = auth.newKey();
        d.apiKeyHash = DeviceAuthService.sha256(key);
        devices.save(d);
        return Map.of("deviceId", code, "deviceKey", key);
    }

    @PostMapping("/location")
    public Map<String, Object> location(@RequestHeader("X-Device-Id") String id, @RequestHeader("X-Device-Key") String key,
                                        @Valid @RequestBody LocationRequest r) {
        ingest.location(auth.authenticate(id, key), r.latitude(), r.longitude(), r.accuracy(), "hardware");
        return Map.of("ok", true);
    }

    @PostMapping("/sos")
    public Map<String, Object> sos(@RequestHeader("X-Device-Id") String id, @RequestHeader("X-Device-Key") String key,
                                   @Valid @RequestBody(required = false) SosRequest r) {
        SosRequest b = r == null ? new SosRequest(null, null, null) : r;
        return ingest.sos(auth.authenticate(id, key), b.latitude(), b.longitude(), b.note(), "hardware");
    }

    @PostMapping("/haptic")
    public Map<String, Object> haptic(@RequestHeader("X-Device-Id") String id, @RequestHeader("X-Device-Key") String key,
                                      @Valid @RequestBody HapticRequest r) {
        ingest.haptic(auth.authenticate(id, key), r.type(), r.latitude(), r.longitude(), "hardware");
        return Map.of("ok", true);
    }

    @PostMapping("/heartbeat")
    public Map<String, Object> heartbeat(@RequestHeader("X-Device-Id") String id, @RequestHeader("X-Device-Key") String key,
                                         @Valid @RequestBody HeartbeatRequest r) {
        ingest.heartbeat(auth.authenticate(id, key), r.battery(), r.gsm(), r.gps(), r.firmware());
        return Map.of("ok", true);
    }

    /** GET /api/device/status (JWT) */
    @GetMapping("/status")
    public Map<String, Object> status(@AuthenticationPrincipal Long userId) {
        Device d = mine(userId);
        Map<String, Object> m = new HashMap<>();
        m.put("deviceId", d.deviceCode); m.put("connection", d.connectionStatus); m.put("gps", d.gpsStatus);
        m.put("gsm", d.gsmStatus); m.put("battery", d.batteryPercent); m.put("firmware", d.firmwareVersion);
        m.put("lastSeen", d.lastSeenAt);
        m.put("simNumber", mask(d.simNumber));        // never expose the full SIM number
        return m;
    }

    /** GET /api/device/location (JWT) */
    @GetMapping("/location")
    public Object latest(@AuthenticationPrincipal Long userId) {
        return locations.findFirstByDeviceIdOrderByRecordedAtDesc(mine(userId).id).orElse(null);
    }

    static String mask(String n) {
        if (n == null || n.length() < 4) return null;
        return "******" + n.substring(n.length() - 4);
    }
}
