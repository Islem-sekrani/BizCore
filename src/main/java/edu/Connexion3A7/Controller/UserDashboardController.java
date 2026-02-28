package edu.Connexion3A7.Controller;

import edu.Connexion3A7.entities.coach;
import edu.Connexion3A7.entities.user;
import edu.Connexion3A7.services.CoachService;
import edu.Connexion3A7.services.DisponibiliteService;
import edu.Connexion3A7.services.ReservationService;
import edu.Connexion3A7.tools.MyConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Unified user dashboard controller.
 * Merges the old UserDashboardController (coach cards + rating + booking)
 * with CoachSelectionController (weekly availability + date-based booking)
 * into one master-detail view.
 */
public class UserDashboardController {

    // ── Sidebar ──────────────────────────────────────────────────────────────
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Label sidebarInitial;

    // ── Top bar ──────────────────────────────────────────────────────────────
    @FXML
    private Circle profileCircle;
    @FXML
    private Label profileInitials;
    @FXML
    private Label breadcrumbLabel;

    // ── Left panel: coach cards ──────────────────────────────────────────────
    @FXML
    private VBox coachCardsContainer;
    @FXML
    private Label statusLabel;
    @FXML
    private Label coachCountLabel;

    // ── Right panel: availability ────────────────────────────────────────────
    @FXML
    private VBox noSelectionPlaceholder;
    @FXML
    private VBox availabilityPanel;
    @FXML
    private Label selectedCoachLabel;
    @FXML
    private Label weekLabel;
    @FXML
    private VBox dayGrid;
    @FXML
    private Button prevWeekBtn;
    @FXML
    private Button nextWeekBtn;
    @FXML
    private Button reserveButton;
    @FXML
    private Label reserveStatusLabel;

    // ── Chatbot overlay ──────────────────────────────────────────────────────
    @FXML
    private VBox chatbotOverlay;
    @FXML
    private Button chatbotFab;

    private boolean chatbotLoaded = false;
    private boolean chatbotVisible = false;

    // ── Services ─────────────────────────────────────────────────────────────
    private final CoachService coachService = new CoachService();
    private final DisponibiliteService dispoService = new DisponibiliteService();
    private final ReservationService reservationService = new ReservationService();

    // ── State ────────────────────────────────────────────────────────────────
    private user loggedInUser;
    private coach selectedCoach;
    private LocalDate currentWeekStart;
    private LocalDate selectedDate;
    private VBox selectedCoachCard;
    private HBox selectedDayRow;

