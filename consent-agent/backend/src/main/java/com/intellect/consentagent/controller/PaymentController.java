package com.intellect.consentagent.controller;

import com.intellect.consentagent.model.PaymentIntent;
import com.intellect.consentagent.repository.PaymentIntentRepository;
import com.intellect.consentagent.service.PaymentAgentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentAgentService paymentAgentService;
    private final PaymentIntentRepository paymentRepo;

    public PaymentController(PaymentAgentService paymentAgentService, PaymentIntentRepository paymentRepo) {
        this.paymentAgentService = paymentAgentService;
        this.paymentRepo = paymentRepo;
    }

    /** Step 1: parse the natural language command into a proposed payment. Does NOT move money. */
    @PostMapping("/initiate")
    public PaymentIntent initiate(@RequestBody Map<String, String> body) {
        String command = body.get("command");
        String accountId = body.getOrDefault("accountId", "ACC-001");
        return paymentAgentService.parseAndPropose(command, accountId);
    }

    /** Step 2: the human explicitly confirms - only now does the transaction execute. */
    @PostMapping("/{id}/confirm")
    public PaymentIntent confirm(@PathVariable Long id) {
        return paymentAgentService.confirm(id);
    }

    @PostMapping("/{id}/decline")
    public PaymentIntent decline(@PathVariable Long id) {
        return paymentAgentService.decline(id);
    }

    /** Full history - this is what "proves" the transactions to an audience. */
    @GetMapping("/history")
    public List<PaymentIntent> history() {
        return paymentRepo.findAllByOrderByCreatedAtDesc();
    }
}
