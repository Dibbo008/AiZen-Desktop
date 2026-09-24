package com.aizen.controller;

import com.aizen.exception.ValidationException;
import com.aizen.model.Resume;
import com.aizen.service.CoverLetterService;
import com.aizen.service.GenerationResult;
import com.aizen.service.ResumeService;
import com.aizen.thread.ApiTask;
import com.aizen.thread.SaveTask;
import com.aizen.thread.TaskManager;
import com.aizen.util.Session;
import com.aizen.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.StringConverter;

import java.util.List;

/** Generates cover letters (AI or local fallback) and copies them to the clipboard. */
public class CoverLetterController {
    @FXML private TextField tfRole;
    @FXML private TextField tfCompany;
    @FXML private ChoiceBox<String> cbTone;
    @FXML private ComboBox<Resume> cbResume;
    @FXML private TextArea taNotes;
    @FXML private TextArea taOutput;
    @FXML private Button btnGenerate;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progress;

    private final CoverLetterService coverService = new CoverLetterService();
    private final ResumeService resumeService = new ResumeService();

    @FXML
    private void initialize(){
        cbTone.setItems(FXCollections.observableArrayList("Professional", "Confident", "Friendly"));
        cbTone.setValue("Professional");
        cbResume.setConverter(new StringConverter<>() {
            @Override
            public String toString(Resume r) {
                return r == null ? "" : r.getTitle();
            }

            @Override
            public Resume fromString(String s) {
                return null;
            }
        });

        final int userId = Session.getCurrentUser().getId();
        SaveTask<List<Resume>> task = new SaveTask<>("Loading resumes...", () -> resumeService.listForUser(userId));
        TaskManager.run(task, progress,
                list -> cbResume.setItems(FXCollections.observableArrayList(list)),
                ex -> statusLabel.setText("Could not load saved resumes: " + TaskManager.messageOf(ex)));
    }
    @FXML
    private void onClearResume(){
        cbResume.getSelectionModel().clearSelection();
        cbResume.setValue(null);
    }
    @FXML
    private void onGenerate(){
        final String role;
        final String company;
        try {
            role = ValidationUtil.requireNonBlank("Job role", tfRole.getText());
            company = ValidationUtil.requireNonBlank("Target company", tfCompany.getText());
        } catch (ValidationException e) {
            statusLabel.setText(e.getMessage());
            return;
        }
        final String tone = cbTone.getValue();
        final Resume resume = cbResume.getValue();
        final String notes = taNotes.getText();
        final String name = (resume != null && !ValidationUtil.isBlank(resume.getFullName()))
                ? resume.getFullName().trim() : Session.getCurrentUser().getFullName();

        statusLabel.setText("Generating...");
        btnGenerate.setDisable(true);
        ApiTask<GenerationResult> task = new ApiTask<>("Generating cover letter...",
                () -> coverService.generate(name, role, company, tone, resume, notes));
        TaskManager.run(task, progress, result -> {
            btnGenerate.setDisable(false);
            taOutput.setText(result.text());
            statusLabel.setText(result.note());
        }, ex -> {
            btnGenerate.setDisable(false);
            statusLabel.setText("Error: " + TaskManager.messageOf(ex));
        });
    }
    @FXML
    private void onCopy() {
        String text = taOutput.getText();
        if (ValidationUtil.isBlank(text)) {
            statusLabel.setText("Nothing to copy yet - generate a letter first.");
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
        statusLabel.setText("Copied to clipboard.");
    }
    @FXML
    private void onClearOutput() {
        taOutput.clear();
        statusLabel.setText("Ready.");
    }
}
