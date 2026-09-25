package com.aizen.controller;

import com.aizen.service.InterviewQuestionData;
import com.aizen.service.InterviewQuestionData.QuestionStat;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Shows the top interview-question topics for a chosen job sector as a horizontal bar chart. */
public class InterviewPrepController {
    @FXML private ChoiceBox<String> cbSector;
    @FXML private BarChart<Number, String> chart;
    @FXML private Label lblCount;

    @FXML
    private void initialize() {
        List<String> sectors = InterviewQuestionData.sectors();
        cbSector.setItems(FXCollections.observableArrayList(sectors));
        cbSector.valueProperty().addListener((obs, oldV, newV) -> render(newV));
        cbSector.getSelectionModel().selectFirst();
        render(cbSector.getValue());
    }

    private void render(String sector) {
        chart.getData().clear();
        if (sector == null) {
            return;
        }
        List<QuestionStat> stats = new ArrayList<>(InterviewQuestionData.topTen(sector));
        // Reverse so the highest-importance topic renders at the top of the chart.
        Collections.reverse(stats);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName(sector);
        for (QuestionStat stat : stats) {
            XYChart.Data<Number, String> data = new XYChart.Data<>(stat.importance(), stat.topic());
            series.getData().add(data);
        }
        chart.getData().add(series);

        if (lblCount != null) {
            lblCount.setText(stats.size() + " topics");
        }

        // Attach a small tooltip to each bar once the nodes exist.
        for (XYChart.Data<Number, String> data : series.getData()) {
            Node barNode = data.getNode();
            if (barNode != null) {
                Tooltip.install(barNode, new Tooltip(data.getYValue() + " — importance " + data.getXValue() + "/10"));
            }
        }
    }
}