package com.smartumbrella.controller;

import com.smartumbrella.dto.Dtos.ContactRequest;
import com.smartumbrella.model.*;
import com.smartumbrella.repository.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

/** Alerts and emergency contacts. Every query is filtered by the logged-in user's id. */
@RestController
@RequestMapping("/api")
public class UserDataController {
    private final AlertEventRepository events; private final EmergencyContactRepository contacts;
    public UserDataController(AlertEventRepository e, EmergencyContactRepository c) { events = e; contacts = c; }

    /** GET /api/alerts?type=sos|gps|haptic|device */
    @GetMapping("/alerts")
    public List<AlertEvent> alerts(@AuthenticationPrincipal Long userId, @RequestParam(required = false) String type) {
        return type == null ? events.findTop200ByUserIdOrderByCreatedAtDesc(userId)
                            : events.findTop200ByUserIdAndEventTypeOrderByCreatedAtDesc(userId, type);
    }

    @GetMapping("/emergency-contacts")
    public List<EmergencyContact> list(@AuthenticationPrincipal Long userId) {
        return contacts.findByUserIdOrderByIsPrimaryDescPriorityAsc(userId);
    }

    @PostMapping("/emergency-contacts")
    public EmergencyContact add(@AuthenticationPrincipal Long userId, @Valid @RequestBody ContactRequest r) {
        EmergencyContact c = new EmergencyContact();
        c.userId = userId; apply(c, r);
        return contacts.save(c);
    }

    @PutMapping("/emergency-contacts/{id}")
    public EmergencyContact edit(@AuthenticationPrincipal Long userId, @PathVariable Long id, @Valid @RequestBody ContactRequest r) {
        EmergencyContact c = owned(userId, id); apply(c, r);
        return contacts.save(c);
    }

    @DeleteMapping("/emergency-contacts/{id}")
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        contacts.delete(owned(userId, id));
    }

    @PostMapping("/emergency-contacts/{id}/primary") @Transactional
    public void primary(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        EmergencyContact target = owned(userId, id);
        contacts.findByUserIdOrderByIsPrimaryDescPriorityAsc(userId).forEach(c -> { c.isPrimary = false; contacts.save(c); });
        target.isPrimary = true; target.priority = 1; contacts.save(target);
    }

    private EmergencyContact owned(Long userId, Long id) {
        return contacts.findById(id).filter(c -> c.userId.equals(userId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private static void apply(EmergencyContact c, ContactRequest r) {
        c.name = r.name(); c.relationship = r.relationship(); c.phone = r.phone();
        c.priority = r.priority() == null ? 1 : r.priority();
    }
}
