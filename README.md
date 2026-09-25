# CareerForge — AI Resume & Career Builder

A JavaFX desktop application for building professional resumes and cover letters, backed by a local SQLite database and enhanced with Google Gemini AI. Built as a university Java coursework project demonstrating core object-oriented and concurrent programming principles.

## Features

- Secure account system with salted password hashing
- Dynamic resume builder with add/remove sections and live preview
- Classic and Modern resume templates
- AI-generated summaries and cover letters, with automatic offline fallback
- PDF and JSON export/import
- Searchable saved-resumes table with edit, duplicate, and delete
- Interview Prep — sector-wise chart of the most commonly asked interview topics
- To The Point — a two-question wizard that maps a target role to hand-curated learning resources (AI-assisted sector matching, zero hallucinated links)
- Job Application Tracker — log every application with company, role, status and notes, and follow it through to an offer
- Light and dark themes

## Tech Stack

Java 21 · JavaFX 21 · Maven · SQLite (JDBC) · Jackson · Apache PDFBox · Google Gemini API

## Project Structure

**Requirements:** JDK 21, Maven 3.9+ (or an IDE's bundled Maven)

```bash
mvn clean javafx:run
```

On first launch, CareerForge creates its SQLite database at `~/.aizen/aizen.db`.

## Enabling AI Generation (Optional)

The app works fully offline by default. To enable live Gemini-powered generation:

1. Get a free API key from [Google AI Studio](https://aistudio.google.com/apikey).
2. Set the environment variable `AIZEN_API_KEY` to your key.
3. Optionally set `AIZEN_MODEL` to override the default model.

Without a key, or if a request fails, the app automatically falls back to a local text generator.

## Database

SQLite with nine tables — `users`, `resumes`, five resume-child tables (`experiences`, `education`, `projects`, `certifications`, `skills`), and `applications` (for the Job Application Tracker) — created automatically on first run. Every child table cascade-deletes with its parent (`resumes` cascade from `users`; the five resume-child tables cascade from `resumes`; `applications` cascades from `users`). All queries use `PreparedStatement`, and resume writes are transactional.

## Academic Concepts Demonstrated

| Concept | Implementation |
|---|---|
| Abstraction & inheritance | `Person` (abstract) → `Applicant` → `Student` |
| Polymorphism | Overridden `getRole()`; `ResumeService.toProfile()` |
| Method overloading | `Applicant.updateProfile(...)` in multiple forms |
| Encapsulation | Validated setters throughout `model/` |
| Generics & collections | `GenericDAO<T>`, `ObservableList`, `Task<T>` |
| Multithreading | 3-thread pool via `TaskManager`, keeping the UI responsive |
| Exception handling | Checked/unchecked exceptions with transactional rollback |
| Asynchronous networking | `HttpClient.sendAsync()` with `CompletableFuture` |
| Networking & JSON parsing | `ApiService` — real HTTP POST to the Gemini REST API, response parsed with Jackson (`JsonNode`) |

---

*Developed for academic coursework purposes.*