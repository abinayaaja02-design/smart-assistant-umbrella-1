package com.smartumbrella.model;

import jakarta.persistence.*;

@Entity @Table(name = "emergency_contacts")
public class EmergencyContact {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(name = "user_id") public Long userId;
    public String name;
    public String relationship;
    public String phone;
    public Integer priority = 1;
    @Column(name = "is_primary") public Boolean isPrimary = false;
}
