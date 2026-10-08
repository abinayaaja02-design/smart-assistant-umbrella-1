package com.smartumbrella.controller;

import com.smartumbrella.dto.Dtos.*;
import com.smartumbrella.model.*;
import com.smartumbrella.repository.*;
import com.smartumbrella.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final DeviceRepository devices;
    private final EmergencyContactRepository contacts; private final PasswordEncoder encoder; private final JwtService jwt;

    public AuthController(UserRepository u, DeviceRepository d, EmergencyContactRepository c, PasswordEncoder p, JwtService j) {
        users = u; devices = d; contacts = c; encoder = p; jwt = j;
    }

    /** POST /api/auth/register - creates user, links umbrella and primary emergency contact. */
    @PostMapping("/register") @Transactional
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest r) {
        if (users.findByEmailIgnoreCase(r.email()).isPresent() || users.findByMobile(r.mobile()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already exists");
        if (devices.findByDeviceCode(r.deviceId().toUpperCase()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Umbrella already registered");
        User u = new User();
        u.fullName = r.fullName(); u.mobile = r.mobile(); u.email = r.email().toLowerCase();
        u.passwordHash = encoder.encode(r.password());   // BCrypt hashing
        users.save(u);
        Device d = new Device();
        d.userId = u.id; d.deviceCode = r.deviceId().toUpperCase();
        d.simNumber = (r.simNumber() == null || r.simNumber().isBlank()) ? null : r.simNumber();
        devices.save(d);
        EmergencyContact c = new EmergencyContact();
        c.userId = u.id; c.name = r.emergencyContactName(); c.phone = r.emergencyContactNumber();
        c.relationship = "Emergency contact"; c.isPrimary = true;
        contacts.save(c);
        return Map.of("token", jwt.issue(u.id), "name", u.fullName);
    }

    /** POST /api/auth/login - identifier can be email or mobile number. */
    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest r) {
        String id = r.identifier().trim();
        User u = (id.contains("@") ? users.findByEmailIgnoreCase(id) : users.findByMobile(id))
            .filter(x -> encoder.matches(r.password(), x.passwordHash))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect credentials"));
        return Map.of("token", jwt.issue(u.id), "name", u.fullName);
    }
}
