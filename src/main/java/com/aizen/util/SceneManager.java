package com.aizen.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.prefs.Preferences;

/** Screen routing and Light/Dark theme handling. */
public final class SceneManager {

    /** Screens shown in the center area of the main window. */
    public enum View {
        DASHBOARD("/fxml/dashboard.fxml"),
        RESUME("/fxml/resume.fxml"),
        COVER_LETTER("/fxml/cover_letter.fxml"),
        SAVED("/fxml/saved_resumes.fxml"),
        PREVIEW("/fxml/preview.fxml"),
        INTERVIEW_PREP("/fxml/interview_prep.fxml");

        private final String path;

        View(String path) {
            this.path = path;
        }

        public String path() {
            return path;
        }
    }

    private static final Preferences PREFS = Preferences.userNodeForPackage(SceneManager.class);
    private static Stage stage;
    private static Scene scene;
    private static boolean dark = PREFS.getBoolean("dark", false);

    private SceneManager() {
    }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("CareerForge - AI Resume & Career Builder");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
    }

    public static Parent load(String fxmlPath) {
        URL url = SceneManager.class.getResource(fxmlPath);
        if (url == null) {
            throw new IllegalStateException("Missing FXML: " + fxmlPath);
        }
        try {
            return FXMLLoader.load(url);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load " + fxmlPath, e);
        }
    }

    public static void showLogin() {
        setRoot(load("/fxml/login.fxml"));
    }

    public static void showMain() {
        setRoot(load("/fxml/main.fxml"));
    }

    private static void setRoot(Parent root) {
        if (scene == null) {
            scene = new Scene(root, 1200, 780);
            stage.setScene(scene);
            applyTheme();
            stage.show();
        } else {
            scene.setRoot(root);
        }
    }

    public static boolean isDark() {
        return dark;
    }

    public static void toggleTheme() {
        dark = !dark;
        PREFS.putBoolean("dark", dark);
        applyTheme();
    }

    private static void applyTheme() {
        if (scene == null) {
            return;
        }
        scene.getStylesheets().setAll(css("/css/style.css"));
        if (dark) {
            scene.getStylesheets().add(css("/css/dark.css"));
        }
    }

    private static String css(String path) {
        return SceneManager.class.getResource(path).toExternalForm();
    }

    public static Stage getStage() {
        return stage;
    }

    // ----------------------------------------------------------------- dialogs

    private static Alert styled(Alert alert) {
        if (scene != null) {
            alert.getDialogPane().getStylesheets().setAll(scene.getStylesheets());
        }
        if (stage != null) {
            alert.initOwner(stage);
        }
        return alert;
    }

    public static void error(String title, String message) {
        Alert alert = styled(new Alert(Alert.AlertType.ERROR, message, ButtonType.OK));
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public static void info(String title, String message) {
        Alert alert = styled(new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK));
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public static boolean confirm(String title, String message) {
        Alert alert = styled(new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO));
        alert.setTitle(title);
        alert.setHeaderText(title);
        return alert.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }
}