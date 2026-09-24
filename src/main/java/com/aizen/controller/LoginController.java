package com.aizen.controller;

import com.aizen.exception.ValidationException;
import com.aizen.model.User;
import com.aizen.service.AuthService;
import com.aizen.thread.SaveTask;
import com.aizen.thread.TaskManager;
import com.aizen.util.Session;
import com.aizen.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Handles sign-in and registration. */
public class LoginController {
    @FXML private Label formTitle;
    @FXML private Label errorLabel;
    @FXML private VBox registerFields;
    @FXML private TextField tfFullName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private PasswordField pfConfirm;
    @FXML private Button btnSubmit;
    @FXML private Hyperlink switchLink;
    @FXML private ProgressIndicator progress;

    private final AuthService auth = new AuthService();
    private boolean registerMode;

    @FXML
    private void initialize() {
        tfUsername.setText(Session.getLastUsername());
        setMode(false);
    }

    @FXML
    private void onSwitchMode() {
        setMode(!registerMode);
    }

    private void setMode(boolean register) {
        registerMode = register;
        registerFields.setVisible(register);
        registerFields.setManaged(register);
        pfConfirm.setVisible(register);
        pfConfirm.setManaged(register);
        formTitle.setText(register ? "Create your account" : "Sign in");
        btnSubmit.setText(register ? "Register" : "Sign in");
        switchLink.setText(register ? "Already registered? Sign in" : "No account yet? Create one");
        errorLabel.setText("");
    }

    @FXML
    private void onSubmit() {
        errorLabel.setText("");
        final String username = tfUsername.getText();
        final String password = pfPassword.getText();
        SaveTask<User> task;

        if (registerMode) {
            final String fullName = tfFullName.getText();
            final String email = tfEmail.getText();
            if (!password.equals(pfConfirm.getText())) {
                errorLabel.setText("Passwords do not match.");
                return;
            }
            task = new SaveTask<>("Creating account...", () -> auth.register(username, password, fullName, email));
        } else {
            task = new SaveTask<>("Signing in...", () -> auth.login(username, password));
        }

        btnSubmit.disableProperty().bind(task.runningProperty());
        TaskManager.run(task, progress, user -> {
            Session.login(user);
            SceneManager.showMain();
        }, ex -> errorLabel.setText(ex instanceof ValidationException
                ? ex.getMessage() : "Error: " + TaskManager.messageOf(ex)));
    }
}
