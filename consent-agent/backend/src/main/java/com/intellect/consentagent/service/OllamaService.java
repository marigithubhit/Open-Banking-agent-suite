package com.intellect.consentagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Talks to a locally running Ollama instance (http://localhost:11434 by default).
 * Nothing here leaves the machine - that's the compliance story for the demo.
 */
@Service
public class OllamaService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model}")
    private String model;

    /**
     * Turns a raw, factual flag ("consent unused for 96 days, scope has 4 permissions")
     * into a plain-English explanation a non-technical customer or auditor can read.
     */
    public String explainFlag(String tppName, String flagType, String rawDetail) {
        String prompt = """
            You are a banking assistant explaining an Open Banking data-access issue
            to a bank customer or compliance officer. Be concise (2-3 sentences),
            plain-English, no jargon, no markdown. State the fact and why it matters.

            Third party: %s
            Issue type: %s
            Raw facts: %s

            Write the explanation now:
            """.formatted(tppName, flagType, rawDetail);

        try {
            return generate(prompt);
        } catch (Exception e) {
            // fall back gracefully so the demo never dies on a network hiccup
            return "This %s consent triggered a %s flag: %s".formatted(tppName, flagType, rawDetail);
        }
    }

    /** Generic call to the local Ollama model. Reused by both the Consent Agent and Payment Agent. */
    public String generate(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/generate", request, String.class);
        try {
            JsonNode json = mapper.readTree(response.getBody());
            return json.path("response").asText().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Ollama response", e);
        }
    }
}
