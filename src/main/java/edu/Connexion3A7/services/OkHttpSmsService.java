package edu.Connexion3A7.services;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import edu.Connexion3A7.tools.MyConnection;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Sends booking-confirmation SMS via the Twilio REST API.
 * Uses OkHttp (already in pom.xml) — no Twilio SDK required.
 *
 * Credentials are read from config.properties:
 * twilio.accountSid = ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 * twilio.authToken = your_auth_token_here
 * twilio.fromNumber = +1XXXXXXXXXX
 */
public class OkHttpSmsService {

    private static final String API_TEMPLATE = "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final boolean testMode;  // Mode test pour simuler l'envoi SMS
    private final OkHttpClient client = new OkHttpClient();

    // ── Singleton so credentials are only loaded once per session ─────────────
    private static OkHttpSmsService instance;

    public static OkHttpSmsService getInstance() {
        if (instance == null)
            instance = new OkHttpSmsService();
        return instance;
    }

    private OkHttpSmsService() {
        Properties props = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/config.properties")) {
            if (in == null)
                throw new RuntimeException("config.properties introuvable");
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture config.properties: " + e.getMessage());
        }
        this.accountSid = props.getProperty("twilio.accountSid", "").trim();
        this.authToken = props.getProperty("twilio.authToken", "").trim();
        this.fromNumber = props.getProperty("twilio.fromNumber", "").trim();
        this.testMode = Boolean.parseBoolean(props.getProperty("sms.test.mode", "false"));
        
        if (testMode) {
            System.out.println("╔═══════════════════════════════════════════════════════════╗");
            System.out.println("║  ⚠️  MODE TEST SMS ACTIVÉ - Aucun SMS réel ne sera envoyé ║");
            System.out.println("║  Pour envoyer de vrais SMS, changez sms.test.mode=false  ║");
            System.out.println("╚═══════════════════════════════════════════════════════════╝");
        }
    }

    /**
     * Returns true if real credentials have been configured.
     * The placeholder values that ship in config.properties all start with
     * "AC" + 32 chars but the accountSid placeholder is exactly that pattern —
     * we treat any value starting with "ACxxx" or "your_" as unconfigured.
     */
    public boolean isConfigured() {
        return !accountSid.startsWith("ACxxxxxxx")
                && !authToken.startsWith("your_")
                && !fromNumber.startsWith("+1XXXXX");
    }

    // ── Phone number validation and formatting ───────────────────────────────

    /**
     * Normalizes a phone number to E.164 format.
     * Handles common Tunisian formats and auto-corrects them.
     * 
     * @param phoneNumber Raw phone number (may be missing country code or +)
     * @return E.164 formatted number (e.g., "+21658410216") or null if invalid
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }

        // Remove all spaces, dashes, parentheses, and dots
        String cleaned = phoneNumber.replaceAll("[\\s\\-().]+", "");

        // Already in E.164 format
        if (cleaned.startsWith("+216") && cleaned.length() == 12) {
            return cleaned;
        }

        // Missing + but has country code (216...)
        if (cleaned.startsWith("216") && cleaned.length() == 11) {
            return "+" + cleaned;
        }

        // Local Tunisian number (8 digits starting with 2, 4, 5, 7, or 9)
        if (cleaned.matches("[24579]\\d{7}") && cleaned.length() == 8) {
            return "+216" + cleaned;
        }

        // Invalid format
        System.err.println("[SMS] Format de numéro invalide: " + phoneNumber + 
                          " (attendu: +21658410216 ou 58410216)");
        return null;
    }

    // ── Core send method ──────────────────────────────────────────────────────

    /**
     * Sends an SMS asynchronously (non-blocking).
     * Automatically normalizes phone numbers to E.164 format.
     *
     * @param toNumber Phone number (will be auto-corrected to E.164 if possible)
     * @param body     Message text (≤ 160 chars for single segment)
     */
    public void sendSmsAsync(String toNumber, String body) {
        // Normalize phone number to E.164 format
        String normalizedNumber = normalizePhoneNumber(toNumber);
        if (normalizedNumber == null) {
            System.out.println("[SMS] Numéro destinataire invalide — SMS ignoré.");
            return;
        }

        // Log if number was corrected
        if (!toNumber.equals(normalizedNumber)) {
            System.out.println("[SMS] Numéro corrigé: " + toNumber + " → " + normalizedNumber);
        }

        // MODE TEST: Simulate SMS without calling Twilio API
        if (testMode) {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║               📱 SMS SIMULÉ (MODE TEST)                    ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("║  Destinataire: " + normalizedNumber);
            System.out.println("║  De: " + fromNumber);
            System.out.println("║  Message:");
            for (String line : body.split("\n")) {
                System.out.println("║    " + line);
            }
            System.out.println("║  Longueur: " + body.length() + " caractères");
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");
            return;  // Don't make actual API call
        }

        // Real SMS mode - check credentials
        if (!isConfigured()) {
            System.out.println("[SMS] Credentials Twilio non configurés — SMS ignoré.");
            return;
        }

        String url = String.format(API_TEMPLATE, accountSid);

        RequestBody form = new FormBody.Builder()
                .add("To", normalizedNumber)
                .add("From", fromNumber)
                .add("Body", body)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", Credentials.basic(accountSid, authToken))
                .post(form)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("[SMS] Echec réseau: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody rb = response.body()) {
                    if (response.isSuccessful()) {
                        System.out.println("[SMS] Envoyé avec succès (HTTP " + response.code() + ")");
                    } else {
                        System.err.println("[SMS] Erreur Twilio HTTP " + response.code()
                                + ": " + (rb != null ? rb.string() : ""));
                    }
                }
            }
        });
    }


    // ── Booking confirmation helper ────────────────────────────────────────────

    /**
     * Sends a session-reminder SMS to the COACH.
     * The user's in-app notification already handles their side;
     * this SMS targets the coach only, informing them that a user has booked
     * a session with them on the selected date.
     *
     * @param userName   User's display name (the person who made the booking)
     * @param coachName  Full coach name (the recipient)
     * @param coachPhone Coach phone in E.164 format (the recipient)
     * @param domaine    Coach domain (e.g. "LEADERSHIP")
     * @param dateStr    Session date chosen by the user (e.g. "Mercredi
     *                   25/02/2026")
     */
    public void sendBookingConfirmation(String userName,
            String userPhone, // kept in signature for backwards-compat, no longer used
            String coachName,
            String coachPhone,
            String domaine,
            String dateStr) {
        // Build a SHORT message for Trial account (max 160 chars = 1 segment)
        String body = String.format(
                "BizCore: Nouvelle réservation\n" +
                        "Client: %s\n" +
                        "Date: %s",
                abbreviate(userName, 25),
                dateStr);
        sendSmsAsync(coachPhone, body);
    }

    // ── DB helper: look up caller's phone ─────────────────────────────────────

    /**
     * Fetches the phone number of a user from the `users` table.
     * Returns null if not set.
     */
    public String fetchUserPhone(int userId) {
        String sql = "SELECT telephone FROM users WHERE id_user = ?";
        try (Connection c = MyConnection.getInstance().getCnx();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("telephone");
        } catch (SQLException e) {
            System.err.println("[SMS] Erreur lecture téléphone: " + e.getMessage());
        }
        return null;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static String abbreviate(String s, int max) {
        if (s == null)
            return "Utilisateur";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
