package edu.Connexion3A7.services;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Chatbot service backed by the Hugging Face Inference API (OpenAI-compatible
 * endpoint).
 *
 * Configuration (config.properties):
 * huggingface.api.key – your HF access token (hf_…)
 * huggingface.api.url –
 * https://api-inference.huggingface.co/v1/chat/completions
 * huggingface.model – e.g. HuggingFaceH4/zephyr-7b-beta
 * huggingface.max.tokens – integer, default 500
 * huggingface.temperature– double, default 0.7
 * chatbot.system.prompt – persona injected as the system message
 */
public class ChatbotService {

    private static ChatbotService instance;

    private final OkHttpClient httpClient;
    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final int maxTokens;
    private final double temperature;
    private final String systemPrompt;
    private final List<JSONObject> conversationHistory;
    private String coachContext = "";

    private static final MediaType JSON_MEDIA = MediaType.get("application/json; charset=utf-8");

    // ── Constructor ──────────────────────────────────────────────────────────

    private ChatbotService() {
        Properties props = loadProperties();

        this.apiKey = props.getProperty("huggingface.api.key", "").trim();
        this.apiUrl = props.getProperty("huggingface.api.url",
                "https://api-inference.huggingface.co/v1/chat/completions");
        this.model = props.getProperty("huggingface.model",
                "HuggingFaceH4/zephyr-7b-beta");
        this.maxTokens = Integer.parseInt(
                props.getProperty("huggingface.max.tokens", "500"));
        this.temperature = Double.parseDouble(
                props.getProperty("huggingface.temperature", "0.7"));
        this.systemPrompt = props.getProperty("chatbot.system.prompt",
                "Tu es un assistant expert en coaching business et entrepreneuriat.");

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        this.conversationHistory = new ArrayList<>();
    }

    // ── Singleton ────────────────────────────────────────────────────────────

    public static synchronized ChatbotService getInstance() {
        if (instance == null) {
            instance = new ChatbotService();
        }
        return instance;
    }

    /** Force re-creation on next getInstance() — useful after config changes. */
    public static synchronized void resetInstance() {
        instance = null;
    }

    // ── Config loader ────────────────────────────────────────────────────────

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                System.err.println("[ChatbotService] config.properties introuvable dans le classpath.");
            }
        } catch (IOException e) {
            System.err.println("[ChatbotService] Erreur chargement config.properties: " + e.getMessage());
        }
        return props;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Returns true when a valid HuggingFace token is set.
     * HF tokens start with "hf_".
     */
    public boolean isConfigured() {
        return !apiKey.isEmpty()
                && !apiKey.equals("YOUR_HUGGINGFACE_TOKEN_HERE")
                && apiKey.startsWith("hf_");
    }

    /**
     * Sets the coach context data that will be injected into the system prompt.
     * Call this with the output of CoachService.buildCoachSummary() to make
     * the AI aware of available coaches in the database.
     */
    public void setCoachContext(String context) {
        this.coachContext = (context != null) ? context : "";
    }

    /**
     * Sends a message asynchronously using the HuggingFace Inference API.
     *
     * @param userMessage the user's text input
     * @param onSuccess   callback with the assistant reply
     * @param onError     callback with a human-readable error description
     */
    public void sendMessageAsync(String userMessage,
            Consumer<String> onSuccess,
            Consumer<String> onError) {

        if (!isConfigured()) {
            onError.accept(
                    "Token Hugging Face non configure.\n" +
                            "Ajoutez votre token (hf_...) dans config.properties\n" +
                            "sous la cle : huggingface.api.key");
            return;
        }

        // Append the new user message to history
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        conversationHistory.add(userMsg);

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(buildRequestBody().toString(), JSON_MEDIA))
                .build();

        httpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                // Roll back the optimistic append
                conversationHistory.remove(userMsg);
                onError.accept("Erreur reseau : " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody body = response.body()) {
                    if (!response.isSuccessful() || body == null) {
                        String rawDetail = (body != null) ? body.string() : "Reponse vide";
                        conversationHistory.remove(userMsg);
                        onError.accept(buildFriendlyError(response.code(), rawDetail));
                        return;
                    }

                    JSONObject json = new JSONObject(body.string());

                    // HF OpenAI-compatible response: choices[0].message.content
                    String reply = json
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                            .trim();

                    JSONObject assistantMsg = new JSONObject();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", reply);
                    conversationHistory.add(assistantMsg);

                    onSuccess.accept(reply);

                } catch (Exception e) {
                    conversationHistory.remove(userMsg);
                    onError.accept("Erreur parsing reponse : " + e.getMessage());
                }
            }
        });
    }

    /** Resets the conversation history (keeps the system prompt). */
    public void clearHistory() {
        conversationHistory.clear();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Converts raw API error responses (often HTML pages) into clean,
     * user-readable French messages.
     */
    private String buildFriendlyError(int httpCode, String rawBody) {
        switch (httpCode) {
            case 410:
                return "Le modele IA \"" + model + "\" n'est plus disponible.\n" +
                        "Changez le modele dans config.properties.\n" +
                        "Modeles recommandes :\n" +
                        "  • mistralai/Mistral-7B-Instruct-v0.2\n" +
                        "  • HuggingFaceH4/zephyr-7b-beta";
            case 401:
                return "Token Hugging Face invalide ou expire.\n" +
                        "Verifiez votre token dans config.properties.";
            case 429:
                return "Limite de requetes atteinte.\n" +
                        "Attendez quelques secondes avant de reessayer.";
            case 503:
                return "Le modele est en cours de chargement.\n" +
                        "Reessayez dans quelques secondes.";
            case 422:
                // Often means the prompt is too long
                return "Le message est trop long pour le modele.\n" +
                        "Essayez un message plus court.";
            default:
                // Strip HTML from the response body for cleaner display
                String clean = rawBody;
                if (clean.contains("<") && clean.contains(">")) {
                    clean = clean.replaceAll("<[^>]*>", "").trim();
                    // Collapse multiple whitespace/newlines
                    clean = clean.replaceAll("\\s+", " ").trim();
                    if (clean.length() > 200) {
                        clean = clean.substring(0, 200) + "...";
                    }
                }
                return "Erreur API (" + httpCode + "): " + clean;
        }
    }

    /**
     * Builds the JSON body for the OpenAI-compatible HF endpoint.
     * Limits context to the 20 most-recent messages to stay within token limits.
     */
    private JSONObject buildRequestBody() {
        JSONArray messages = new JSONArray();

        // System message first — includes coach context if available
        JSONObject sysMsg = new JSONObject();
        sysMsg.put("role", "system");
        String fullPrompt = systemPrompt;
        if (coachContext != null && !coachContext.isEmpty()) {
            fullPrompt += "\n\n--- DONNEES DES COACHS ---\n" + coachContext
                    + "\n--- FIN DES DONNEES ---\n"
                    + "Quand l'utilisateur demande un coach, recommande des coachs de la liste ci-dessus "
                    + "en fonction du domaine, du budget (tarif), de la note et de la disponibilite. "
                    + "Presente les recommandations de maniere claire avec le nom, domaine, tarif et note.";
        }
        sysMsg.put("content", fullPrompt);
        messages.put(sysMsg);

        // Rolling window of the last 20 turns
        int start = Math.max(0, conversationHistory.size() - 20);
        for (int i = start; i < conversationHistory.size(); i++) {
            messages.put(conversationHistory.get(i));
        }

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("messages", messages);
        body.put("max_tokens", maxTokens);
        body.put("temperature", temperature);
        return body;
    }
}
