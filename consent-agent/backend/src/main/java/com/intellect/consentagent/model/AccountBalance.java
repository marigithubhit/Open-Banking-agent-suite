package com.intellect.consentagent.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "account_balance")
public class AccountBalance {

    @Id
    @Column(name = "account_id")
    private String accountId;

    @Column(name = "available_amount")
    private BigDecimal availableAmount;

    private String currency;

    public String getAccountId() { return accountId; }
    public BigDecimal getAvailableAmount() { return availableAmount; }
    public String getCurrency() { return currency; }
}
