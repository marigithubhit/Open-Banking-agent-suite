package com.intellect.consentagent.controller;

import com.intellect.consentagent.service.OrchestratorService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "*")
public class OrchestratorController {

    private final OrchestratorService orchestratorService;

    public OrchestratorController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    /** Lets the dashboard show "routed to: PAYMENT_AGENT" before calling the specialist endpoint. */
    @PostMapping("/classify")
    public Map<String, String> classify(@RequestBody Map<String, String> body) {
        OrchestratorService.RoutedTo routedTo = orchestratorService.classify(body.get("command"));
        return Map.of("routedTo", routedTo.name());
    }
}
