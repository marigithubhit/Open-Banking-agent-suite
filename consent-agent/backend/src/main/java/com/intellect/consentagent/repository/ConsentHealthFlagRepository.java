package com.intellect.consentagent.repository;

import com.intellect.consentagent.model.ConsentHealthFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsentHealthFlagRepository extends JpaRepository<ConsentHealthFlag, Long> {
    List<ConsentHealthFlag> findByResolvedFalse();
    List<ConsentHealthFlag> findByConsentId(String consentId);
}
