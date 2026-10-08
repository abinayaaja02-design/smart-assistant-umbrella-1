package com.smartumbrella.service;

import com.smartumbrella.model.Device;
import com.smartumbrella.repository.DeviceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Authenticates the ESP32 using its Device ID and secret key (only the SHA-256 hash is stored). */
@Service
public class DeviceAuthService {
    private final DeviceRepository devices;
    private final SecureRandom random = new SecureRandom();
    public DeviceAuthService(DeviceRepository devices) { this.devices = devices; }

    public Device authenticate(String deviceId, String key) {
        if (deviceId == null || key == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing device headers");
        Device d = devices.findByDeviceCode(deviceId.trim().toUpperCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid device credentials"));
        if (d.apiKeyHash == null || !MessageDigest.isEqual(d.apiKeyHash.getBytes(), sha256(key).getBytes()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid device credentials");
        return d;
    }

    public String newKey() {
        byte[] b = new byte[24]; random.nextBytes(b);
        return "sau_" + HexFormat.of().formatHex(b);
    }

    public static String sha256(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
