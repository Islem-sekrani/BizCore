<?php

namespace App\Service;

use App\Entity\Coach;
use App\Repository\CoachRepository;
use App\Repository\RatingRepository;
use App\Repository\ReservationRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * Service métier pour calculer les statistiques et KPI des coachs
 * 
 * Calcule 4 KPI principaux :
 * 1. Taux d'occupation (35%)
 * 2. Note pondérée (35%)
 * 3. Fidélité client (20%)
 * 4. Tendance (10%)
 */
class CoachStatisticsService
{
    public function __construct(
        private CoachRepository $coachRepository,
        private ReservationRepository $reservationRepository,
        private EntityManagerInterface $em,
        private RatingRepository $ratingRepository,
    ) {}

    /**
     * Récupère toutes les statistiques des coachs, triées par score global
     * 
     * @return array<array> Liste des coachs avec leurs statistiques et score
     */
    public function getAllCoachesStatistics(): array
    {
        $coaches = $this->coachRepository->findAll();
        $stats = [];

        foreach ($coaches as $coach) {
            $stats[] = $this->calculateCoachStatistics($coach);
        }

        // Trier par score global (décroissant)
        usort($stats, fn($a, $b) => $b['scoreGlobal'] <=> $a['scoreGlobal']);

        // Ajouter le rang
        foreach ($stats as $key => $stat) {
            $stats[$key]['rang'] = $key + 1;
        }

        return $stats;
    }

    /**
     * Calcule toutes les statistiques d'un coach
     * 
     * @param Coach $coach Le coach à analyser
     * @return array Tableau contenant tous les KPI et le score global
     */
    private function calculateCoachStatistics(Coach $coach): array
    {
        $tauxOccupation = $this->calculateOccupationRate($coach);
        $notePonderee = $this->calculateWeightedRating($coach);
        $fidelite = $this->calculateClientLoyalty($coach);
        $tendance = $this->calculateTrend($coach);

        // Normaliser la tendance (0-200) sur 0-100 pour le score global
        $tendanceNormalisee = min($tendance / 2, 100.0);

        // Score global avec pondération : Occupation 35% + Note 35% + Fidélité 20% + Tendance 10%
        $scoreGlobal = (
            ($tauxOccupation * 0.35) +
            ($notePonderee * 0.35) +
            ($fidelite * 0.20) +
            ($tendanceNormalisee * 0.10)
        );

        return [
            'coach' => $coach,
            'nom' => $coach->getNom(),
            'prenom' => $coach->getPrenom(),
            'fullName' => $coach->getPrenom() . ' ' . $coach->getNom(),
            'tauxOccupation' => round($tauxOccupation, 2),
            'notePonderee' => round($notePonderee, 2),
            'fidelite' => round($fidelite, 2),
            'tendance' => round($tendance, 2),
            'scoreGlobal' => round(min($scoreGlobal, 100.0), 2),
            'domaine' => $coach->getDomaine(),
            'experienceAnnees' => $coach->getExperienceAnnees(),
            'tarifHoraire' => $coach->getTarifHoraire(),
        ];
    }

    /**
     * KPI 1 : Taux d'occupation (35%)
     * 
     * Calcule le pourcentage de créneaux réservés vs créneaux disponibles
     * Valeur entre 0 et 100
     * 
     * @param Coach $coach
     * @return float Pourcentage d'occupation (0-100)
     */
    private function calculateOccupationRate(Coach $coach): float
    {
        // Période de référence: 30 derniers jours
        $endDate = new \DateTime('today');
        $startDate = (clone $endDate)->modify('-30 days');

        // Réservations confirmées selon la date de séance dans la période
        $reservationsInPeriod = $this->reservationRepository
            ->countConfirmedReservationsByCoachInPeriodBySeance($coach, $startDate, $endDate);

        if ($reservationsInPeriod === 0) {
            return 0.0;
        }

        // Nombre de jours ouvrables (lundi-vendredi) dans la période
        $workingDays = 0;
        $current = clone $startDate;
        while ($current <= $endDate) {
            $dayOfWeek = (int) $current->format('N');
            if ($dayOfWeek <= 5) {
                $workingDays++;
            }
            $current->modify('+1 day');
        }

        if ($workingDays === 0) {
            return 0.0;
        }

        $occupation = ($reservationsInPeriod / $workingDays) * 100;

        return min(round($occupation, 2), 100.0);
    }

