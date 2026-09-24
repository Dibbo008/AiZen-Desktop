package com.aizen.service;

import com.aizen.dao.ResumeDAO;
import com.aizen.exception.DatabaseException;
import com.aizen.model.Applicant;
import com.aizen.model.Certification;
import com.aizen.model.Education;
import com.aizen.model.Experience;
import com.aizen.model.Person;
import com.aizen.model.Project;
import com.aizen.model.Resume;
import com.aizen.model.Skill;
import com.aizen.model.Student;
import com.aizen.util.ValidationUtil;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Business logic for resumes: validation, persistence, text preview and AI summary. */
public class ResumeService {
    private static final Pattern NUMBER = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    private final ResumeDAO dao = new ResumeDAO();
    private final ApiService api = new ApiService();

    // -------------------------------------------------------------- validation

    public void validate(Resume r) {
        ValidationUtil.requireNonBlank("Resume title", r.getTitle());
        ValidationUtil.requireMaxLength("Resume title", r.getTitle(), 100);
        ValidationUtil.requireNonBlank("Full name", r.getFullName());
        ValidationUtil.requireEmail("E-mail", r.getEmail());
        ValidationUtil.optionalPhone("Phone", r.getPhone());
        ValidationUtil.requireMaxLength("Summary", r.getSummary(), 2000);
    }

    // ------------------------------------------------------------- persistence

    public Resume save(Resume r, int userId) throws DatabaseException {
        validate(r);
        r.setUserId(userId);
        return r.getId() > 0 ? dao.update(r) : dao.insert(r);
    }

    public List<Resume> listForUser(int userId) throws DatabaseException {
        return dao.findByUserId(userId);
    }

    public int countForUser(int userId) throws DatabaseException {
        return dao.countByUserId(userId);
    }

    public boolean delete(int resumeId) throws DatabaseException {
        return dao.delete(resumeId);
    }

    // ------------------------------------------------------ OOP: polymorphism

    /**
     * Maps a resume to a Person subtype. Someone who is still studying becomes a Student,
     * everybody else an Applicant; callers only rely on the polymorphic getRole().
     */
    public Person toProfile(Resume r) {
        Applicant applicant;
        Education latest = r.getEducations().isEmpty() ? null : r.getEducations().get(0);
        if (latest != null && isStudying(latest)) {
            Student student = new Student();
            student.updateProfile(latest.getInstitution(), parseCgpa(latest.getGrade())); // overload (String,double)
            applicant = student;
        } else {
            applicant = new Applicant();
        }
        applicant.setId(r.getId());
        applicant.updateProfile(r.getFullName(), r.getEmail(), r.getPhone());               // overload (3 x String)
        applicant.updateProfile(r.getJobTitle(), r.getLocation(), r.getLinkedin(), r.getSummary()); // overload (4 x String)
        return applicant;
    }

    private static boolean isStudying(Education e) {
        String y = e.getYear().toLowerCase(Locale.ROOT);
        if (y.contains("present") || y.contains("expected") || y.contains("ongoing") || y.contains("current")) {
            return true;
        }
        Matcher m = Pattern.compile("(19|20)\\d{2}").matcher(y);
        int last = -1;
        while (m.find()) {
            last = Integer.parseInt(m.group());
        }
        return last >= Year.now().getValue();
    }

    private static double parseCgpa(String grade) {
        Matcher m = NUMBER.matcher(grade == null ? "" : grade);
        if (m.find()) {
            double v = Double.parseDouble(m.group(1));
            return (v >= 0 && v <= 10) ? v : 0;
        }
        return 0;
    }

    // ----------------------------------------------------------------- preview

