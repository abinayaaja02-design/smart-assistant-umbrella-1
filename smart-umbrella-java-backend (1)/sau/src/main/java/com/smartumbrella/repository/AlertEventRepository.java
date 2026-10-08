package com.smartumbrella.repository;

import com.smartumbrella.model.AlertEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {
    List<AlertEvent> findTop200ByUserIdOrderByCreatedAtDesc(Long userId);
    List<AlertEvent> findTop200ByUserIdAndEventTypeOrderByCreatedAtDesc(Long userId, String type);
}
