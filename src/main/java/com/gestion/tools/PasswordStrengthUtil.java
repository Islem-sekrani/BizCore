package com.gestion.tools;

import java.util.ArrayList;
import java.util.List;

public class PasswordStrengthUtil {

    public static int calculateScore(String password) {

        if (password == null) return 0;

        int score = 0;

        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[@$!%*?&].*")) score++;

        return score;
    }

    public static String getStrengthLabel(int score) {
        if (score <= 2) return "Weak";
        if (score == 3 || score == 4) return "Medium";
        return "Strong";
    }

    public static List<String> getMissingCriteria(String password) {

        List<String> missing = new ArrayList<>();

        if (password.length() < 8)
            missing.add("At least 8 characters");

        if (!password.matches(".*[A-Z].*"))
            missing.add("1 uppercase letter");

        if (!password.matches(".*[a-z].*"))
            missing.add("1 lowercase letter");

        if (!password.matches(".*\\d.*"))
            missing.add("1 number");

        if (!password.matches(".*[@$!%*?&].*"))
            missing.add("1 special character (@$!%*?&)");

        return missing;
    }
}