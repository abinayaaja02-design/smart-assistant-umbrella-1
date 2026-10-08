package com.smartumbrella.repository;

import com.smartumbrella.model.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByUserIdOrderByIsPrimaryDescPriorityAsc(Long userId);
}
