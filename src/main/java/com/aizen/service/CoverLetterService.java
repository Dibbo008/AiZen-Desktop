package com.aizen.service;

import com.aizen.model.Experience;
import com.aizen.model.Resume;
import com.aizen.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

/** Generates cover letters with Gemini, or locally when the API is unavailable. */
public class CoverLetterService {
    private final ApiService api = new ApiService();

    public GenerationResult generate(String applicantName, String role, String company, String tone,
                                     Resume resume, String notes) {
        String note;
        if (api.hasApiKey()) {
            try {
                String text = api.generate(buildPrompt(applicantName, role, company, tone, resume, notes));
                if (!ValidationUtil.isBlank(text)) {
                    return new GenerationResult(text.trim(), true, "Cover letter generated with Gemini.");
                }
                note = "AI returned an empty answer";
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                note = "the request was interrupted";
            } catch (Exception e) {
                note = "AI unavailable (" + e.getMessage() + ")";
            }
        } else {
            note = "AIZEN_API_KEY is not set";
        }
        return new GenerationResult(localLetter(applicantName, role, company, tone, resume, notes),
                false, "Local template used: " + note + ".");
    }

    private static String buildPrompt(String name, String role, String company, String tone,
                                      Resume resume, String notes) {
        StringBuilder p = new StringBuilder();
        p.append("Write a cover letter in a ").append(tone.toLowerCase())
                .append(" tone for ").append(name)
                .append(" applying for the position of ").append(role)
                .append(" at ").append(company).append(".\n")
                .append("Requirements: 3 to 4 short paragraphs, plain text, start with a greeting to the hiring manager, ")
                .append("end with a sign-off using the applicant's name, no placeholders like [Your Name].\n");
        if (resume != null) {
            if (!ValidationUtil.isBlank(resume.getSummary())) {
                p.append("Applicant summary: ").append(resume.getSummary().trim()).append('\n');
            }
            List<String> skills = new ArrayList<>();
            resume.getSkills().forEach(s -> {
                if (!ValidationUtil.isBlank(s.getName())) {
                    skills.add(s.getName().trim());
                }
            });
            if (!skills.isEmpty()) {
                p.append("Skills: ").append(String.join(", ", skills)).append('\n');
            }
            for (Experience e : resume.getExperiences()) {
                p.append("Experience: ").append(e.getRole()).append(" at ").append(e.getCompany())
                        .append(". ").append(e.getDescription()).append('\n');
            }
        }
        if (!ValidationUtil.isBlank(notes)) {
            p.append("Extra points to mention: ").append(notes.trim()).append('\n');
        }
        return p.toString();
    }

    static String localLetter(String name, String role, String company, String tone, Resume resume, String notes) {
        String opening = switch (tone) {
            case "Confident" -> ("I am excited to apply for the %s position at %s. With a proven ability to deliver "
                    + "results, I am certain I can make an immediate impact on your team.").formatted(role, company);
            case "Friendly" -> ("I was genuinely happy to see the %s opening at %s, and I would love the chance "
                    + "to be part of your team.").formatted(role, company);
            default -> ("I am writing to express my interest in the %s position at %s. I believe my background "
                    + "and skills make me a strong candidate for this opportunity.").formatted(role, company);
        };

        StringBuilder background = new StringBuilder();
        if (resume != null && !resume.getExperiences().isEmpty()) {
            Experience e = resume.getExperiences().get(0);
            background.append("In my most recent role");
            if (!ValidationUtil.isBlank(e.getRole())) {
                background.append(" as ").append(e.getRole().trim());
            }
            if (!ValidationUtil.isBlank(e.getCompany())) {
                background.append(" at ").append(e.getCompany().trim());
            }
            background.append(", I developed practical skills that transfer directly to this position.");
            if (!ValidationUtil.isBlank(e.getDescription())) {
                background.append(" ").append(e.getDescription().trim().split("\\R")[0].trim());
                if (!background.toString().endsWith(".")) {
                    background.append('.');
                }
            }
        } else if (resume != null && !ValidationUtil.isBlank(resume.getSummary())) {
            background.append(resume.getSummary().trim());
        } else {
            background.append("I bring a strong work ethic, a quick learning curve and a genuine passion for "
                    + "doing excellent work.");
        }

        String skillsPara = "";
        if (resume != null && !resume.getSkills().isEmpty()) {
            List<String> skills = new ArrayList<>();
            resume.getSkills().forEach(s -> {
                if (!ValidationUtil.isBlank(s.getName())) {
                    skills.add(s.getName().trim());
                }
            });
            if (!skills.isEmpty()) {
                skillsPara = "My core strengths include "
                        + ResumeService.humanJoin(skills.subList(0, Math.min(6, skills.size())))
                        + ", which I apply with care and attention to detail.";
            }
        }

        String notesPara = ValidationUtil.isBlank(notes) ? ""
                : "I would also like to highlight the following: " + notes.trim();

        String interest = switch (tone) {
            case "Confident" -> "%s is exactly the kind of organisation where I can grow while raising the bar for the team."
                    .formatted(company);
            case "Friendly" -> "I really admire what %s is doing, and I would be thrilled to contribute my energy and ideas."
                    .formatted(company);
            default -> "I am drawn to %s because of its reputation and the opportunity to contribute meaningfully."
                    .formatted(company);
        };

        String closing = switch (tone) {
            case "Confident" -> "I would welcome the opportunity to discuss how I can help %s succeed. I look forward to speaking with you."
                    .formatted(company);
            case "Friendly" -> "Thank you so much for your time - I would love to chat more about how I can help!";
            default -> "Thank you for considering my application. I look forward to the opportunity to discuss my qualifications further.";
        };
        String signOff = switch (tone) {
            case "Friendly" -> "Warm regards,";
            case "Confident" -> "Best regards,";
            default -> "Sincerely,";
        };

        List<String> paragraphs = new ArrayList<>();
        paragraphs.add("Dear Hiring Manager,");
        paragraphs.add(opening);
        paragraphs.add(background.toString());
        if (!skillsPara.isEmpty()) {
            paragraphs.add(skillsPara);
        }
        if (!notesPara.isEmpty()) {
            paragraphs.add(notesPara);
        }
        paragraphs.add(interest + " " + closing);
        paragraphs.add(signOff + "\n" + name);
        return String.join("\n\n", paragraphs);
    }
}
