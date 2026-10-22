package com.intellect.consentagent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consent")
public class Consent {

    @Id
    @Column(name = "consent_id")
    private String consentId;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "tpp_name")
    private String tppName;

    private String scope;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private String status;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // getters and setters
    public String getConsentId() { return consentId; }
    public void setConsentId(String consentId) { this.consentId = consentId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getTppName() { return tppName; }
    public void setTppName(String tppName) { this.tppName = tppName; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
