package edu.Connexion3A7.Controller;

import edu.Connexion3A7.services.ChatbotService;
import edu.Connexion3A7.services.CoachService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatbotController {

    // ── FXML bindings ────────────────────────────────────────────────────────

    @FXML
    private Button clearButton;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox messagesContainer;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField messageInput;
    @FXML
    private Button sendButton;

    private final ChatbotService chatbotService = ChatbotService.getInstance();
    private final CoachService coachService = new CoachService();

    // ── Initialisation ───────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        loadingIndicator.setVisible(false);
        statusLabel.setText("");

        // Inject live coach data from DB into the chatbot's context
        try {
            String coachSummary = coachService.buildCoachSummary();
            chatbotService.setCoachContext(coachSummary);
        } catch (Exception e) {
            System.err.println("[ChatbotController] Could not load coach context: " + e.getMessage());
        }

        if (chatbotService.isConfigured()) {
            appendBotMessage(
                    "Bonjour ! Je suis votre assistant GestionCoach. 🤖\n\n" +
                            "Je peux vous aider a trouver le coach ideal :\n" +
                            "  • Dites-moi votre domaine (Leadership, Finance, Branding...)\n" +
                            "  • Indiquez votre budget maximum (ex: 50 DT/H)\n" +
                            "  • Demandez des conseils sportifs ou nutrition\n\n" +
                            "Comment puis-je vous aider ?");
        } else {
            appendBotMessage(
                    "Configuration manquante.\n\n" +
                            "Ajoutez votre token Hugging Face dans config.properties :\n" +
                            "  huggingface.api.key=hf_votre_token_ici\n\n" +
                            "Obtenez un token gratuit sur huggingface.co/settings/tokens");
            setInputEnabled(false);
            statusLabel.setText("Token HuggingFace non configure");
        }
    }

    // ── Event handlers ───────────────────────────────────────────────────────

    @FXML
    private void handleSendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty())
            return;

        messageInput.clear();
        appendUserMessage(text);
        setInputEnabled(false);
        loadingIndicator.setVisible(true);
        statusLabel.setText("En attente de la reponse...");

        chatbotService.sendMessageAsync(
                text,
                reply -> Platform.runLater(() -> {
                    appendBotMessage(reply);
                    loadingIndicator.setVisible(false);
                    statusLabel.setText("");
                    setInputEnabled(true);
                    messageInput.requestFocus();
                }),
                error -> Platform.runLater(() -> {
                    appendBotMessage("Erreur : " + error);
                    loadingIndicator.setVisible(false);
                    statusLabel.setText("Erreur de communication");
                    setInputEnabled(true);
                }));
    }

    @FXML
    private void handleClear() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Effacer tout l'historique de conversation ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Effacer la conversation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                chatbotService.clearHistory();
                messagesContainer.getChildren().clear();
                statusLabel.setText("");
                appendBotMessage(
                        "Conversation effacee. Comment puis-je vous aider ?");
            }
        });
    }

    // ── Static window opener (called from other controllers) ─────────────────

    /**
     * Opens the chatbot in a new, independent window.
     * Called by AjouterCoach and any sidebar navigation button.
     */
    public static void openChatbotWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ChatbotController.class.getResource(
                            "/fxml/chatbot.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 620, 700);

            // Load the stylesheet (null-safe)
            var css = ChatbotController.class.getResource("/styles/chatbot.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("Assistant GestionCoach");
            stage.setScene(scene);
            stage.setMinWidth(420);
            stage.setMinHeight(500);
            stage.show();

        } catch (IOException e) {
            System.err.println("[ChatbotController] Impossible d'ouvrir la fenetre : "
                    + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir le chatbot");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // ── Message bubble helpers ───────────────────────────────────────────────

    /** Appends a right-aligned blue bubble for the user's own text. */
    private void appendUserMessage(String text) {
        Label bubble = new Label(text);
        bubble.getStyleClass().add("user-bubble");
        bubble.setWrapText(true);
        // Bind to 70% of the container width so bubbles fit both the
        // standalone 620px window and the embedded 380px panel.
        bubble.maxWidthProperty().bind(
                messagesContainer.widthProperty().multiply(0.70));

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_RIGHT);
        HBox.setMargin(bubble, new Insets(2, 4, 2, 60));

        messagesContainer.getChildren().add(row);
        scrollToBottom();
    }

    /** Appends a left-aligned white bubble for the bot's reply. */
    private void appendBotMessage(String text) {
        Label bubble = new Label(text);
        bubble.getStyleClass().add("bot-bubble");
        bubble.setWrapText(true);
        bubble.maxWidthProperty().bind(
                messagesContainer.widthProperty().multiply(0.70));

        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(bubble, new Insets(2, 60, 2, 4));

        messagesContainer.getChildren().add(row);
        scrollToBottom();
    }

    /** Scrolls the message pane to the very bottom. */
    private void scrollToBottom() {
        // Defer until after layout pass so the new node has a position
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    // ── UI helpers ───────────────────────────────────────────────────────────

    private void setInputEnabled(boolean enabled) {
        messageInput.setDisable(!enabled);
        sendButton.setDisable(!enabled);
        clearButton.setDisable(!enabled);
    }
}
