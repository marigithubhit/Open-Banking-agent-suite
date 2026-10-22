package com.intellect.consentagent.repository;

import com.intellect.consentagent.model.Consent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentRepository extends JpaRepository<Consent, String> {
}
