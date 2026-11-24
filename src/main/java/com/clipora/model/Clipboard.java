package com.clipora.model;

import java.time.OffsetDateTime;

public class Clipboard {

    private long id;
    private String code;
    private String content;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;

    // Default constructor
    public Clipboard() {
    }

    // Parameterized constructor
    public Clipboard(
            long id,
            String code,
            String content,
            OffsetDateTime createdAt,
            OffsetDateTime expiresAt) {

        this.id = id;
        this.code = code;
        this.content = content;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}