    /**
     * KPI 2 : Note pondérée (35%)
     * 
     * Ajuste la note moyenne selon le nombre d'avis
     * Une note basée sur peu d'avis compte moins qu'une note avec beaucoup d'avis
     * 
     * Valeur entre 0 et 5 (ou 0 et 100 si convertie)
     * 
     * @param Coach $coach
     * @return float Note pondérée (0-100, convertie de 0-5)
     */
    private function calculateWeightedRating(Coach $coach): float
    {
        $noteMoyenne = $coach->getNoteMoyenne() ?? 0.0;

        // Si pas de note, retourner 0
        if ($noteMoyenne === 0.0) {
            return 0.0;
        }

        // Compter le nombre réel d'avis
        $nbreAvis = $this->ratingRepository->getCountRatings($coach);

        // Coefficient de pondération basé sur le nombre d'avis
        if ($nbreAvis === 0) {
            return 0.0;
        } elseif ($nbreAvis < 3) {
            $coefficientPonderation = 0.5; // 50% du poids
        } elseif ($nbreAvis < 10) {
            $coefficientPonderation = 0.75; // 75% du poids
        } else {
            $coefficientPonderation = 1.0; // 100% du poids
        }

        // Note pondérée : redimensionner de 0-5 à 0-100
        $notePonderee = ($noteMoyenne / 5) * 100;
        
        return round($notePonderee * $coefficientPonderation, 2);
    }

    /**
     * KPI 3 : Fidélité client (20%)
     * 
     * Pourcentage de clients qui reviennent réserver plusieurs fois
     * avec le même coach
     * 
     * Valeur entre 0 et 100
     * 
     * @param Coach $coach
     * @return float Pourcentage de fidélité (0-100)
     */
    private function calculateClientLoyalty(Coach $coach): float
    {
        // Récupérer toutes les réservations confirmées du coach
        $allReservations = $this->reservationRepository->findConfirmedByCoach($coach);

        if (empty($allReservations)) {
            return 0;
        }

        // Compter les clients uniques
        $uniqueClients = [];
        $repeatClients = [];

        foreach ($allReservations as $reservation) {
            $userId = $reservation->getUser()?->getId();
            
            if ($userId) {
                if (isset($uniqueClients[$userId])) {
                    $uniqueClients[$userId]++;
                    // Si ce client a plus d'une réservation, il fait partie des clients fidèles
                    if ($uniqueClients[$userId] === 2) {
                        $repeatClients[$userId] = true;
                    }
                } else {
                    $uniqueClients[$userId] = 1;
                }
            }
        }

        $totalClients = count($uniqueClients);
        $loyalClients = count($repeatClients);

        // Fidélité = pourcentage de clients qui reviennent
        return ($totalClients > 0) ? ($loyalClients / $totalClients) * 100 : 0;
    }

    /**
     * KPI 4 : Tendance (10%)
     * 
     * Comparaison entre les réservations du mois courant et du mois précédent
     * 
     * Résultat : 
     * - > 100 : augmentation des réservations
     * - = 100 : stable
     * - < 100 : diminution
     * 
     * @param Coach $coach
     * @return float Index de tendance (0-100+)
     */
    private function calculateTrend(Coach $coach): float
    {
        $now = new \DateTime();
        
        // Réservations du mois courant
        $startOfCurrentMonth = (clone $now)->modify('first day of this month')->setTime(0, 0, 0);
        $endOfCurrentMonth = (clone $now)->modify('last day of this month')->setTime(23, 59, 59);
        
        $currentMonthCount = $this->reservationRepository->countConfirmedReservationsByCoachInPeriodBySeance(
            $coach,
            $startOfCurrentMonth,
            $endOfCurrentMonth
        );

        // Réservations du mois précédent
        $startOfPreviousMonth = (clone $now)->modify('first day of last month')->setTime(0, 0, 0);
        $endOfPreviousMonth = (clone $now)->modify('last day of last month')->setTime(23, 59, 59);
        
        $previousMonthCount = $this->reservationRepository->countConfirmedReservationsByCoachInPeriodBySeance(
            $coach,
            $startOfPreviousMonth,
            $endOfPreviousMonth
        );

        // Éviter division par zéro
        if ($previousMonthCount === 0) {
            // S'il y avait 0 réservation avant et maintenant il y en a
            return ($currentMonthCount > 0) ? 100.0 : 50.0; // 50 = stable baseline
        }

        // Index de tendance (mois courant / mois précédent) * 100
        $trend = ($currentMonthCount / $previousMonthCount) * 100;

        // Limiter entre 0 et 200 pour une meilleure lisibilité
        return min(round($trend, 2), 200.0);
    }

    /**
     * Récupère le coach au top et celui au bas du classement
     * 
     * @return array Contenant 'topCoach' et 'bottomCoach'
     */
    public function getTopAndBottomCoaches(array $alreadyCalculatedStats): array
    {
        return [
            'topCoach' => $alreadyCalculatedStats[0] ?? null,
            'bottomCoach' => $alreadyCalculatedStats[count($alreadyCalculatedStats) - 1] ?? null,
        ];
    }

    /**
     * Récupère les statistiques d'un coach spécifique
     * 
     * @param Coach $coach
     * @return array Statistiques du coach
     */
    public function getCoachStatistics(Coach $coach): array
    {
        return $this->calculateCoachStatistics($coach);
    }
}
