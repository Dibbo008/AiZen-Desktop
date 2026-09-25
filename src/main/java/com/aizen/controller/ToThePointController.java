package com.aizen.controller;

import com.aizen.service.CareerResourceData;
import com.aizen.service.CareerResourceData.Resource;
import com.aizen.service.GenerationResult;
import com.aizen.service.ToThePointService;
import com.aizen.thread.ApiTask;
import com.aizen.thread.TaskManager;
import com.aizen.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

/**
 * "To The Point" - a short wizard that asks the user which role they're targeting and how
 * experienced they are, then shows a hand-curated set of resources for the matching career
 * sector. The AI (when available) only ever picks a sector name from a fixed list; the
 * resource links themselves always come from {@link CareerResourceData}, so nothing shown
 * here can be a broken or hallucinated URL.
 */
public class ToThePointController {
    private final ToThePointService service = new ToThePointService();

    @FXML private VBox stepRole;
    @FXML private VBox stepLevel;
    @FXML private VBox stepLoading;
    @FXML private VBox stepResult;

    @FXML private TextField tfRole;
    @FXML private Label lblRoleError;

    @FXML private Label lblSector;
    @FXML private Label lblSectorNote;
    @FXML private VBox resourceBox;

    private String roleText;
    private String level = "Entry-level";

    @FXML
    private void initialize() {
        showOnly(stepRole);
    }

    @FXML
    private void onRoleNext() {
        String role = tfRole.getText();
        if (ValidationUtil.isBlank(role)) {
            lblRoleError.setText("Please type the role you're aiming for.");
            lblRoleError.setVisible(true);
            lblRoleError.setManaged(true);
            return;
        }
        lblRoleError.setVisible(false);
        lblRoleError.setManaged(false);
        roleText = role.trim();
        showOnly(stepLevel);
    }

    @FXML
    private void onBackToRole() {
        showOnly(stepRole);
    }

    @FXML private void onLevelEntry() { chooseLevel("Entry-level"); }

    @FXML private void onLevelMid() { chooseLevel("Mid-level"); }

    @FXML private void onLevelSenior() { chooseLevel("Experienced"); }

    private void chooseLevel(String chosen) {
        level = chosen;
        showOnly(stepLoading);
        ApiTask<GenerationResult> task = new ApiTask<>("Matching your role...",
                () -> service.classifySector(roleText));
        TaskManager.run(task, null, this::showResult,
                ex -> showResult(new GenerationResult("Software Engineering", false,
                        "Could not reach the AI (" + TaskManager.messageOf(ex) + "); showing a general match.")));
    }

    private void showResult(GenerationResult result) {
        String sector = result.text();
        lblSector.setText(sector);

        String levelNote = switch (level) {
            case "Mid-level" -> "You're mid-level for \"" + roleText + "\" - these resources go a bit deeper.";
            case "Experienced" -> "You're experienced for \"" + roleText
                    + "\" - lean on the practice and community links to stay sharp.";
            default -> "You're just starting out with \"" + roleText + "\" - work through the list from the top.";
        };
        lblSectorNote.setText(levelNote + (result.usedAi() ? "" : " (" + result.note() + ")"));

        resourceBox.getChildren().clear();
        List<Resource> resources = CareerResourceData.forSector(sector);
        for (Resource r : resources) {
            resourceBox.getChildren().add(buildResourceCard(r));
        }
        showOnly(stepResult);
    }

    private VBox buildResourceCard(Resource r) {
        Label title = new Label(r.title());
        title.getStyleClass().add("ttp-resource-title");

        Label tag = new Label(r.type());
        tag.getStyleClass().add("ttp-resource-tag");

        HBox header = new HBox(8, title, tag);
        header.getStyleClass().add("ttp-resource-header");

        Label desc = new Label(r.description());
        desc.getStyleClass().add("ttp-resource-desc");
        desc.setWrapText(true);

        Hyperlink link = new Hyperlink(r.url());
        link.getStyleClass().add("ttp-resource-link");
        link.setOnAction(e -> openLink(r.url()));

        VBox card = new VBox(6, header, desc, link);
        card.getStyleClass().add("ttp-resource-card");
        return card;
    }

    private void openLink(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ignored) {
            // If the platform can't open a browser the link text is still visible and copyable.
        }
    }

    @FXML
    private void onRestart() {
        tfRole.clear();
        roleText = null;
        showOnly(stepRole);
    }

    private void showOnly(VBox target) {
        for (VBox step : List.of(stepRole, stepLevel, stepLoading, stepResult)) {
            boolean show = step == target;
            step.setVisible(show);
            step.setManaged(show);
        }
    }
}