package com.aizen.controller;

import com.aizen.model.Application;
import com.aizen.service.ApplicationService;
import com.aizen.thread.SaveTask;
import com.aizen.thread.TaskManager;
import com.aizen.util.SceneManager;
import com.aizen.util.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * "Job Application Tracker" - a small CRUD screen: an add/edit form on top and a table of
 * every application the user is tracking below, with double-click-to-edit and delete.
 */
public class ApplicationTrackerController {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ApplicationService service = new ApplicationService();
    private final ObservableList<Application> items = FXCollections.observableArrayList();

    @FXML private TextField tfCompany;
    @FXML private TextField tfRole;
    @FXML private ChoiceBox<String> cbStatus;
    @FXML private DatePicker dpDate;
    @FXML private TextArea taNotes;
    @FXML private Label lblFormError;
    @FXML private Button btnSave;

    @FXML private TableView<Application> table;
    @FXML private TableColumn<Application, String> colCompany;
    @FXML private TableColumn<Application, String> colRole;
    @FXML private TableColumn<Application, String> colStatus;
    @FXML private TableColumn<Application, String> colDate;
    @FXML private TableColumn<Application, String> colNotes;

    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progress;

    private int editingId = 0;

    @FXML
    private void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(Application.statuses()));
        cbStatus.getSelectionModel().selectFirst();
        dpDate.setValue(LocalDate.now());

        colCompany.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompany()));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoleTitle()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAppliedDate()));
        colNotes.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNotes()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label chip = new Label(status);
                chip.getStyleClass().addAll("at-badge", "at-badge-" + status.toLowerCase());
                setGraphic(chip);
                setText(null);
            }
        });

        table.setItems(items);
        table.setPlaceholder(new Label("No applications tracked yet - add your first one above."));
        table.setRowFactory(tv -> {
            TableRow<Application> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    populateForm(row.getItem());
                }
            });
            return row;
        });

        reload();
    }

    private void reload() {
        final int userId = Session.getCurrentUser().getId();
        SaveTask<List<Application>> task = new SaveTask<>("Loading applications...",
                () -> service.listForUser(userId));
        TaskManager.run(task, progress, list -> {
            items.setAll(list);
            statusLabel.setText(list.size() + (list.size() == 1 ? " application" : " applications") + " tracked.");
        }, ex -> statusLabel.setText("Could not load applications: " + TaskManager.messageOf(ex)));
    }

    @FXML
    private void onSave() {
        Application a = new Application();
        a.setId(editingId);
        a.setCompany(tfCompany.getText());
        a.setRoleTitle(tfRole.getText());
        a.setStatus(cbStatus.getValue());
        a.setAppliedDate(dpDate.getValue() == null ? "" : dpDate.getValue().format(DATE_FMT));
        a.setNotes(taNotes.getText());

        hideFormError();
        final int userId = Session.getCurrentUser().getId();
        SaveTask<Application> task = new SaveTask<>("Saving...", () -> service.save(a, userId));
        TaskManager.run(task, progress, saved -> {
            String company = saved.getCompany();
            onClear();
            reload();
            statusLabel.setText("Saved \"" + company + "\".");
        }, ex -> showFormError(TaskManager.messageOf(ex)));
    }

    private void populateForm(Application a) {
        editingId = a.getId();
        tfCompany.setText(a.getCompany());
        tfRole.setText(a.getRoleTitle());
        cbStatus.setValue(a.getStatus());
        try {
            dpDate.setValue(a.getAppliedDate().isBlank() ? LocalDate.now() : LocalDate.parse(a.getAppliedDate(), DATE_FMT));
        } catch (Exception ignored) {
            dpDate.setValue(LocalDate.now());
        }
        taNotes.setText(a.getNotes());
        btnSave.setText("Update application");
        statusLabel.setText("Editing \"" + a.getCompany() + "\" - change the fields and click Update.");
    }

    @FXML
    private void onClear() {
        editingId = 0;
        tfCompany.clear();
        tfRole.clear();
        cbStatus.getSelectionModel().selectFirst();
        dpDate.setValue(LocalDate.now());
        taNotes.clear();
        btnSave.setText("Add application");
        hideFormError();
    }

    @FXML
    private void onDeleteSelected() {
        Application a = table.getSelectionModel().getSelectedItem();
        if (a == null) {
            statusLabel.setText("Select a row in the table first.");
            return;
        }
        if (!SceneManager.confirm("Delete application",
                "Delete the entry for \"" + a.getCompany() + "\"? This cannot be undone.")) {
            return;
        }
        SaveTask<Boolean> task = new SaveTask<>("Deleting...", () -> service.delete(a.getId()));
        TaskManager.run(task, progress, ok -> {
            items.remove(a);
            if (editingId == a.getId()) {
                onClear();
            }
            statusLabel.setText("Deleted \"" + a.getCompany() + "\".");
        }, ex -> statusLabel.setText("Delete failed: " + TaskManager.messageOf(ex)));
    }

    private void showFormError(String message) {
        lblFormError.setText(message);
        lblFormError.setVisible(true);
        lblFormError.setManaged(true);
    }

    private void hideFormError() {
        lblFormError.setVisible(false);
        lblFormError.setManaged(false);
    }
}