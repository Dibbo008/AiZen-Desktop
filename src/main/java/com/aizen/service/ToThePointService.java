package com.aizen.service;

import com.aizen.util.ValidationUtil;

import java.util.List;

/**
 * Powers the "To The Point" feature: maps whatever role the user types into one of the
 * fixed, curated career sectors. Gemini is only ever asked to pick a name from a closed
 * list - it never invents a resource or a link, so the result can't hallucinate a broken URL.
 * When the API key is missing or the call fails, a simple local keyword match takes over.
 */
public class ToThePointService {
    private final ApiService api = new ApiService();

    public GenerationResult classifySector(String roleText) {
        List<String> sectors = InterviewQuestionData.sectors();
        if (api.hasApiKey()) {
            try {
                String raw = api.generate(buildPrompt(roleText, sectors));
                String matched = matchSector(raw, sectors);
                if (matched != null) {
                    return new GenerationResult(matched, true, "Matched with Gemini.");
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception ignored) {
                // Fall through to the local keyword match below.
            }
        }
        return new GenerationResult(localMatch(roleText, sectors), false,
                "Matched locally - AI unavailable.");
    }

    private static String buildPrompt(String role, List<String> sectors) {
        return "A job seeker wants to apply for this role: \"" + role.trim() + "\".\n"
                + "Pick exactly ONE category from this fixed list that best matches the role, "
                + "and reply with ONLY that category name and nothing else, no punctuation, no explanation:\n"
                + String.join("\n", sectors);
    }

    /** Matches the AI's raw reply back to one of the fixed sector names. */
    private static String matchSector(String raw, List<String> sectors) {
        if (ValidationUtil.isBlank(raw)) {
            return null;
        }
        String cleaned = raw.trim().replaceAll("[.\"']", "");
        for (String s : sectors) {
            if (s.equalsIgnoreCase(cleaned)) {
                return s;
            }
        }
        for (String s : sectors) {
            if (cleaned.toLowerCase().contains(s.toLowerCase())) {
                return s;
            }
        }
        return null;
    }

    /** Small local keyword fallback so the feature still works without an API key. */
    private static String localMatch(String role, List<String> sectors) {
        String r = role == null ? "" : role.toLowerCase();
        if (containsAny(r, "develop", "engineer", "programmer", "software", "coder", "coding")) {
            return "Software Engineering";
        }
        if (containsAny(r, "data", "machine learning", " ml", "ai ", "analyst", "scientist")) {
            return "Data Science / AI";
        }
        if (containsAny(r, "market", "seo", "content", "brand")) {
            return "Marketing";
        }
        if (containsAny(r, "sale", "business development", "account executive")) {
            return "Sales";
        }
        if (containsAny(r, "financ", "account", "audit", "bookkeep")) {
            return "Finance & Accounting";
        }
        if (containsAny(r, "hr", "human resource", "recruit", "talent")) {
            return "Human Resources";
        }
        if (containsAny(r, "support", "customer service", "customer care")) {
            return "Customer Service";
        }
        if (containsAny(r, "project manager", "product manager", "scrum", "program manager")) {
            return "Project Management";
        }
        return sectors.isEmpty() ? "Software Engineering" : sectors.get(0);
    }

    private static boolean containsAny(String text, String... needles) {
        for (String n : needles) {
            if (text.contains(n)) {
                return true;
            }
        }
        return false;
    }
}