package com.intellect.consentagent.controller;

import com.intellect.consentagent.model.Consent;
import com.intellect.consentagent.model.ConsentHealthFlag;
import com.intellect.consentagent.repository.ConsentRepository;
import com.intellect.consentagent.service.ConsentHealthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consents")
@CrossOrigin(origins = "*") // fine for a local demo; tighten before anything real
public class ConsentController {

    private final ConsentRepository consentRepository;
    private final ConsentHealthService healthService;

    public ConsentController(ConsentRepository consentRepository, ConsentHealthService healthService) {
        this.consentRepository = consentRepository;
        this.healthService = healthService;
    }

    @GetMapping
    public List<Consent> getAllConsents() {
        return consentRepository.findAll();
    }

    /** Triggers the agent's scan immediately - use this button in the demo instead of waiting 6 hours. */
    @PostMapping("/scan")
    public List<ConsentHealthFlag> triggerScan() {
        return healthService.scanAllConsents();
    }
}