    public String buildPreview(Resume r) {
        boolean modern = "Modern".equalsIgnoreCase(r.getTemplate());
        StringBuilder sb = new StringBuilder();
        String name = ValidationUtil.isBlank(r.getFullName()) ? "YOUR NAME" : r.getFullName().trim().toUpperCase();

        List<String> contact = new ArrayList<>();
        addIfPresent(contact, r.getEmail());
        addIfPresent(contact, r.getPhone());
        addIfPresent(contact, r.getLocation());
        addIfPresent(contact, r.getLinkedin());

        if (modern) {
            sb.append("==== ").append(name).append(" ====\n");
            if (!ValidationUtil.isBlank(r.getJobTitle())) {
                sb.append(r.getJobTitle().trim()).append('\n');
            }
            sb.append(String.join("  //  ", contact)).append("\n");
        } else {
            sb.append(name).append('\n');
            if (!ValidationUtil.isBlank(r.getJobTitle())) {
                sb.append(r.getJobTitle().trim()).append('\n');
            }
            sb.append(String.join(" | ", contact)).append("\n");
        }

        if (!ValidationUtil.isBlank(r.getSummary())) {
            section(sb, "Summary", modern);
            sb.append(r.getSummary().trim()).append('\n');
        }

        List<Experience> exps = r.getExperiences();
        if (!exps.isEmpty()) {
            section(sb, "Experience", modern);
            for (Experience e : exps) {
                String head = join(" - ", e.getRole(), e.getCompany());
                String dates = dateRange(e.getStartDate(), e.getEndDate());
                sb.append(modern ? "* " : "").append(head);
                if (!dates.isEmpty()) {
                    sb.append(modern ? "  |  " : "  (").append(dates).append(modern ? "" : ")");
                }
                sb.append('\n');
                appendIndented(sb, e.getDescription());
            }
        }

        List<Education> edus = r.getEducations();
        if (!edus.isEmpty()) {
            section(sb, "Education", modern);
            for (Education e : edus) {
                sb.append(modern ? "* " : "").append(join(", ", e.getDegree(), e.getInstitution()));
                String extra = join(" - ", e.getYear(), ValidationUtil.isBlank(e.getGrade()) ? "" : "Grade: " + e.getGrade());
                if (!extra.isEmpty()) {
                    sb.append("  (").append(extra).append(')');
                }
                sb.append('\n');
            }
        }

        List<Project> projects = r.getProjects();
        if (!projects.isEmpty()) {
            section(sb, "Projects", modern);
            for (Project p : projects) {
                sb.append(modern ? "* " : "").append(p.getName());
                if (!ValidationUtil.isBlank(p.getLink())) {
                    sb.append("  <").append(p.getLink().trim()).append('>');
                }
                sb.append('\n');
                appendIndented(sb, p.getDescription());
            }
        }

        List<Certification> certs = r.getCertifications();
        if (!certs.isEmpty()) {
            section(sb, "Certifications", modern);
            for (Certification c : certs) {
                sb.append(modern ? "* " : "- ").append(join(", ", c.getName(), c.getIssuer()));
                if (!ValidationUtil.isBlank(c.getYear())) {
                    sb.append(" (").append(c.getYear().trim()).append(')');
                }
                sb.append('\n');
            }
        }

        if (!r.getSkills().isEmpty()) {
            section(sb, "Skills", modern);
            List<String> names = new ArrayList<>();
            for (Skill s : r.getSkills()) {
                addIfPresent(names, s.getName());
            }
            sb.append(String.join(modern ? "  #  " : ", ", names)).append('\n');
        }
        return sb.toString();
    }

    private static void section(StringBuilder sb, String title, boolean modern) {
        sb.append('\n');
        if (modern) {
            sb.append(">> ").append(title.toUpperCase()).append('\n');
        } else {
            sb.append(title.toUpperCase()).append('\n').append("-".repeat(40)).append('\n');
        }
    }

    private static void appendIndented(StringBuilder sb, String text) {
        if (ValidationUtil.isBlank(text)) {
            return;
        }
        for (String line : text.trim().split("\\R")) {
            if (!line.isBlank()) {
                sb.append("    ").append(line.trim()).append('\n');
            }
        }
    }

    private static void addIfPresent(List<String> list, String value) {
        if (!ValidationUtil.isBlank(value)) {
            list.add(value.trim());
        }
    }

