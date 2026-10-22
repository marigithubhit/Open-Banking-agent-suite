package com.intellect.consentagent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consent_health_flag")
public class ConsentHealthFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consent_id")
    private String consentId;

    @Column(name = "flag_type")
    private String flagType;   // STALE, SCOPE_MISMATCH, EXPIRING_SOON, ACCESS_SPIKE

    private String severity;   // LOW, MEDIUM, HIGH

    private String detail;      // raw facts fed to the LLM
    private String explanation; // plain-English output from Ollama

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean resolved = false;

    public Long getId() { return id; }
    public String getConsentId() { return consentId; }
    public void setConsentId(String consentId) { this.consentId = consentId; }
    public String getFlagType() { return flagType; }
    public void setFlagType(String flagType) { this.flagType = flagType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
}
