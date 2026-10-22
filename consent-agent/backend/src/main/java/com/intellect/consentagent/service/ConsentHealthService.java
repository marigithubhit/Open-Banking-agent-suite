package com.intellect.consentagent.service;

import com.intellect.consentagent.model.Consent;
import com.intellect.consentagent.model.ConsentHealthFlag;
import com.intellect.consentagent.repository.ConsentHealthFlagRepository;
import com.intellect.consentagent.repository.ConsentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The "Consent Watcher": simple, explainable rules - not a black-box model.
 * Every rule here is something you can say out loud to an auditor and have it make sense.
 */
@Service
public class ConsentHealthService {

    private static final int STALE_DAYS_THRESHOLD = 60;
    private static final int EXPIRING_SOON_DAYS = 14;
    private static final int WIDE_SCOPE_PERMISSION_COUNT = 3;

    private final ConsentRepository consentRepository;
    private final ConsentHealthFlagRepository flagRepository;
    private final OllamaService ollamaService;

    public ConsentHealthService(ConsentRepository consentRepository,
                                 ConsentHealthFlagRepository flagRepository,
                                 OllamaService ollamaService) {
        this.consentRepository = consentRepository;
        this.flagRepository = flagRepository;
        this.ollamaService = ollamaService;
    }

    /** Runs every 6 hours in production; call /api/consents/scan to trigger on demand for the demo. */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public void scheduledScan() {
        scanAllConsents();
    }

    public List<ConsentHealthFlag> scanAllConsents() {
        for (Consent c : consentRepository.findAll()) {
            checkStale(c);
            checkScope(c);
            checkExpiringSoon(c);
        }
        return flagRepository.findByResolvedFalse();
    }

    private void checkStale(Consent c) {
        if (c.getLastUsedAt() == null) return;
        long daysUnused = Duration.between(c.getLastUsedAt(), LocalDateTime.now()).toDays();
        if (daysUnused >= STALE_DAYS_THRESHOLD) {
            raiseFlag(c, "STALE", daysUnused > 90 ? "HIGH" : "MEDIUM",
                    "Unused for " + daysUnused + " days. Granted on " + c.getGrantedAt().toLocalDate() + ".");
        }
    }

    private void checkScope(Consent c) {
        int permissionCount = c.getScope().split(",").length;
        if (permissionCount >= WIDE_SCOPE_PERMISSION_COUNT) {
            raiseFlag(c, "SCOPE_MISMATCH", "MEDIUM",
                    "Consent scope includes " + permissionCount + " permissions (" + c.getScope() + ").");
        }
    }

    private void checkExpiringSoon(Consent c) {
        if (c.getExpiresAt() == null) return;
        long daysToExpiry = Duration.between(LocalDateTime.now(), c.getExpiresAt()).toDays();
        if (daysToExpiry >= 0 && daysToExpiry <= EXPIRING_SOON_DAYS) {
            raiseFlag(c, "EXPIRING_SOON", "LOW",
                    "Expires in " + daysToExpiry + " days.");
        }
    }

    private void raiseFlag(Consent c, String flagType, String severity, String detail) {
        boolean alreadyFlagged = flagRepository.findByConsentId(c.getConsentId()).stream()
                .anyMatch(f -> f.getFlagType().equals(flagType) && !f.isResolved());
        if (alreadyFlagged) return;

        ConsentHealthFlag flag = new ConsentHealthFlag();
        flag.setConsentId(c.getConsentId());
        flag.setFlagType(flagType);
        flag.setSeverity(severity);
        flag.setDetail(detail);
        flag.setExplanation(ollamaService.explainFlag(c.getTppName(), flagType, detail));
        flagRepository.save(flag);
    }
}
