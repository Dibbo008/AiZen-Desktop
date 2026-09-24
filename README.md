# CareerForge - AI Resume & Career Builder

JavaFX 21 + SQLite + Jackson + PDFBox desktop app (Java 21, Maven).

## Project layout
```
pom.xml
src/main/java/com/aizen/
  MainApp.java
  model/      Person(abstract) > Applicant > Student, Experience, Education, Project,
              Certification, Skill, Resume, User
  db/         Database, DatabaseSetup (auto-creates tables)
  dao/        GenericDAO<T>, UserDAO, ResumeDAO
  controller/ Login, Main, Dashboard, Resume, CoverLetter, SavedResume, Preview
  service/    ResumeService, CoverLetterService, ApiService, JsonService, PdfService,
              AuthService, GenerationResult
  thread/     TaskManager (3-thread pool), SaveTask, ApiTask, PdfTask
  exception/  DatabaseException, ValidationException
  util/       JsonUtil, ValidationUtil, PasswordUtil, Session, SceneManager
  ui/         DynamicSection (add/remove row component)
src/main/resources/
  fxml/       login, main, dashboard, resume, cover_letter, saved_resumes, preview
  css/        style.css (light), dark.css (dark overrides)
```

## Setup
1. Install JDK 21 and Maven 3.9+.
2. IDE import: File > Open / Import > select `pom.xml` (IntelliJ: "Open as Project";
   Eclipse: Import > Existing Maven Projects; VS Code: open the folder). Set project SDK to 21.
3. (Optional) enable Gemini:
   - Windows (PowerShell): `setx AIZEN_API_KEY "your-key"` then restart the terminal/IDE
   - macOS/Linux: `export AIZEN_API_KEY="your-key"`
   Optional: `AIZEN_MODEL` overrides the model (default `gemini-1.5-flash`; use a current
   model such as `gemini-2.0-flash` if Google has retired 1.5).
   Without a key (or offline) the app uses the local generator - it never crashes.
4. Run: `mvn clean javafx:run`

Data is stored in `~/.aizen/aizen.db`.

## How the coursework requirements are met
| Requirement | Where |
|---|---|
| Inheritance & abstraction | `Person` (abstract `getRole()`) -> `Applicant` -> `Student` |
| Polymorphism / overriding | `getRole()` overridden in `Applicant`/`Student`; `ResumeService.toProfile()` returns a `Person`, `DashboardController` only calls `getRole()` |
| Method overloading | `Applicant.updateProfile(2/3/4 Strings)`, `Student.updateProfile(String,double)` |
| Encapsulation | private fields + validating setters in `Person`/`Applicant`/`Student` |
| Generics & collections | `GenericDAO<T>`, `ArrayList` children in `Resume`, `ObservableList` in TableView, `Task<T>` subclasses |
| Multithreading | every DB / PDF / HTTP call runs in `SaveTask`/`PdfTask`/`ApiTask` via `TaskManager` (fixed pool of 3); `ProgressIndicator`s bound to `task.runningProperty()`; DB init runs in `Application.init()` |
| Exception handling | `DatabaseException` (checked), `ValidationException` (unchecked); `ResumeDAO` uses try/catch/finally with rollback |
| Async REST | `ApiService.generateAsync()` uses `HttpClient.sendAsync` |
