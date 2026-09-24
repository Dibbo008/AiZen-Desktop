package com.aizen.service;

import com.aizen.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Thin asynchronous client for the Gemini REST API (v1 beta generateContent).
 * The API key is read from the AIZEN_API_KEY environment variable; the model can be
 * overridden with AIZEN_MODEL (default: gemini-1.5-flash).
 */
public class ApiService {
    private static final String DEFAULT_MODEL = "gemini-flash-lite-latest";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String apiKey() {
        String key = System.getenv("AIZEN_API_KEY");
        if (key == null || key.isBlank()) {
            key = "XXXXXXXXXXXXXXXXXXXXXXXXXX";
        }
        return key.trim();
    }

    public boolean hasApiKey() {
        return !apiKey().isEmpty();
    }

    private static String model() {
        String m = System.getenv("AIZEN_MODEL");
        return (m == null || m.isBlank()) ? DEFAULT_MODEL : m.trim();
    }

    /** Non-blocking call: returns a future that completes with the generated text. */
    public CompletableFuture<String> generateAsync(String prompt) {
        if (!hasApiKey()) {
            return CompletableFuture.failedFuture(new IOException("AIZEN_API_KEY is not set"));
        }
        try {
            ObjectNode root = JsonUtil.mapper().createObjectNode();
            ArrayNode contents = root.putArray("contents");
            ObjectNode content = contents.addObject();
            content.putArray("parts").addObject().put("text", prompt);
            ObjectNode config = root.putObject("generationConfig");
            config.put("temperature", 0.7);
            config.put("maxOutputTokens", 900);

            HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT.formatted(model())))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.mapper().writeValueAsString(root)))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(ApiService::extractText);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /** Blocking convenience wrapper - only ever called from a background Task. */
    public String generate(String prompt) throws IOException, InterruptedException {
        try {
            return generateAsync(prompt).get(40, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IOException(cause.getMessage() == null ? cause.toString() : cause.getMessage(), cause);
        } catch (TimeoutException e) {
            throw new IOException("The AI service timed out.", e);
        }
    }

    private static String extractText(HttpResponse<String> response) {
        try {
            if (response.statusCode() / 100 != 2) {
                String detail = response.body();
                try {
                    JsonNode err = JsonUtil.mapper().readTree(response.body()).path("error").path("message");
                    if (!err.isMissingNode()) {
                        detail = err.asText();
                    }
                } catch (IOException ignored) {
                    // keep raw body
                }
                throw new IllegalStateException("Gemini API returned HTTP " + response.statusCode() + ": " + detail);
            }
            JsonNode root = JsonUtil.mapper().readTree(response.body());
            String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
            if (text.isBlank()) {
                throw new IllegalStateException("Gemini API returned an empty response.");
            }
            return text.trim();
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse the Gemini API response.", e);
        }
    }
}
