package com.gestion.services;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ProductImageAnalyzer {

    public static class AnalysisResult {
        private boolean accepted;
        private String category;
        private String message;

        public AnalysisResult(boolean accepted, String category, String message) {
            this.accepted = accepted;
            this.category = category;
            this.message = message;
        }

        public boolean isAccepted() {
            return accepted;
        }

        public String getCategory() {
            return category;
        }

        public String getMessage() {
            return message;
        }
    }

    private static final java.util.List<String> VALID_CATEGORIES = Arrays.asList(
            "Formation", "Livre", "Abonnement", "Logiciel", "Service");

    /**
     * Analyse l'image (Simulation)
     * Pour tester : nommez votre fichier avec un mot clé (ex: "mon_livre.jpg",
     * "formation_java.png")
     */
    public AnalysisResult analyzeImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            return new AnalysisResult(false, "Aucune", "Fichier introuvable.");
        }

        String filename = imageFile.getName().toLowerCase();

        // Simulation de l'analyse IA basée sur le nom du fichier pour la démonstration
        String detectedCategory = null;

        if (filename.contains("livre") || filename.contains("book") || filename.contains("bouquin")) {
            detectedCategory = "Livre";
        } else if (filename.contains("formation") || filename.contains("course") || filename.contains("tuto")) {
            detectedCategory = "Formation";
        } else if (filename.contains("abonnement") || filename.contains("sub") || filename.contains("plan")) {
            detectedCategory = "Abonnement";
        } else if (filename.contains("logiciel") || filename.contains("soft") || filename.contains("app")) {
            detectedCategory = "Logiciel";
        } else if (filename.contains("service") || filename.contains("presta")) {
            detectedCategory = "Service";
        }

        if (detectedCategory != null) {
            return new AnalysisResult(true, detectedCategory,
                    "✅ Image acceptée. Catégorie détectée : " + detectedCategory);
        } else {
            return new AnalysisResult(false, "Aucune", "❌ Image refusée : Aucune catégorie valide ("
                    + String.join(", ", VALID_CATEGORIES) + ") n'a été clairement identifiée dans le nom du fichier.");
        }
    }
}
