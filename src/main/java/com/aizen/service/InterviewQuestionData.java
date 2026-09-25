package com.aizen.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Static, curated reference data mapping a job sector to the interview topics
 * most commonly associated with it, each with a relative importance score (1-10).
 *
 * This is a general guide compiled from commonly reported interview themes,
 * not statistics from any single survey or dataset.
 */
public final class InterviewQuestionData {

    public record QuestionStat(String topic, int importance) {
    }

    private static final Map<String, List<QuestionStat>> SECTORS = new LinkedHashMap<>();

    static {
        SECTORS.put("Software Engineering", List.of(
                new QuestionStat("Data Structures & Algorithms", 10),
                new QuestionStat("Problem Solving / Coding Challenge", 9),
                new QuestionStat("System Design", 8),
                new QuestionStat("OOP Concepts", 8),
                new QuestionStat("Past Project Experience", 7),
                new QuestionStat("Debugging & Troubleshooting", 7),
                new QuestionStat("SQL & Databases", 6),
                new QuestionStat("Version Control (Git)", 6),
                new QuestionStat("Teamwork & Collaboration", 6),
                new QuestionStat("Career Goals / Why This Role", 5)
        ));
        SECTORS.put("Data Science / AI", List.of(
                new QuestionStat("Statistics & Probability", 10),
                new QuestionStat("Machine Learning Fundamentals", 9),
                new QuestionStat("Python / Data Tooling", 8),
                new QuestionStat("SQL & Data Wrangling", 8),
                new QuestionStat("Model Evaluation Metrics", 7),
                new QuestionStat("Past Project / Case Study Walkthrough", 7),
                new QuestionStat("Data Visualization", 6),
                new QuestionStat("Communicating Findings to Non-Technical Audiences", 6),
                new QuestionStat("Handling Messy / Missing Data", 5),
                new QuestionStat("Career Goals / Why This Role", 5)
        ));
        SECTORS.put("Marketing", List.of(
                new QuestionStat("Campaign Strategy & Planning", 9),
                new QuestionStat("Target Audience & Positioning", 9),
                new QuestionStat("Analytics & KPI Tracking", 8),
                new QuestionStat("Social Media / Digital Channels", 8),
                new QuestionStat("Brand Storytelling", 7),
                new QuestionStat("Budget Management", 6),
                new QuestionStat("Past Campaign Results", 7),
                new QuestionStat("Collaboration with Sales/Design", 6),
                new QuestionStat("Handling Underperforming Campaigns", 5),
                new QuestionStat("Career Goals / Why This Role", 5)
        ));
        SECTORS.put("Sales", List.of(
                new QuestionStat("Handling Objections", 10),
                new QuestionStat("Sales Process & Pipeline Management", 9),
                new QuestionStat("Meeting / Exceeding Targets", 9),
                new QuestionStat("Negotiation Skills", 8),
                new QuestionStat("Client Relationship Building", 8),
                new QuestionStat("Cold Outreach / Prospecting", 7),
                new QuestionStat("Handling Rejection", 6),
                new QuestionStat("Product Knowledge", 6),
                new QuestionStat("CRM Tools Experience", 5),
                new QuestionStat("Career Goals / Why This Role", 5)
        ));
        SECTORS.put("Finance & Accounting", List.of(
                new QuestionStat("Financial Statement Analysis", 9),
                new QuestionStat("Excel / Spreadsheet Proficiency", 9),
                new QuestionStat("Attention to Detail & Accuracy", 8),
                new QuestionStat("Regulatory / Compliance Knowledge", 8),
                new QuestionStat("Budgeting & Forecasting", 7),
                new QuestionStat("Handling Discrepancies / Audits", 7),
                new QuestionStat("Ethics & Confidentiality", 6),
                new QuestionStat("Software Tools (ERP, QuickBooks, etc.)", 6),
                new QuestionStat("Working Under Deadlines", 5),
                new QuestionStat("Career Goals / Why This Role", 5)
        ));
        SECTORS.put("Human Resources", List.of(
                new QuestionStat("Conflict Resolution", 9),
                new QuestionStat("Recruitment & Interviewing", 9),
                new QuestionStat("Employee Relations", 8),
                new QuestionStat("Labour Law / Compliance Awareness", 7),
                new QuestionStat("Confidentiality & Ethics", 7),
                new QuestionStat("Performance Management", 6),
                new QuestionStat("Onboarding Process Design", 6),
                new QuestionStat("Handling Difficult Conversations", 7),
                new QuestionStat("HR Software / ATS Tools", 5),
                new QuestionStat("Career Goals / Why This Role", 5)
        ));
        SECTORS.put("Customer Service", List.of(
                new QuestionStat("Handling Difficult Customers", 10),
                new QuestionStat("Communication Skills", 9),
                new QuestionStat("Problem Resolution Under Pressure", 8),
                new QuestionStat("Patience & Empathy", 8),
                new QuestionStat("Product / Service Knowledge", 7),
                new QuestionStat("Multitasking", 6),
                new QuestionStat("Following Procedures / Scripts", 5),
                new QuestionStat("Teamwork with Other Departments", 6),
                new QuestionStat("Handling Complaints & Escalations", 7),
                new QuestionStat("Career Goals / Why This Role", 5)
        ));
        SECTORS.put("Project Management", List.of(
                new QuestionStat("Planning & Scheduling", 9),
                new QuestionStat("Risk Management", 8),
                new QuestionStat("Stakeholder Communication", 9),
                new QuestionStat("Handling Scope Creep", 7),
                new QuestionStat("Team Leadership", 8),
                new QuestionStat("Agile / Scrum Methodology", 7),
                new QuestionStat("Budget & Resource Management", 6),
                new QuestionStat("Conflict Resolution", 6),
                new QuestionStat("Tools (Jira, Trello, MS Project)", 5),
                new QuestionStat("Career Goals / Why This Role", 5)
        ));
    }

    private InterviewQuestionData() {
    }

    /** Sector names in a stable, sensible display order. */
    public static List<String> sectors() {
        return List.copyOf(SECTORS.keySet());
    }

    /** Up to 10 topics for the sector, sorted by importance (highest first). */
    public static List<QuestionStat> topTen(String sector) {
        return SECTORS.getOrDefault(sector, List.of()).stream()
                .sorted(Comparator.comparingInt(QuestionStat::importance).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
}