    private static String join(String sep, String... parts) {
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            addIfPresent(list, p);
        }
        return String.join(sep, list);
    }

    private static String dateRange(String start, String end) {
        boolean s = !ValidationUtil.isBlank(start);
        boolean e = !ValidationUtil.isBlank(end);
        if (s && e) {
            return start.trim() + " - " + end.trim();
        }
        if (s) {
            return start.trim() + " - Present";
        }
        return e ? end.trim() : "";
    }

    // ------------------------------------------------------------ AI summary

    /** Asks Gemini for a professional summary; falls back to a local template on any failure. */
    public GenerationResult generateSummary(Resume r) {
        String note;
        if (api.hasApiKey()) {
            try {
                String text = api.generate(buildSummaryPrompt(r));
                if (!ValidationUtil.isBlank(text)) {
                    return new GenerationResult(text.trim(), true, "Summary generated with Gemini.");
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
        return new GenerationResult(localSummary(r), false, "Local template used: " + note + ".");
    }

    private static String buildSummaryPrompt(Resume r) {
        StringBuilder p = new StringBuilder();
        p.append("Write a professional resume summary of 3 to 4 sentences for a ")
                .append(ValidationUtil.isBlank(r.getJobTitle()) ? "job seeker" : r.getJobTitle().trim())
                .append(". Use plain text only: no headings, no bullet points, no quotation marks, no first-person 'I'.\n");
        if (!r.getSkills().isEmpty()) {
            List<String> names = new ArrayList<>();
            r.getSkills().forEach(s -> addIfPresent(names, s.getName()));
            p.append("Skills: ").append(String.join(", ", names)).append(".\n");
        }
        for (Experience e : r.getExperiences()) {
            p.append("Experience: ").append(join(" at ", e.getRole(), e.getCompany())).append(". ")
                    .append(e.getDescription()).append('\n');
        }
        for (Education e : r.getEducations()) {
            p.append("Education: ").append(join(", ", e.getDegree(), e.getInstitution())).append(".\n");
        }
        if (!ValidationUtil.isBlank(r.getSummary())) {
            p.append("Improve this existing draft: ").append(r.getSummary().trim()).append('\n');
        }
        return p.toString();
    }

    static String localSummary(Resume r) {
        String title = ValidationUtil.isBlank(r.getJobTitle()) ? "professional" : r.getJobTitle().trim();
        StringBuilder sb = new StringBuilder("Motivated ").append(title);
        if (!r.getExperiences().isEmpty()) {
            Experience latest = r.getExperiences().get(0);
            int n = r.getExperiences().size();
            sb.append(" with hands-on experience across ").append(n).append(n == 1 ? " role" : " roles");
            String recent = join(" at ", latest.getRole(), latest.getCompany());
            if (!recent.isEmpty()) {
                sb.append(", most recently as ").append(recent);
            }
            sb.append(". ");
        } else {
            sb.append(" eager to start and grow a career with real-world impact. ");
        }
        List<String> skills = new ArrayList<>();
        r.getSkills().forEach(s -> addIfPresent(skills, s.getName()));
        if (!skills.isEmpty()) {
            sb.append("Skilled in ").append(humanJoin(skills.subList(0, Math.min(5, skills.size()))))
                    .append(", with a strong focus on quality and continuous learning. ");
        }
        if (!r.getEducations().isEmpty()) {
            Education e = r.getEducations().get(0);
            String edu = join(" from ", e.getDegree(), e.getInstitution());
            if (!edu.isEmpty()) {
                sb.append("Holds ").append(edu).append(". ");
            }
        }
        sb.append("Seeking to contribute to a collaborative team while delivering measurable results.");
        return sb.toString();
    }

    static String humanJoin(List<String> items) {
        if (items.size() <= 1) {
            return String.join("", items);
        }
        return String.join(", ", items.subList(0, items.size() - 1)) + " and " + items.get(items.size() - 1);
    }
}
