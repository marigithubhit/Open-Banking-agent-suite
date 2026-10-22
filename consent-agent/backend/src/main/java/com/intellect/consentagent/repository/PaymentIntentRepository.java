package com.intellect.consentagent.repository;

import com.intellect.consentagent.model.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {
    List<PaymentIntent> findAllByOrderByCreatedAtDesc();
}
