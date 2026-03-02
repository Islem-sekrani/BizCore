package edu.Connexion3A7.Controller;

import edu.Connexion3A7.dto.CoachPerformanceDto;
import edu.Connexion3A7.services.StatistiqueService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controller for stat.fxml — shows performance leaderboard for all coaches.
 * Uses the 4 weighted KPI factors: Occupation (35%), Note (35%), Fidélité
 * (20%), Tendance (10%).
 */
public class StatController {

    @FXML
    private ScrollPane chartArea;
    @FXML
    private Label perfStatusLabel;
    @FXML
    private Button refreshChartBtn;

    private final StatistiqueService statsService = new StatistiqueService();
    private List<CoachPerformanceDto> perfData = List.of();

    @FXML
    public void initialize() {
        loadPerfDataAsync();
    }

    // =========================================================================
    // Data loading
    // =========================================================================

    /** Loads performance data asynchronously then renders the leaderboard. */
    private void loadPerfDataAsync() {
        perfStatusLabel.setText("Chargement des statistiques…");
        Thread t = new Thread(() -> {
            try {
                List<CoachPerformanceDto> data = statsService.getCoachesPerformance();
                Platform.runLater(() -> {
                    perfData = data;
                    buildLeaderboard();
                    perfStatusLabel.setText(
                            data.isEmpty() ? "Aucune donnée disponible."
                                    : data.size() + " coach(s) analysé(s)");
                });
            } catch (Exception e) {
                Platform.runLater(() -> perfStatusLabel.setText("Erreur stats: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.setName("stat-loader");
        t.start();
    }

    @FXML
    void handleRefreshChart() {
        loadPerfDataAsync();
    }

    // =========================================================================
    // Leaderboard builder
    // =========================================================================

    /**
     * Renders one card per coach showing the 4 KPI factors + final score.
     *
     * ┌──────────────────────────────────────────────────────────────┐
     * │ 🥇 Prénom NOM ↑ +3 séances [83.4] │
     * │ Occupation 35% ████████████░░ 72.0% │
     * │ Fidélité 20% ████████░░░░░░ 60.0% │
     * │ Note (/5) 35% █████████████░ 4.2 / 5 │
     * │ Tendance 10% ██████░░░░░░░░ +3 séances │
     * └──────────────────────────────────────────────────────────────┘
     */
    private void buildLeaderboard() {
        if (perfData.isEmpty()) {
            Label empty = new Label("⚠ Aucune donnée de performance disponible.");
            empty.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 13px; -fx-padding: 30;");
            chartArea.setContent(empty);
            return;
        }

        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #F8F9FA;");

        // Medal colors for top 3
        String[] medals = { "🥇", "🥈", "🥉" };
        String[] cardColors = { "#FFFDE7", "#F3EEF8", "#E8F5E9" };
        String[] scoreColors = { "#F39C12", "#8E44AD", "#27AE60" };

        for (int idx = 0; idx < perfData.size(); idx++) {
            CoachPerformanceDto d = perfData.get(idx);

            String cardBg = idx < 3 ? cardColors[idx] : "#FFFFFF";
            String scoreFg = idx < 3 ? scoreColors[idx] : "#2C3E50";
            String rankLabel = idx < 3 ? medals[idx] : "#" + (idx + 1);

            // ── Card ─────────────────────────────────────────────────────────
            VBox card = new VBox(6);
            card.setPadding(new Insets(10, 14, 10, 14));
            card.setStyle("-fx-background-color: " + cardBg + "; " +
                    "-fx-background-radius: 10; " +
                    "-fx-border-color: #E0E0E0; -fx-border-radius: 10; -fx-border-width: 1;");

            // ── Header: rank | name | tendance chip | score badge ────────────
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label rankLbl = new Label(rankLabel);
            rankLbl.setStyle("-fx-font-size: 14px; -fx-min-width: 30;");

            String fullName = d.getNomComplet() != null ? d.getNomComplet() : "Inconnu";
            Label nameLbl = new Label(fullName);
            nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1A3C5E;");
            HBox.setHgrow(nameLbl, Priority.ALWAYS);

            int t = d.getTendance();
            String arrow = t >= 0 ? "↑" : "↓";
            String chipBg = t >= 0 ? "#E8F8F0" : "#FDEDEC";
            String chipColor = t >= 0 ? "#27AE60" : "#E74C3C";
            Label tendLbl = new Label(arrow + " " + (t >= 0 ? "+" : "") + t + " séances");
            tendLbl.setStyle("-fx-background-color: " + chipBg + "; -fx-text-fill: " + chipColor + "; " +
                    "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;");

            Label scoreLbl = new Label(String.format("%.1f", d.getScoreFinal()));
            scoreLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + scoreFg + "; " +
                    "-fx-min-width: 52; -fx-alignment: CENTER;");

            header.getChildren().addAll(rankLbl, nameLbl, tendLbl, scoreLbl);

            // ── 4 KPI bars ───────────────────────────────────────────────────
            VBox kpisBox = new VBox(4);

            double noteNorm = Math.min(100, d.getNotePonderee() / 5.0 * 100.0);
            double tendNorm = Math.min(100, Math.max(0, (d.getTendance() + 10) * 5.0));

            String[][] kpis = {
                    { "Occupation", "35%", "#3498DB",
                            String.format("%.1f%%", d.getTauxOccupation()),
                            String.valueOf(d.getTauxOccupation() / 100.0) },
                    { "Fidélité", "20%", "#2ECC71",
                            String.format("%.1f%%", d.getScoreFidelite()),
                            String.valueOf(d.getScoreFidelite() / 100.0) },
                    { "Note (/5)", "35%", "#9B59B6",
                            String.format("%.2f / 5", d.getNotePonderee()),
                            String.valueOf(noteNorm / 100.0) },
                    { "Tendance", "10%", t >= 0 ? "#27AE60" : "#E74C3C",
                            (t >= 0 ? "+" : "") + t + " séances",
                            String.valueOf(tendNorm / 100.0) },
            };

            for (String[] kpi : kpis) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);

                Label kpiLbl = new Label(kpi[0]);
                kpiLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #555; -fx-min-width: 82;");
                Label weightLbl = new Label(kpi[1]);
                weightLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #AAA; -fx-min-width: 28;");

                ProgressBar bar = new ProgressBar(Double.parseDouble(kpi[4]));
                bar.setPrefWidth(180);
                bar.setPrefHeight(11);
                bar.setStyle("-fx-accent: " + kpi[2] + ";");

                Label valLbl = new Label(kpi[3]);
                valLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + kpi[2] + ";");

                row.getChildren().addAll(kpiLbl, weightLbl, bar, valLbl);
                kpisBox.getChildren().add(row);
            }

            card.getChildren().addAll(header, kpisBox);
            container.getChildren().add(card);
        }

        chartArea.setContent(container);
        chartArea.setStyle("-fx-background: #F8F9FA; -fx-background-color: #F8F9FA;");
    }
}
