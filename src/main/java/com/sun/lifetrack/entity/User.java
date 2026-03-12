package com.sun.lifetrack.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

    @Data
    @Entity
    @Table(name = "user")
    public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true, length = 20)//非空，名称不重复（唯一），限长
        private String username;

        @Column(name = "password_hash", nullable = false, length = 255)
        private String passwordHash; // 后续在Service层用BCrypt加密后存入

        @Column(unique = true, nullable = false)
        private String email;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @PrePersist
        protected void onCreate() {
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }

        @PreUpdate
        protected void onUpdate() {
            this.updatedAt = LocalDateTime.now();
        }
    }

