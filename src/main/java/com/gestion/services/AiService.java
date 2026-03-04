package com.gestion.services;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AiService {

    private static final String API_KEY = "AIzaSyA-TGayJAFudbZyzBS6e4ljTzgnW_E5kBo";

    public static String askAI(String prompt) {

        try {

            // 🔹 Build Gemini JSON body
            JsonObject requestJson = new JsonObject();

            JsonArray contents = new JsonArray();
            JsonObject contentObj = new JsonObject();

            JsonArray parts = new JsonArray();
            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", prompt);

            parts.add(textPart);
            contentObj.add("parts", parts);
            contents.add(contentObj);

            requestJson.add("contents", contents);

            String requestBody = requestJson.toString();

            String endpoint =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                            + API_KEY;

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();

            InputStream is = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            System.out.println("Gemini response: " + response);

            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            if (jsonResponse.has("error")) {
                return "AI Error: " +
                        jsonResponse.getAsJsonObject("error")
                                .get("message").getAsString();
            }

            return jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text")
                    .getAsString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "AI error occurred.";
    }
}