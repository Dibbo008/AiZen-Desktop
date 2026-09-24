# CareerForge — AI Resume & Career Builder

A JavaFX desktop application for building professional resumes and cover letters, backed by a local SQLite database and enhanced with Google Gemini AI. Built as a university Java coursework project demonstrating core object-oriented and concurrent programming principles.

## Features

- Secure account system with salted password hashing
- Dynamic resume builder with add/remove sections and live preview
- Classic and Modern resume templates
- AI-generated summaries and cover letters, with automatic offline fallback
- PDF and JSON export/import
- Searchable saved-resumes table with edit, duplicate, and delete
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

SQLite with seven tables — `users`, `resumes`, and five resume-child tables (`experiences`, `education`, `projects`, `certifications`, `skills`) — created automatically on first run. Child tables cascade-delete with their parent resume. All queries use `PreparedStatement`, and resume writes are transactional.

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

---

*Developed for academic coursework purposes.*