    private static final String[] JOUR_FR = {
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"
    };
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] AVATAR_COLORS = {
            "#2ECC9B", "#3498DB", "#E67E22", "#9B59B6", "#E74C3C", "#1ABC9C"
    };

    // ═════════════════════════════════════════════════════════════════════════
    // Initialisation
    // ═════════════════════════════════════════════════════════════════════════

    public void setLoggedInUser(user u) {
        this.loggedInUser = u;
        if (u != null) {
            String initial = u.getEmail() != null && !u.getEmail().isEmpty()
                    ? String.valueOf(u.getEmail().charAt(0)).toUpperCase()
                    : "U";
            profileInitials.setText(initial);
            sidebarInitial.setText(initial);
            userNameLabel.setText(u.getEmail());
            userRoleLabel.setText("Utilisateur");
        }
        loadCoachCards();
    }

    @FXML
    public void initialize() {
        profileInitials.setText("U");
        currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        if (reserveButton != null) {
            reserveButton.setDisable(true);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // LEFT PANEL — Coach cards with rating + "📅 Disponibilité" button
    // ═════════════════════════════════════════════════════════════════════════

    private void loadCoachCards() {
        coachCardsContainer.getChildren().clear();

        // Reset right panel
        resetAvailabilityPanel();

        if (!MyConnection.getInstance().isConnected()) {
            statusLabel.setText("Base de donnees non disponible.");
            statusLabel.setStyle("-fx-text-fill: #E74C3C;");
            coachCountLabel.setText("0 coachs");
            return;
        }

        try {
            List<coach> coaches = coachService.getData();
            coachCountLabel.setText(coaches.size() + " coach(s)");

            if (coaches.isEmpty()) {
                Label empty = new Label("Aucun coach disponible pour le moment.");
                empty.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 14px; -fx-padding: 30;");
                coachCardsContainer.getChildren().add(empty);
            } else {
                for (coach c : coaches) {
                    coachCardsContainer.getChildren().add(createCoachCard(c));
                }
            }
            statusLabel.setText("");
        } catch (SQLException e) {
            showErrorAlert("Erreur chargement coachs", e.getMessage());
            statusLabel.setText("Erreur: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #E74C3C;");
            coachCountLabel.setText("Erreur");
        }
    }

    private VBox createCoachCard(coach c) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: #E8EAED; -fx-border-radius: 10; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 1);");

        // === Top section: Avatar + Name + Domain badge ===
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        Circle circle = new Circle(24);
        circle.setFill(Color.web(AVATAR_COLORS[Math.abs(c.getId_coach()) % AVATAR_COLORS.length]));
        Label initials = new Label(getInitials(c));
        initials.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        avatar.getChildren().addAll(circle, initials);

        VBox nameBox = new VBox(2);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label nameLabel = new Label(c.getNom() + " " + c.getPrenom());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: #2C3E50;");

        Label domainBadge = new Label(c.getDomaine() != null ? c.getDomaine() : "N/A");
        domainBadge.setStyle("-fx-background-color: #EBF5FB; -fx-text-fill: #2980B9; " +
                "-fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");

        nameBox.getChildren().addAll(nameLabel, domainBadge);

        // Dispo badge
        String dispoText = c.getDispo() != null ? c.getDispo() : "N/A";
        Label dispoBadge = new Label(dispoText);
        if ("Disponible".equalsIgnoreCase(dispoText)) {
            dispoBadge.setStyle("-fx-background-color: #D5F5E3; -fx-text-fill: #27AE60; " +
                    "-fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
        } else {
            dispoBadge.setStyle("-fx-background-color: #FADBD8; -fx-text-fill: #E74C3C; " +
                    "-fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
        }

        topRow.getChildren().addAll(avatar, nameBox, dispoBadge);

        // === Info row: tarif + experience ===
        HBox infoRow = new HBox(15);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        Label tarifLabel = new Label(String.format("💰 %.0f DT/H", c.getTarif()));
        tarifLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");
        Label expLabel = new Label("📅 " + c.getExperience() + " ans exp.");
        expLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");
        Label phoneLabel = new Label("📱 " + (c.getNumTel() != null ? c.getNumTel() : "N/A"));
        phoneLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");
        infoRow.getChildren().addAll(tarifLabel, expLabel, phoneLabel);

        // === Star rating ===
        HBox starBox = buildInteractiveStarRating(c);

        // === Action buttons ===
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(4, 0, 0, 0));

        boolean booked = false;
        try {
            if (loggedInUser != null) {
                booked = reservationService.isBooked(loggedInUser.getId_user(), c.getId_coach());
            }
        } catch (SQLException e) {
            System.out.println("Erreur check booking: " + e.getMessage());
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (booked) {
            Label bookedBadge = new Label("✅ Reservé");
            bookedBadge.setStyle("-fx-background-color: #D5F5E3; -fx-text-fill: #27AE60; " +
                    "-fx-padding: 4 10; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");

            Button cancelBtn = new Button("Annuler");
            cancelBtn.getStyleClass().add("btn-danger");
            cancelBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 12;");
            cancelBtn.setOnAction(e -> handleCancelBooking(c));

            buttonBox.getChildren().addAll(bookedBadge, spacer, cancelBtn);
        } else {
            buttonBox.getChildren().add(spacer);
        }

        // "📅 Disponibilité" button — opens the right availability panel
        boolean isIndisponible = "Indisponible".equalsIgnoreCase(c.getDispo());
        if (!isIndisponible) {
            Button dispoBtn = new Button("📅 Disponibilité");
            dispoBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; " +
                    "-fx-font-size: 11px; -fx-padding: 5 14; -fx-background-radius: 8; -fx-cursor: hand;");
            dispoBtn.setOnAction(e -> selectCoach(c, card));
            buttonBox.getChildren().add(dispoBtn);
        }

        card.getChildren().addAll(topRow, infoRow, starBox, buttonBox);

        // Hover effects
        card.setOnMouseEntered(e -> {
            if (card != selectedCoachCard) {
                card.setStyle("-fx-background-color: #F0FFF8; -fx-background-radius: 10; " +
                        "-fx-border-color: #2ECC9B; -fx-border-radius: 10; -fx-border-width: 1; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(46,204,155,0.15), 8, 0, 0, 2);");
            }
        });
        card.setOnMouseExited(e -> {
            if (card != selectedCoachCard) {
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-border-color: #E8EAED; -fx-border-radius: 10; -fx-border-width: 1; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 1);");
            }
        });

        return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // RIGHT PANEL — Weekly availability + date-based booking
    // ═════════════════════════════════════════════════════════════════════════

    private void selectCoach(coach c, VBox card) {
        // Remove highlight from previous
        if (selectedCoachCard != null) {
            selectedCoachCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-border-color: #E8EAED; -fx-border-radius: 10; -fx-border-width: 1; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 1);");
        }

        selectedCoach = c;
        selectedCoachCard = card;
        selectedDate = null;
        selectedDayRow = null;

        // Highlight selected card
        card.setStyle("-fx-background-color: #E8F8F0; -fx-background-radius: 10; " +
                "-fx-border-color: #2ECC9B; -fx-border-radius: 10; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(46,204,155,0.25), 10, 0, 0, 3);");

        // Show availability panel, hide placeholder
        noSelectionPlaceholder.setVisible(false);
        noSelectionPlaceholder.setManaged(false);
        availabilityPanel.setVisible(true);
        availabilityPanel.setManaged(true);

        selectedCoachLabel.setText(c.getNom() + " " + c.getPrenom() + " — " +
                (c.getDomaine() != null ? c.getDomaine() : ""));

        reserveButton.setDisable(true);
        reserveStatusLabel.setText("");

        loadWeekAvailability();
    }

    private void resetAvailabilityPanel() {
        selectedCoach = null;
        selectedCoachCard = null;
        selectedDate = null;
        selectedDayRow = null;

        if (noSelectionPlaceholder != null) {
            noSelectionPlaceholder.setVisible(true);
            noSelectionPlaceholder.setManaged(true);
        }
        if (availabilityPanel != null) {
            availabilityPanel.setVisible(false);
            availabilityPanel.setManaged(false);
        }
    }

    private void loadWeekAvailability() {
        dayGrid.getChildren().clear();
        selectedDate = null;
        selectedDayRow = null;
        reserveButton.setDisable(true);

        if (selectedCoach == null)
            return;

        LocalDate weekEnd = currentWeekStart.plusDays(6);
        weekLabel.setText("Semaine du " + currentWeekStart.format(DATE_FMT) +
                " au " + weekEnd.format(DATE_FMT));

        try {
            Map<LocalDate, String> week = dispoService.getWeekAvailability(
                    selectedCoach.getId_coach(), currentWeekStart);

            for (Map.Entry<LocalDate, String> entry : week.entrySet()) {
                LocalDate day = entry.getKey();
                String statut = entry.getValue();
                dayGrid.getChildren().add(buildDayRow(day, statut));
            }
        } catch (SQLException e) {
            Label err = new Label("Erreur chargement disponibilites: " + e.getMessage());
            err.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");
            dayGrid.getChildren().add(err);
        }
    }

    private HBox buildDayRow(LocalDate day, String statut) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));

        boolean available = "Disponible".equalsIgnoreCase(statut);
        boolean bookedByMe = false;
        boolean bookedByOther = false;

        try {
            if (loggedInUser != null) {
                bookedByMe = reservationService.isBookedOnDate(
                        loggedInUser.getId_user(), selectedCoach.getId_coach(), day);
            }
            if (!bookedByMe) {
                bookedByOther = reservationService.isCoachBookedOnDate(
                        selectedCoach.getId_coach(), day);
            }
        } catch (SQLException ignored) {
        }

        // Day name
        int dayIdx = day.getDayOfWeek().getValue() - 1;
        Label dayName = new Label(JOUR_FR[dayIdx]);
        dayName.setMinWidth(90);
        dayName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        dayName.setStyle("-fx-text-fill: #2C3E50;");

        // Date
        Label dateLabel = new Label(day.format(DATE_FMT));
        dateLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (bookedByMe) {
            // ── Current user's booking → show badge + cancel button ──
            Label badge = new Label("✅ Mon RDV");
            badge.getStyleClass().add("cs-badge-booked");

            Button cancelBtn = new Button("❌ Annuler");
            cancelBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                    "-fx-font-size: 10px; -fx-padding: 4 10; -fx-background-radius: 6; -fx-cursor: hand;");
            cancelBtn.setOnAction(e -> handleCancelOnDate(day));

            row.getChildren().addAll(dayName, dateLabel, spacer, badge, cancelBtn);
            row.getStyleClass().add("cs-day-row-booked");

        } else if (bookedByOther) {
            // ── Another user's booking → show "Occupé" (not clickable) ──
            Label badge = new Label("🔒 Occupé");
            badge.setStyle("-fx-background-color: #FADBD8; -fx-text-fill: #C0392B; " +
                    "-fx-padding: 4 10; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");

            row.getChildren().addAll(dayName, dateLabel, spacer, badge);
            row.getStyleClass().add("cs-day-row-unavailable");

        } else if (available) {
            // ── Available → clickable for booking ──
            Label badge = new Label("Disponible");
            badge.getStyleClass().add("cs-badge-available");
            row.getStyleClass().add("cs-day-row-available");
            row.setOnMouseClicked(e -> selectDay(day, row));

            row.getChildren().addAll(dayName, dateLabel, spacer, badge);

        } else {
            // ── Unavailable ──
            Label badge = new Label("Indisponible");
            badge.getStyleClass().add("cs-badge-unavailable");
            row.getStyleClass().add("cs-day-row-unavailable");

            row.getChildren().addAll(dayName, dateLabel, spacer, badge);
        }

        return row;
    }

    /**
     * Cancel a date-specific booking and refresh the availability calendar.
     */
    private void handleCancelOnDate(LocalDate date) {
        if (loggedInUser == null || selectedCoach == null)
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Annuler votre reservation du " + date.format(DATE_FMT) +
                        " avec " + selectedCoach.getNom() + " " + selectedCoach.getPrenom() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Annuler la reservation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    reservationService.removeReservationOnDate(
                            loggedInUser.getId_user(), selectedCoach.getId_coach(), date);
                    showInfoAlert("Succes", "Reservation annulee pour le " + date.format(DATE_FMT));
                    loadWeekAvailability(); // refresh calendar
                    loadCoachCards(); // refresh left panel booking badges
                } catch (SQLException e) {
                    showErrorAlert("Erreur annulation", e.getMessage());
                }
            }
        });
    }

    private void selectDay(LocalDate day, HBox row) {
        if (selectedDayRow != null) {
            selectedDayRow.getStyleClass().remove("cs-day-row-selected");
        }

        selectedDate = day;
        selectedDayRow = row;
        row.getStyleClass().add("cs-day-row-selected");

        reserveButton.setDisable(false);
        reserveStatusLabel.setText(JOUR_FR[day.getDayOfWeek().getValue() - 1] +
                " " + day.format(DATE_FMT) + " selectionne");
    }

    // ── Week navigation ──────────────────────────────────────────────────────

    @FXML
    void handlePrevWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1);
        loadWeekAvailability();
    }

    @FXML
    void handleNextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1);
        loadWeekAvailability();
    }

    // ── Reserve (date-based) ─────────────────────────────────────────────────

    @FXML
    void handleReserve() {
        if (loggedInUser == null) {
            showErrorAlert("Erreur", "Vous devez etre connecte.");
            return;
        }
        if (selectedCoach == null || selectedDate == null) {
            showErrorAlert("Erreur", "Selectionnez un coach et un jour.");
            return;
        }

        if ("Indisponible".equalsIgnoreCase(selectedCoach.getDispo())) {
            showInfoAlert("Coach indisponible",
                    "Ce coach est indisponible, vous ne pouvez pas le reserver.");
            return;
        }

        // Verify day-level availability
        try {
            Map<LocalDate, String> week = dispoService.getWeekAvailability(
                    selectedCoach.getId_coach(), currentWeekStart);
            String dayStatus = week.get(selectedDate);
            if (!"Disponible".equalsIgnoreCase(dayStatus)) {
                showInfoAlert("Jour indisponible",
                        "Ce coach est indisponible ce jour-la.");
                return;
            }
        } catch (SQLException e) {
            showErrorAlert("Erreur verification", e.getMessage());
            return;
        }

        // Already booked?
        try {
            if (reservationService.isBookedOnDate(
                    loggedInUser.getId_user(), selectedCoach.getId_coach(), selectedDate)) {
                showInfoAlert("Deja reserve",
                        "Vous avez deja une reservation avec ce coach ce jour-la.");
                return;
            }
        } catch (SQLException e) {
            showErrorAlert("Erreur", e.getMessage());
            return;
        }

        // Confirm
        String msg = "Reserver " + selectedCoach.getNom() + " " + selectedCoach.getPrenom() +
                "\nle " + JOUR_FR[selectedDate.getDayOfWeek().getValue() - 1] +
                " " + selectedDate.format(DATE_FMT) + " ?";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmer la reservation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    reservationService.addReservation(
                            loggedInUser.getId_user(), selectedCoach.getId_coach(), selectedDate);
                    showInfoAlert("Reservation confirmee",
                            "Votre seance est reservee pour le " + selectedDate.format(DATE_FMT));
                    reserveStatusLabel.setText("Reservation confirmee !");
                    reserveStatusLabel.setStyle(
                            "-fx-text-fill: #27AE60; -fx-font-weight: bold; -fx-font-size: 11px;");
                    loadWeekAvailability();
                } catch (SQLException e) {
                    showErrorAlert("Erreur reservation", e.getMessage());
                }
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Interactive star rating
    // ═════════════════════════════════════════════════════════════════════════

    private HBox buildInteractiveStarRating(coach c) {
        HBox box = new HBox(3);
        box.setAlignment(Pos.CENTER_LEFT);

        int currentRating = Math.round(c.getNote());
        Label[] stars = new Label[5];

        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < currentRating ? "★" : "☆");
            star.setStyle(i < currentRating
                    ? "-fx-font-size: 16px; -fx-text-fill: #F1C40F; -fx-cursor: hand;"
                    : "-fx-font-size: 16px; -fx-text-fill: #BDC3C7; -fx-cursor: hand;");
            stars[i] = star;
            box.getChildren().add(star);
        }

        Label ratingValueLabel = new Label(String.format(" %.1f/5", c.getNote()));
        ratingValueLabel.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: #2C3E50; -fx-font-weight: bold; -fx-padding: 0 0 0 4;");
        box.getChildren().add(ratingValueLabel);

        for (int i = 0; i < 5; i++) {
            final int starIndex = i + 1;
            final Label rvl = ratingValueLabel;

            stars[i].setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++) {
                    stars[j].setText(j < starIndex ? "★" : "☆");
                    stars[j].setStyle(j < starIndex
                            ? "-fx-font-size: 16px; -fx-text-fill: #F1C40F; -fx-cursor: hand;"
                            : "-fx-font-size: 16px; -fx-text-fill: #BDC3C7; -fx-cursor: hand;");
                }
            });

            stars[i].setOnMouseExited(e -> {
                int cur = Math.round(c.getNote());
                for (int j = 0; j < 5; j++) {
                    stars[j].setText(j < cur ? "★" : "☆");
                    stars[j].setStyle(j < cur
                            ? "-fx-font-size: 16px; -fx-text-fill: #F1C40F; -fx-cursor: hand;"
                            : "-fx-font-size: 16px; -fx-text-fill: #BDC3C7; -fx-cursor: hand;");
                }
            });

            stars[i].setOnMouseClicked(e -> {
                if (loggedInUser == null) {
                    showErrorAlert("Erreur", "Vous devez etre connecte pour noter un coach.");
                    return;
                }
                try {
                    coachService.addOrUpdateUserRating(
                            loggedInUser.getId_user(), c.getId_coach(), starIndex);
                    float newAvg = coachService.getAverageRating(c.getId_coach());
                    c.setNote(newAvg);

                    int rounded = Math.round(newAvg);
                    for (int j = 0; j < 5; j++) {
                        stars[j].setText(j < rounded ? "★" : "☆");
                        stars[j].setStyle(j < rounded
                                ? "-fx-font-size: 16px; -fx-text-fill: #F1C40F; -fx-cursor: hand;"
                                : "-fx-font-size: 16px; -fx-text-fill: #BDC3C7; -fx-cursor: hand;");
                    }
                    rvl.setText(String.format(" %.1f/5", newAvg));
                } catch (SQLException ex) {
                    showErrorAlert("Erreur", "Impossible de mettre a jour la note: " + ex.getMessage());
                }
            });
        }

        return box;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Cancel booking
    // ═════════════════════════════════════════════════════════════════════════

    private void handleCancelBooking(coach c) {
        if (loggedInUser == null) {
            showErrorAlert("Erreur", "Utilisateur non connecte.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Annuler la reservation avec " + c.getNom() + " " + c.getPrenom() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Annuler la reservation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    reservationService.removeReservation(loggedInUser.getId_user(), c.getId_coach());
                    showInfoAlert("Succes", "Reservation annulee.");
                    loadCoachCards();
                } catch (SQLException e) {
                    showErrorAlert("Erreur annulation", e.getMessage());
                }
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Sidebar navigation
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    void handleCoachingClick(javafx.event.ActionEvent event) {
        loadCoachCards();
    }

    @FXML
    void handleEvenementsClick(javafx.event.ActionEvent event) {
        /* placeholder */
    }

    @FXML
    void handleBlogClick(javafx.event.ActionEvent event) {
        /* placeholder */
    }

    @FXML
    void handleProduitsClick(javafx.event.ActionEvent event) {
        /* placeholder */
    }

    @FXML
    void handleLogout(javafx.event.ActionEvent event) {
        try {
            loggedInUser = null;
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/edu/Connexion3A7/Controller/login.fxml"));
            Parent root = loader.load();
            coachCardsContainer.getScene().setRoot(root);
        } catch (IOException e) {
            showErrorAlert("Erreur deconnexion", e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Chatbot FAB toggle
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    void handleChatbotToggle() {
        if (!chatbotLoaded) {
            loadChatbotPanel();
        }

        chatbotVisible = !chatbotVisible;
        chatbotOverlay.setVisible(chatbotVisible);
        chatbotOverlay.setManaged(chatbotVisible);
        chatbotFab.setText(chatbotVisible ? "✕" : "💬");
    }

    private void loadChatbotPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/edu/Connexion3A7/Controller/chatbotPanel.fxml"));
            Parent panel = loader.load();
            chatbotOverlay.getChildren().setAll(panel);
            VBox.setVgrow(panel, Priority.ALWAYS);
            chatbotLoaded = true;
        } catch (IOException e) {
            System.err.println("[UserDashboard] Cannot load chatbot panel: " + e.getMessage());
            e.printStackTrace();

            Label errorLabel = new Label("Impossible de charger le chatbot:\n" + e.getMessage());
            errorLabel.setWrapText(true);
            errorLabel.setStyle("-fx-text-fill: #E74C3C; -fx-padding: 20;");
            chatbotOverlay.getChildren().setAll(errorLabel);
            chatbotLoaded = true;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═════════════════════════════════════════════════════════════════════════

    private String getInitials(coach c) {
        String init = "";
        if (c.getNom() != null && !c.getNom().isEmpty())
            init += c.getNom().charAt(0);
        if (c.getPrenom() != null && !c.getPrenom().isEmpty())
            init += c.getPrenom().charAt(0);
        return init.toUpperCase();
    }

    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
