package edu.Connexion3A7.tests;

/**
 * Launcher class that does NOT extend Application.
 * This bypasses the JavaFX module-path check that causes
 * "des composants d'exécution JavaFX obligatoires sont manquants"
 * when running from IntelliJ with classpath mode.
 *
 * Use this class as your Run Configuration main class in IntelliJ.
 */
public class AppLauncher {
    public static void main(String[] args) {
        MainFx.main(args);
    }
}
