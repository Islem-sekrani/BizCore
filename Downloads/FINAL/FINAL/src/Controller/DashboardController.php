<?php

namespace App\Controller;

use App\Repository\RoleRepository;
use App\Repository\UserRepository;
use App\Repository\ArticleRepository;
use App\Repository\CommandeRepository;
use App\Repository\EvenementRepository;
use App\Repository\ProductRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Dompdf\Dompdf;
use Dompdf\Options;


class DashboardController extends AbstractController
{
    #[Route('/dashboard', name: 'dashboard')]
    #[IsGranted('ROLE_USER')]
    public function index(
        Request $request,
        UserRepository $userRepository,
        ProductRepository $productRepository,
        CommandeRepository $commandeRepository,
        EvenementRepository $evenementRepository,
        ArticleRepository $articleRepository,
        RoleRepository $roleRepository
    ): Response {
        $today = new \DateTimeImmutable();

        $month = max(1, min(12, (int) $request->query->get('month', $today->format('n'))));
        $year = (int) $request->query->get('year', $today->format('Y'));

        $currentMonthDate = new \DateTimeImmutable(sprintf('%04d-%02d-01', $year, $month));

        $stats = [
            'users' => $userRepository->countAll(),
            'active_users' => $userRepository->countActive(),
            'inactive_users' => $userRepository->countInactive(),
            'recent_users' => $userRepository->countRecentRegistrations(7),
            'dormant_users' => $userRepository->countDormant(30),
            'without_role' => $userRepository->countWithoutRole(),
            'incomplete_profiles' => $userRepository->countIncompleteProfiles(),
            'products' => $productRepository->countAll(),
            'orders' => $commandeRepository->countAll(),
            'events' => $evenementRepository->countAll(),
            'articles' => $articleRepository->countAll(),
        ];

        $roles = array_map(
            static fn($role) => [
                'id_role' => $role->getId(),
                'nom_role' => $role->getNomRole(),
            ],
            $roleRepository->findBy([], ['id' => 'ASC'])
        );

        return $this->render('dashboard/index.html.twig', [
            'stats' => $stats,
            'roles' => $roles,
            'month' => $month,
            'year' => $year,
            'currentMonthLabel' => $currentMonthDate->format('F Y'),
            'daysInMonth' => (int) $currentMonthDate->format('t'),
            'firstDayOfMonth' => (int) $currentMonthDate->format('N'),
            'prevMonth' => $month === 1 ? 12 : $month - 1,
            'prevYear' => $month === 1 ? $year - 1 : $year,
            'nextMonth' => $month === 12 ? 1 : $month + 1,
            'nextYear' => $month === 12 ? $year + 1 : $year,
            'authUser' => $this->getUser(),
            'todayMonth' => (int) $today->format('n'),
            'todayYear' => (int) $today->format('Y'),
        ]);
    }

    #[Route('/dashboard/load', name: 'dashboard_load')]
    #[IsGranted('ROLE_USER')]
    public function load(
        Request $request,
        UserRepository $userRepository,
        RoleRepository $roleRepository
    ): Response {
        $view = (string) $request->query->get('view', 'users');

        if ($view === 'users') {
            $search = trim((string) $request->query->get('search', ''));
            $page = max(1, (int) $request->query->get('page', 1));
            $limit = 10;
            $offset = ($page - 1) * $limit;
            $sort = (string) $request->query->get('sort', 'id');
            $dir = (string) $request->query->get('dir', 'ASC');

            $totalUsers = $userRepository->countSearch($search);
            $totalPages = max(1, (int) ceil($totalUsers / $limit));

            if ($page > $totalPages) {
                $page = $totalPages;
                $offset = ($page - 1) * $limit;
            }

            $users = array_map(
                static fn($user) => [
                    'id_user' => $user->getId(),
                    'nom' => $user->getNom(),
                    'prenom' => $user->getPrenom(),
                    'email' => $user->getEmail(),
                    'telephone' => $user->getTelephone(),
                    'adresse' => $user->getAdresse(),
                    'statut' => $user->getStatut(),
                    'date_inscription' => $user->getDateInscription()?->format('Y-m-d H:i:s'),
                    'id_role' => $user->getRoleEntity()?->getId(),
                    'nom_role' => $user->getRoleEntity()?->getNomRole(),
                ],
                $userRepository->searchPaginated($search, $limit, $offset, $sort, $dir)
            );

            $roles = array_map(
                static fn($role) => [
                    'id_role' => $role->getId(),
                    'nom_role' => $role->getNomRole(),
                ],
                $roleRepository->findBy([], ['id' => 'ASC'])
            );

            return $this->render('dashboard/_users.html.twig', [
                'users' => $users,
                'roles' => $roles,
                'search' => $search,
                'page' => $page,
                'totalPages' => $totalPages,
                'sort' => $sort,
                'dir' => $dir,
            ]);
        }

        if ($view === 'events') {
            return $this->render('evenement/index.html.twig');
        }

        if ($view === 'article') {
            return $this->render('article/index.html.twig');
        }

        return new Response('<h2 style="padding:20px;">View not found</h2>', 200);
    }

    #[Route('/dashboard/report', name: 'dashboard_report', methods: ['GET'])]
    #[IsGranted('ROLE_USER')]
    public function report(
        UserRepository $userRepository,
        ProductRepository $productRepository,
        CommandeRepository $commandeRepository,
        EvenementRepository $evenementRepository,
        ArticleRepository $articleRepository,
        RoleRepository $roleRepository
    ): Response {
        $stats = [
            'users' => $userRepository->countAll(),
            'active_users' => $userRepository->countActive(),
            'inactive_users' => $userRepository->countInactive(),
            'recent_users' => $userRepository->countRecentRegistrations(7),
            'dormant_users' => $userRepository->countDormant(30),
            'without_role' => $userRepository->countWithoutRole(),
            'incomplete_profiles' => $userRepository->countIncompleteProfiles(),
            'products' => $productRepository->countAll(),
            'orders' => $commandeRepository->countAll(),
            'events' => $evenementRepository->countAll(),
            'articles' => $articleRepository->countAll(),
        ];

        $max = max([$stats['users'], $stats['products'], $stats['orders'], $stats['events'], $stats['articles'], 1]);

        $html = $this->renderView('dashboard/report.html.twig', [
            'stats' => $stats,
            'max' => $max,
            'generatedAt' => new \DateTimeImmutable(),
        ]);

        $options = new Options();
        $options->set('isRemoteEnabled', true);

        $dompdf = new Dompdf($options);
        $dompdf->loadHtml($html);
        $dompdf->setPaper('A4', 'portrait');
        $dompdf->render();

        return new Response(
            $dompdf->output(),
            200,
            [
                'Content-Type' => 'application/pdf',
                'Content-Disposition' => 'inline; filename="bizcore-report.pdf"',
            ]
        );
    }
}