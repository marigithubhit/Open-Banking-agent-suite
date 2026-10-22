package com.intellect.consentagent.repository;

import com.intellect.consentagent.model.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, String> {
}
