package com.smartumbrella.repository;

import com.smartumbrella.model.LocationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<LocationRecord, Long> {
    Optional<LocationRecord> findFirstByDeviceIdOrderByRecordedAtDesc(Long deviceId);
}
