package com.smartumbrella.repository;

import com.smartumbrella.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceCode(String code);
    List<Device> findByUserId(Long userId);
}
