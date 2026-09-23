package com.aizen.controller;

import com.aizen.model.Resume;
import com.aizen.service.PdfService;
import com.aizen.service.ResumeService;
import com.aizen.thread.PdfTask;
import com.aizen.thread.TaskManager;
import com.aizen.util.SceneManager;
import com.aizen.util.SceneManager.View;
import com.aizen.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;

/** Read-only text preview of a saved resume with PDF export. */
public class PreviewController {
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea previewArea;
    @FXML private ProgressIndicator progress;

    private final ResumeService resumeService = new ResumeService();
    private final PdfService pdfService = new PdfService();
    private Resume resume;

    @FXML
    private void initialize() {
        resume = Session.getResumeToPreview();
        if (resume == null) {
            titleLabel.setText("Preview");
            previewArea.setText("No resume selected.");
            return;
        }
        titleLabel.setText("Preview - " + resume.getTitle() + " (" + resume.getTemplate() + " template)");
        previewArea.setText(resumeService.buildPreview(resume));
    }

    @FXML
    private void onBack() {
        MainController.getInstance().navigate(View.SAVED);
    }

    @FXML
    private void onEdit() {
        if (resume != null) {
            Session.setResumeToEdit(resume);
            MainController.getInstance().navigate(View.RESUME);
        }
    }

    @FXML
    private void onExportPdf() {
        if (resume == null) {
            return;
        }
        final Resume r = resume;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export resume as PDF");
        chooser.setInitialFileName(r.getTitle().replaceAll("[^A-Za-z0-9_-]+", "_") + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        final File file = chooser.showSaveDialog(SceneManager.getStage());
        if (file == null) {
            return;
        }
        statusLabel.setText("Rendering PDF...");
        PdfTask<File> task = new PdfTask<>("Rendering PDF...", () -> pdfService.export(r, file));
        TaskManager.run(task, progress,
                f -> statusLabel.setText("PDF exported to " + f.getAbsolutePath()),
                ex -> statusLabel.setText("Export failed: " + TaskManager.messageOf(ex)));
    }
}
