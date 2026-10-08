package com.smartumbrella.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(name = "full_name") public String fullName;
    public String mobile;
    public String email;
    @Column(name = "password_hash") public String passwordHash;
    @Column(name = "created_at", insertable = false, updatable = false) public Instant createdAt;
}
