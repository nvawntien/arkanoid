package com.game.arkanoid.controller;

import com.game.arkanoid.container.AppContext;
import com.game.arkanoid.models.RankingEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public final class RankingsController {

    private final SceneController navigator;
    @FXML private TableView<RankingRow> table;
    @FXML private TableColumn<RankingRow, Integer> colRank;
    @FXML private TableColumn<RankingRow, String> colName;
    @FXML private TableColumn<RankingRow, Integer> colScore;
    @FXML private TableColumn<RankingRow, Integer> colRound;
    @FXML private Button backButton;

    public RankingsController(SceneController navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colRound.setCellValueFactory(new PropertyValueFactory<>("round"));

        backButton.setOnAction(e -> navigator.showMenu());

        AppContext.getInstance().db().getRankings(100).whenComplete((list, err) -> {
            Platform.runLater(() -> {
                if (err != null) {
                    table.setItems(FXCollections.observableArrayList());
                } else {
                    table.setItems(toRows(list));
                }
            });
        });
    }

    private ObservableList<RankingRow> toRows(List<RankingEntry> entries) {
        ObservableList<RankingRow> rows = FXCollections.observableArrayList();
        int rank = 1;
        for (RankingEntry e : entries) {
            rows.add(new RankingRow(rank++, e.getName(), e.getBestScore(), e.getBestRound()));
        }
        return rows;
    }

    public static final class RankingRow {
        private final int rank;
        private final String name;
        private final int score;
        private final int round;

        public RankingRow(int rank, String name, int score, int round) {
            this.rank = rank; this.name = name; this.score = score; this.round = round;
        }
        public int getRank() { return rank; }
        public String getName() { return name; }
        public int getScore() { return score; }
        public int getRound() { return round; }
    }
}

