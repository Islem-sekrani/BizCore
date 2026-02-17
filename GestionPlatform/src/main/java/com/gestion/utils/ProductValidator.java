package com.gestion.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Classe utilitaire pour la validation des champs de formulaire de produits
 */
public class ProductValidator {

    // Patterns de validation
    private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile("^[A-Z][a-z\\s]+$");
    private static final Pattern PRICE_PATTERN = Pattern.compile("^\\d+\\.\\d+$");
    private static final Pattern STOCK_PATTERN = Pattern.compile("^\\d+$");

    /**
     * Valide le nom du produit
     * Règles: Non vide, commence par une majuscule, reste en minuscules
     */
    public static ValidationResult validateProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Le nom du produit est obligatoire.");
        }

        if (!PRODUCT_NAME_PATTERN.matcher(name.trim()).matches()) {
            return new ValidationResult(false,
                    "Le nom doit commencer par une majuscule et le reste doit être en minuscules (ex: 'Produit super cool').");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Valide la description du produit
     * Règles: Non vide, minimum 20 caractères
     */
    public static ValidationResult validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return new ValidationResult(false, "La description est obligatoire.");
        }

        if (description.trim().length() < 20) {
            return new ValidationResult(false,
                    "La description doit contenir au minimum 20 caractères (actuellement: " +
                            description.trim().length() + " caractères).");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Valide le prix du produit
     * Règles: Non vide, format décimal avec point (ex: 25.26)
     */
    public static ValidationResult validatePrice(String price) {
        if (price == null || price.trim().isEmpty()) {
            return new ValidationResult(false, "Le prix est obligatoire.");
        }

        // Enlever le symbole € s'il existe
        String cleanPrice = price.trim().replace("€", "").trim();

        if (!PRICE_PATTERN.matcher(cleanPrice).matches()) {
            return new ValidationResult(false,
                    "Le prix doit obligatoirement contenir un point entre les chiffres (ex: 25.50).");
        }

        try {
            double priceValue = Double.parseDouble(cleanPrice);
            if (priceValue <= 0) {
                return new ValidationResult(false, "Le prix doit être supérieur à 0.");
            }
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Le prix n'est pas un nombre valide.");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Valide le stock disponible
     * Règles: Non vide, uniquement des chiffres, valeur valide
     */
    public static ValidationResult validateStock(String stock) {
        if (stock == null || stock.trim().isEmpty()) {
            return new ValidationResult(false, "Le stock disponible est obligatoire.");
        }

        if (!STOCK_PATTERN.matcher(stock.trim()).matches()) {
            return new ValidationResult(false,
                    "Le stock doit contenir uniquement des chiffres.");
        }

        try {
            int stockValue = Integer.parseInt(stock.trim());
            if (stockValue < 0) {
                return new ValidationResult(false, "Le stock ne peut pas être négatif.");
            }
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Le stock n'est pas un nombre valide.");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Valide la catégorie du produit
     * Règles: Non vide, obligatoire
     */
    public static ValidationResult validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return new ValidationResult(false, "La catégorie est obligatoire.");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Valide l'image du produit
     * Règles: Non vide, obligatoire
     */
    public static ValidationResult validateImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return new ValidationResult(false, "L'image du produit est obligatoire.");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Valide le statut du produit
     * Règles: Non vide, obligatoire
     */
    public static ValidationResult validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return new ValidationResult(false, "Le statut est obligatoire.");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Valide tous les champs d'un produit
     * Retourne une liste de tous les messages d'erreur
     */
    public static List<String> validateAllFields(String name, String description, String price,
            String stock, String category, String imagePath,
            String status) {
        List<String> errors = new ArrayList<>();

        ValidationResult nameResult = validateProductName(name);
        if (!nameResult.isValid())
            errors.add("• " + nameResult.getMessage());

        ValidationResult descResult = validateDescription(description);
        if (!descResult.isValid())
            errors.add("• " + descResult.getMessage());

        ValidationResult priceResult = validatePrice(price);
        if (!priceResult.isValid())
            errors.add("• " + priceResult.getMessage());

        ValidationResult stockResult = validateStock(stock);
        if (!stockResult.isValid())
            errors.add("• " + stockResult.getMessage());

        ValidationResult categoryResult = validateCategory(category);
        if (!categoryResult.isValid())
            errors.add("• " + categoryResult.getMessage());

        ValidationResult imageResult = validateImage(imagePath);
        if (!imageResult.isValid())
            errors.add("• " + imageResult.getMessage());

        ValidationResult statusResult = validateStatus(status);
        if (!statusResult.isValid())
            errors.add("• " + statusResult.getMessage());

        return errors;
    }

    /**
     * Classe interne pour représenter le résultat d'une validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
