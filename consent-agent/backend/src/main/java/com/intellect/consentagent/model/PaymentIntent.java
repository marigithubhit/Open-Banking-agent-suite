package com.intellect.consentagent.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_intent")
public class PaymentIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_command")
    private String rawCommand;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "payee_name")
    private String payeeName;

    private BigDecimal amount;
    private String currency = "INR";

    private String status = "PENDING_CONFIRMATION";
    // PENDING_CONFIRMATION, DECLINED, EXECUTED, FAILED_VALIDATION

    @Column(name = "validation_note")
    private String validationNote;

    @Column(name = "transaction_ref")
    private String transactionRef;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    public Long getId() { return id; }
    public String getRawCommand() { return rawCommand; }
    public void setRawCommand(String rawCommand) { this.rawCommand = rawCommand; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getValidationNote() { return validationNote; }
    public void setValidationNote(String validationNote) { this.validationNote = validationNote; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
}
