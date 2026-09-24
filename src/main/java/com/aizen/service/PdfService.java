package com.aizen.service;

import com.aizen.model.Certification;
import com.aizen.model.Education;
import com.aizen.model.Experience;
import com.aizen.model.Project;
import com.aizen.model.Resume;
import com.aizen.model.Skill;
import com.aizen.util.ValidationUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Renders a Resume to an A4 PDF (Classic or Modern template) using Apache PDFBox. */
public class PdfService {
    private static final float MARGIN = 50f;
    private static final float[] BLACK = {0.10f, 0.10f, 0.10f};
    private static final float[] GRAY = {0.38f, 0.38f, 0.38f};
    private static final float[] WHITE = {1f, 1f, 1f};
    private static final float[] ACCENT = {0.24f, 0.25f, 0.78f};

    public File export(Resume resume, File target) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PageWriter w = new PageWriter(doc, "Modern".equalsIgnoreCase(resume.getTemplate()));
            try {
                w.render(resume);
            } finally {
                w.close();
            }
            doc.save(target);
        }
        return target;
    }

    /** Keeps track of the current page / cursor and handles wrapping and page breaks. */
    private static final class PageWriter {
        private final PDDocument doc;
        private final boolean modern;
        private final PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private final PDFont italic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
        private final float pageW = PDRectangle.A4.getWidth();
        private final float pageH = PDRectangle.A4.getHeight();
        private PDPageContentStream cs;
        private float y;

        PageWriter(PDDocument doc, boolean modern) throws IOException {
            this.doc = doc;
            this.modern = modern;
            newPage();
        }

        void close() throws IOException {
            if (cs != null) {
                cs.close();
                cs = null;
            }
        }

        private void newPage() throws IOException {
            if (cs != null) {
                cs.close();
            }
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = pageH - MARGIN;
        }

        private void ensure(float height) throws IOException {
            if (y - height < MARGIN) {
                newPage();
            }
        }

        // ------------------------------------------------------------ text

        private static String clean(String s) {
            if (s == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder(s.length());
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '\u2018', '\u2019' -> sb.append('\'');
                    case '\u201C', '\u201D' -> sb.append('"');
                    case '\u2013', '\u2014' -> sb.append('-');
                    case '\u2022', '\u25CF' -> sb.append('-');
                    case '\u2026' -> sb.append("...");
                    case '\t' -> sb.append(' ');
                    default -> {
                        if ((c >= 32 && c <= 126) || (c >= 160 && c <= 255) || c == '\n') {
                            sb.append(c);
                        } else if (c != '\r') {
                            sb.append('?');
                        }
                    }
                }
            }
            return sb.toString();
        }

        private float width(String s, PDFont font, float size) throws IOException {
            return font.getStringWidth(s) / 1000f * size;
        }

        private List<String> wrap(String text, PDFont font, float size, float maxWidth) throws IOException {
            List<String> lines = new ArrayList<>();
            StringBuilder cur = new StringBuilder();
            for (String word : text.split(" ")) {
                if (word.isEmpty()) {
                    continue;
                }
                String candidate = cur.length() == 0 ? word : cur + " " + word;
                if (width(candidate, font, size) <= maxWidth) {
                    cur = new StringBuilder(candidate);
                    continue;
                }
                if (cur.length() > 0) {
                    lines.add(cur.toString());
                    cur = new StringBuilder();
                }
                String rest = word;
                while (rest.length() > 1 && width(rest, font, size) > maxWidth) {
                    int cut = rest.length() - 1;
                    while (cut > 1 && width(rest.substring(0, cut), font, size) > maxWidth) {
                        cut--;
                    }
                    lines.add(rest.substring(0, cut));
                    rest = rest.substring(cut);
                }
                cur.append(rest);
            }
            if (cur.length() > 0) {
                lines.add(cur.toString());
            }
            return lines;
        }

        /** Draws (possibly multi-line) text at the cursor and moves the cursor down. */
        void paragraph(String raw, PDFont font, float size, float x, float[] color, float spaceAfter)
                throws IOException {
            String text = clean(raw);
            float lead = size * 1.35f;
            for (String block : text.split("\n")) {
                if (block.isBlank()) {
                    y -= lead * 0.5f;
                    continue;
                }
                for (String line : wrap(block.trim(), font, size, pageW - MARGIN - x)) {
                    ensure(lead);
                    y -= size;
                    cs.beginText();
                    cs.setFont(font, size);
                    cs.setNonStrokingColor(color[0], color[1], color[2]);
                    cs.newLineAtOffset(x, y);
                    cs.showText(line);
                    cs.endText();
                    y -= lead - size;
                }
            }
            y -= spaceAfter;
        }

        void centered(String raw, PDFont font, float size, float[] color, float spaceAfter) throws IOException {
            String text = clean(raw);
            float lead = size * 1.35f;
            ensure(lead);
            float w = Math.min(width(text, font, size), pageW - 2 * MARGIN);
            y -= size;
            cs.beginText();
            cs.setFont(font, size);
            cs.setNonStrokingColor(color[0], color[1], color[2]);
            cs.newLineAtOffset((pageW - w) / 2f, y);
            cs.showText(text);
            cs.endText();
            y -= lead - size + spaceAfter;
        }

        // ---------------------------------------------------------- layout

        void heading(String title) throws IOException {
            ensure(40);
            y -= 8;
            paragraph(title.toUpperCase(), bold, 11.5f, MARGIN, modern ? ACCENT : BLACK, 1);
            cs.setStrokingColor(modern ? ACCENT[0] : GRAY[0], modern ? ACCENT[1] : GRAY[1], modern ? ACCENT[2] : GRAY[2]);
            cs.setLineWidth(modern ? 1.4f : 0.7f);
            cs.moveTo(MARGIN, y + 2);
            cs.lineTo(pageW - MARGIN, y + 2);
            cs.stroke();
            y -= 6;
        }

        void render(Resume r) throws IOException {
            String name = ValidationUtil.isBlank(r.getFullName()) ? "Untitled" : r.getFullName().trim();
            List<String> contact = new ArrayList<>();
            for (String s : new String[]{r.getEmail(), r.getPhone(), r.getLocation(), r.getLinkedin()}) {
                if (!ValidationUtil.isBlank(s)) {
                    contact.add(s.trim());
                }
            }
            String contactLine = String.join("  |  ", contact);

            if (modern) {
                float barHeight = 105f;
                cs.setNonStrokingColor(ACCENT[0], ACCENT[1], ACCENT[2]);
                cs.addRect(0, pageH - barHeight, pageW, barHeight);
                cs.fill();
                y = pageH - 28;
                paragraph(name, bold, 24, MARGIN, WHITE, 0);
                if (!ValidationUtil.isBlank(r.getJobTitle())) {
                    paragraph(r.getJobTitle().trim(), regular, 12.5f, MARGIN, WHITE, 0);
                }
                paragraph(contactLine, regular, 9, MARGIN, WHITE, 0);
                y = Math.min(y, pageH - barHeight) - 14;
            } else {
                centered(name, bold, 24, BLACK, 2);
                if (!ValidationUtil.isBlank(r.getJobTitle())) {
                    centered(r.getJobTitle().trim(), italic, 12.5f, GRAY, 2);
                }
                centered(contactLine, regular, 9.5f, GRAY, 4);
            }

            if (!ValidationUtil.isBlank(r.getSummary())) {
                heading("Summary");
                paragraph(r.getSummary(), regular, 10.5f, MARGIN, BLACK, 4);
            }

            if (!r.getExperiences().isEmpty()) {
                heading("Experience");
                for (Experience e : r.getExperiences()) {
                    String head = joinNonBlank(" - ", e.getRole(), e.getCompany());
                    paragraph(head.isEmpty() ? "Experience" : head, bold, 11, MARGIN, BLACK, 0);
                    String dates = dateRange(e.getStartDate(), e.getEndDate());
                    if (!dates.isEmpty()) {
                        paragraph(dates, italic, 9.5f, MARGIN, GRAY, 1);
                    }
                    bullets(e.getDescription());
                    y -= 4;
                }
            }

            if (!r.getEducations().isEmpty()) {
                heading("Education");
                for (Education e : r.getEducations()) {
                    paragraph(joinNonBlank(", ", e.getDegree(), e.getInstitution()), bold, 11, MARGIN, BLACK, 0);
                    String extra = joinNonBlank("  -  ", e.getYear(),
                            ValidationUtil.isBlank(e.getGrade()) ? "" : "Grade: " + e.getGrade().trim());
                    if (!extra.isEmpty()) {
                        paragraph(extra, italic, 9.5f, MARGIN, GRAY, 1);
                    }
                    y -= 4;
                }
            }

            if (!r.getProjects().isEmpty()) {
                heading("Projects");
                for (Project p : r.getProjects()) {
                    paragraph(p.getName(), bold, 11, MARGIN, BLACK, 0);
                    if (!ValidationUtil.isBlank(p.getLink())) {
                        paragraph(p.getLink().trim(), italic, 9.5f, MARGIN, modern ? ACCENT : GRAY, 1);
                    }
                    bullets(p.getDescription());
                    y -= 4;
                }
            }

            if (!r.getCertifications().isEmpty()) {
                heading("Certifications");
                for (Certification c : r.getCertifications()) {
                    String line = joinNonBlank(", ", c.getName(), c.getIssuer());
                    if (!ValidationUtil.isBlank(c.getYear())) {
                        line += " (" + c.getYear().trim() + ")";
                    }
                    paragraph("- " + line, regular, 10.5f, MARGIN + 4, BLACK, 1);
                }
            }

            if (!r.getSkills().isEmpty()) {
                heading("Skills");
                List<String> names = new ArrayList<>();
                for (Skill s : r.getSkills()) {
                    if (!ValidationUtil.isBlank(s.getName())) {
                        names.add(s.getName().trim());
                    }
                }
                paragraph(String.join(modern ? "   |   " : ", ", names), regular, 10.5f, MARGIN, BLACK, 2);
            }
        }

        private void bullets(String text) throws IOException {
            if (ValidationUtil.isBlank(text)) {
                return;
            }
            for (String line : text.trim().split("\\R")) {
                String t = line.trim();
                if (t.isEmpty()) {
                    continue;
                }
                if (t.startsWith("-") || t.startsWith("*") || t.startsWith("\u2022")) {
                    t = t.substring(1).trim();
                }
                paragraph("- " + t, regular, 10.5f, MARGIN + 8, BLACK, 0);
            }
        }

        private static String joinNonBlank(String sep, String... parts) {
            List<String> list = new ArrayList<>();
            for (String p : parts) {
                if (!ValidationUtil.isBlank(p)) {
                    list.add(p.trim());
                }
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
    }
}
