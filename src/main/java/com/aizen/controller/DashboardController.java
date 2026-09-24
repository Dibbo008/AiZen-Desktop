package com.aizen.controller;

import com.aizen.model.Person;
import com.aizen.model.Resume;
import com.aizen.service.ApiService;
import com.aizen.service.ResumeService;
import com.aizen.thread.SaveTask;
import com.aizen.thread.TaskManager;
import com.aizen.util.SceneManager.View;
import com.aizen.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import java.util.List;

/** Welcome banner, resume counter and quick actions. */
public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label countLabel;
    @FXML private Label roleLabel;
    @FXML private Label aiLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progress;

    private final ResumeService resumeService = new ResumeService();

    @FXML
    private void initialize() {
        String name = Session.getCurrentUser().getFullName();
        welcomeLabel.setText("Welcome back, " + name + "!");
        aiLabel.setText(new ApiService().hasApiKey() ? "Gemini" : "Local");

        int userId = Session.getCurrentUser().getId();
        SaveTask<List<Resume>> task = new SaveTask<>("Loading dashboard...", () -> resumeService.listForUser(userId));
        TaskManager.run(task, progress, list -> {
            countLabel.setText(String.valueOf(list.size()));
            if (list.isEmpty()) {
                roleLabel.setText("-");
                statusLabel.setText("You have no resumes yet - create your first one!");
                return;
            }
            try {
                // Polymorphism: we only call getRole(); the runtime type decides the answer.
                Person profile = resumeService.toProfile(list.get(0));
                roleLabel.setText(profile.getRole());
            } catch (RuntimeException ex) {
                roleLabel.setText("Applicant");
            }
            statusLabel.setText("Last updated resume: " + list.get(0).getTitle() + " (" + list.get(0).getUpdatedAt() + ")");
        }, ex -> {
            countLabel.setText("?");
            statusLabel.setText("Could not load your resumes: " + TaskManager.messageOf(ex));
        });
    }

    @FXML
    private void onNewResume() {
        Session.setResumeToEdit(null);
        MainController.getInstance().navigate(View.RESUME);
    }

    @FXML
    private void onCoverLetter() {
        MainController.getInstance().navigate(View.COVER_LETTER);
    }

    @FXML
    private void onSaved() {
        MainController.getInstance().navigate(View.SAVED);
    }
}
