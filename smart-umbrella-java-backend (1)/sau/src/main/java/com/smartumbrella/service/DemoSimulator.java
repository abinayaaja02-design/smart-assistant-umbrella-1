package com.smartumbrella.service;

import com.smartumbrella.model.Device;
import com.smartumbrella.repository.DeviceRepository;
import com.smartumbrella.repository.LocationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Random;

/**
 * DEMO MODE - mock hardware data layer. Enabled with app.demo-mode=true.
 * Set it to false once the real ESP32 is posting data; nothing else changes.
 */
@Component
@ConditionalOnProperty(name = "app.demo-mode", havingValue = "true")
public class DemoSimulator {
    private static final String[] HAPTIC = {"obstacle", "left", "right", "stop", "danger", "emergency"};
    private final DeviceRepository devices;
    private final LocationRepository locations;
    private final IngestService ingest;
    private final Random rnd = new Random();

    public DemoSimulator(DeviceRepository d, LocationRepository l, IngestService i) { devices = d; locations = l; ingest = i; }

    @Scheduled(fixedRate = 60_000)
    public void tick() {
        for (Device d : devices.findAll()) {
            var last = locations.findFirstByDeviceIdOrderByRecordedAtDesc(d.id);
            double lat = last.map(x -> x.latitude).orElse(13.0827) + (rnd.nextDouble() - 0.5) * 0.001;
            double lng = last.map(x -> x.longitude).orElse(80.2707) + (rnd.nextDouble() - 0.5) * 0.001;
            ingest.location(d, lat, lng, 6.0, "demo");
            ingest.heartbeat(d, 55 + rnd.nextInt(45), "connected", "fixed", null);
            if (rnd.nextInt(4) == 0) ingest.haptic(d, HAPTIC[rnd.nextInt(HAPTIC.length)], lat, lng, "demo");
        }
    }
}
