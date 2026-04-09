<?php

namespace App\Controller\Admin;

use App\Service\CoachStatisticsService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

/**
 * Contrôleur pour le tableau de bord des statistiques des coachs (Admin)
 * 
 * Accès restreint aux administrateurs (ROLE_ADMIN)
 * Route : /admin/statistiques
 */
#[Route('/admin/statistiques', name: 'admin_statistics_')]
#[IsGranted('ROLE_ADMIN')]
class AdminCoachStatisticsController extends AbstractController
{
    public function __construct(
        private CoachStatisticsService $statisticsService,
    ) {}

    /**
     * Page principale des statistiques avec classement des coachs
     * 
     * Affiche :
     * - Un classement complet de tous les coachs
     * - Les 4 KPI pour chaque coach
     * - Le score global et le rang
     * - Des progress bars et badges visuels
     */
    #[Route('/', name: 'index', methods: ['GET'])]
    public function index(): Response
    {
        // Récupérer toutes les statistiques (triées par score global)
        $coachesStatistics = $this->statisticsService->getAllCoachesStatistics();

        // Récupérer le top et bottom coach pour affichage des highlights
        $topAndBottom = $this->statisticsService->getTopAndBottomCoaches($coachesStatistics);

        return $this->render('admin/statistics/index.html.twig', [
            'coachesStatistics' => $coachesStatistics,
            'topCoach' => $topAndBottom['topCoach'],
            'bottomCoach' => $topAndBottom['bottomCoach'],
            'totalCoaches' => count($coachesStatistics),
        ]);
    }

    /**
     * Vision détaillée des statistiques avec graphiques (optionnel)
     * 
     * Peut être enrichie avec des graphiques Chart.js ou Apex Charts
     */
    #[Route('/detailles', name: 'detailed', methods: ['GET'])]
    public function detailedStatistics(): Response
    {
        $coachesStatistics = $this->statisticsService->getAllCoachesStatistics();

        return $this->render('admin/statistics/detailed.html.twig', [
            'coachesStatistics' => $coachesStatistics,
        ]);
    }
}
