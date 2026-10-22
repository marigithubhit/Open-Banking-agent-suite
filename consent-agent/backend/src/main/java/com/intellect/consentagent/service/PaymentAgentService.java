package com.intellect.consentagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellect.consentagent.model.AccountBalance;
import com.intellect.consentagent.model.PaymentIntent;
import com.intellect.consentagent.repository.AccountBalanceRepository;
import com.intellect.consentagent.repository.PaymentIntentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Payment Initiation Agent.
 *
 * IMPORTANT DESIGN RULE: this service NEVER moves money on its own.
 * step 1 (parseAndPropose) only ever creates a PENDING_CONFIRMATION record.
 * step 2 (execute) only runs after a human explicitly calls confirm().
 * There is no path from natural language straight to a real transaction.
 */
@Service
public class PaymentAgentService {

    private final OllamaService ollamaService;
    private final PaymentIntentRepository paymentRepo;
    private final AccountBalanceRepository balanceRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    // simple fallback parser in case the model doesn't return clean JSON - keeps demo reliable on stage
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)");
    private static final Pattern TO_PATTERN = Pattern.compile("to\\s+([A-Za-z][A-Za-z\\s]*)");

    public PaymentAgentService(OllamaService ollamaService,
                                PaymentIntentRepository paymentRepo,
                                AccountBalanceRepository balanceRepo) {
        this.ollamaService = ollamaService;
        this.paymentRepo = paymentRepo;
        this.balanceRepo = balanceRepo;
    }

    /** Step 1: understand the command and prepare a proposal. Never executes anything. */
    public PaymentIntent parseAndPropose(String rawCommand, String accountId) {
        ParsedPayment parsed = parseCommand(rawCommand);

        PaymentIntent intent = new PaymentIntent();
        intent.setRawCommand(rawCommand);
        intent.setAccountId(accountId);
        intent.setPayeeName(parsed.payeeName);
        intent.setAmount(parsed.amount);

        validate(intent);
        return paymentRepo.save(intent);
    }

    /** Step 2: only called when the user explicitly clicks confirm in the UI. */
    public PaymentIntent confirm(Long intentId) {
        PaymentIntent intent = paymentRepo.findById(intentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment intent not found"));

        if (!"PENDING_CONFIRMATION".equals(intent.getStatus())) {
            return intent; // already resolved, do nothing
        }

        intent.setConfirmedAt(LocalDateTime.now());
        return execute(intent);
    }

    public PaymentIntent decline(Long intentId) {
        PaymentIntent intent = paymentRepo.findById(intentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment intent not found"));
        intent.setStatus("DECLINED");
        return paymentRepo.save(intent);
    }

    // ---- internals ----

    private void validate(PaymentIntent intent) {
        Optional<AccountBalance> balance = balanceRepo.findById(intent.getAccountId());
        if (intent.getAmount() == null) {
            intent.setStatus("FAILED_VALIDATION");
            intent.setValidationNote("Could not determine an amount from the command.");
        } else if (balance.isEmpty()) {
            intent.setStatus("FAILED_VALIDATION");
            intent.setValidationNote("No balance record found for this account.");
        } else if (balance.get().getAvailableAmount().compareTo(intent.getAmount()) < 0) {
            intent.setStatus("FAILED_VALIDATION");
            intent.setValidationNote("Insufficient funds: available " + balance.get().getAvailableAmount()
                    + " " + balance.get().getCurrency() + ", requested " + intent.getAmount() + ".");
        } else {
            intent.setValidationNote("Passed balance check. Awaiting user confirmation before execution.");
            // status stays PENDING_CONFIRMATION
        }
    }

    /** Simulates calling the bank's real payment rail. Swap this for a real PISP call later. */
    private PaymentIntent execute(PaymentIntent intent) {
        intent.setStatus("EXECUTED");
        intent.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        intent.setExecutedAt(LocalDateTime.now());
        return paymentRepo.save(intent);
    }

    private ParsedPayment parseCommand(String rawCommand) {
        String prompt = """
            Extract the payee name and amount from this payment command.
            Respond with ONLY valid JSON, no other text, in this exact shape:
            {"payeeName": "string", "amount": number}

            Command: "%s"
            """.formatted(rawCommand);

        try {
            String raw = ollamaService.generate(prompt);
            String jsonOnly = raw.substring(raw.indexOf('{'), raw.lastIndexOf('}') + 1);
            JsonNode node = mapper.readTree(jsonOnly);
            String payee = node.path("payeeName").asText(null);
            BigDecimal amount = node.has("amount") ? BigDecimal.valueOf(node.path("amount").asDouble()) : null;
            if (payee != null && amount != null) {
                return new ParsedPayment(payee, amount);
            }
        } catch (Exception ignored) {
            // fall through to the regex fallback below - keeps the demo working even if the
            // model returns malformed JSON
        }
        return fallbackParse(rawCommand);
    }

    private ParsedPayment fallbackParse(String rawCommand) {
        Matcher amountMatcher = AMOUNT_PATTERN.matcher(rawCommand);
        BigDecimal amount = amountMatcher.find() ? new BigDecimal(amountMatcher.group(1)) : null;

        Matcher toMatcher = TO_PATTERN.matcher(rawCommand);
        String payee = toMatcher.find() ? toMatcher.group(1).trim() : "Unknown payee";

        return new ParsedPayment(payee, amount);
    }

    private record ParsedPayment(String payeeName, BigDecimal amount) {}
}
