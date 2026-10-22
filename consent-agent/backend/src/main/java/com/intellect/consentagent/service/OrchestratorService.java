package com.intellect.consentagent.service;

import org.springframework.stereotype.Service;

/**
 * The Orchestrator Agent.
 *
 * For the demo this uses fast, reliable keyword routing rather than an LLM call -
 * an LLM classification step is easy to add later, but on stage you want routing
 * that never flakes. The point being proven is the multi-agent pattern itself:
 * one entry point, specialist agents underneath.
 */
@Service
public class OrchestratorService {

    private final PaymentAgentService paymentAgentService;
    private final ConsentHealthService consentHealthService;

    public OrchestratorService(PaymentAgentService paymentAgentService,
                                ConsentHealthService consentHealthService) {
        this.paymentAgentService = paymentAgentService;
        this.consentHealthService = consentHealthService;
    }

    public enum RoutedTo { PAYMENT_AGENT, CONSENT_AGENT }

    public RoutedTo classify(String command) {
        String lower = command.toLowerCase();
        boolean looksLikePayment = lower.contains("pay") || lower.contains("transfer") || lower.contains("send money");
        return looksLikePayment ? RoutedTo.PAYMENT_AGENT : RoutedTo.CONSENT_AGENT;
    }
}
