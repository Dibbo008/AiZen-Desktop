package com.aizen.controller;

import com.aizen.service.InterviewQuestionData;
import com.aizen.service.InterviewQuestionData.QuestionStat;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;

/** Shows the top interview-question topics for a chosen job sector as a bar chart. */
public class InterviewPrepController {
    @FXML private ChoiceBox<String> cbSector;
    @FXML private BarChart<String, Number> chart;

    @FXML
    private void initialize() {
        cbSector.setItems(FXCollections.observableArrayList(InterviewQuestionData.sectors()));
        cbSector.valueProperty().addListener((obs, oldV, newV) -> render(newV));
        cbSector.getSelectionModel().selectFirst();
        render(cbSector.getValue());
    }

    private void render(String sector) {
        chart.getData().clear();
        if (sector == null) {
            return;
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(sector);
        for (QuestionStat stat : InterviewQuestionData.topTen(sector)) {
            series.getData().add(new XYChart.Data<>(stat.topic(), stat.importance()));
        }
        chart.getData().add(series);
    }
}