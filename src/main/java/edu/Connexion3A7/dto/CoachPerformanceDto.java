package edu.Connexion3A7.dto;

/**
 * DTO transportant les indicateurs de performance calculés pour un coach.
 * Utilisé par StatistiqueService et affiché dans le TableView admin.
 */
public class CoachPerformanceDto {

    /** Prénom + Nom du coach */
    private String nomComplet;

    /** Taux d'occupation sur les 30 derniers jours (0–100 %) */
    private double tauxOccupation;

    /**
     * Delta de réservations confirmées : mois courant - mois précédent.
     * Positif = tendance haussière, négatif = baisse.
     */
    private int tendance;

    /** Pourcentage de clients fidèles (ayant réservé plus d'une fois) — 0 à 100 */
    private double scoreFidelite;

    /**
     * Note pondérée par le volume d'avis (formule bayésienne) — 0 à 5.
     * Formule : (AVG(rating) * COUNT(rating)) / (COUNT(rating) + 5)
     */
    private double notePonderee;

    /** Score composite final sur 100 */
    private double scoreFinal;

    /** Rang du coach dans le classement (1 = meilleur) */
    private int classement;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    public CoachPerformanceDto(String nomComplet,
            double tauxOccupation,
            int tendance,
            double scoreFidelite,
            double notePonderee,
            double scoreFinal,
            int classement) {
        this.nomComplet = nomComplet;
        this.tauxOccupation = tauxOccupation;
        this.tendance = tendance;
        this.scoreFidelite = scoreFidelite;
        this.notePonderee = notePonderee;
        this.scoreFinal = scoreFinal;
        this.classement = classement;
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String v) {
        this.nomComplet = v;
    }

    public double getTauxOccupation() {
        return tauxOccupation;
    }

    public void setTauxOccupation(double v) {
        this.tauxOccupation = v;
    }

    public int getTendance() {
        return tendance;
    }

    public void setTendance(int v) {
        this.tendance = v;
    }

    public double getScoreFidelite() {
        return scoreFidelite;
    }

    public void setScoreFidelite(double v) {
        this.scoreFidelite = v;
    }

    public double getNotePonderee() {
        return notePonderee;
    }

    public void setNotePonderee(double v) {
        this.notePonderee = v;
    }

    public double getScoreFinal() {
        return scoreFinal;
    }

    public void setScoreFinal(double v) {
        this.scoreFinal = v;
    }

    public int getClassement() {
        return classement;
    }

    public void setClassement(int v) {
        this.classement = v;
    }

    @Override
    public String toString() {
        return String.format("CoachPerformanceDto{classement=%d, nom='%s', score=%.1f}",
                classement, nomComplet, scoreFinal);
    }
}
