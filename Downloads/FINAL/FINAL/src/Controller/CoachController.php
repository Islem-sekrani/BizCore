<?php

namespace App\Controller;

use App\Entity\Coach;
use App\Form\CoachType;
use App\Repository\CoachRepository;
use App\Repository\RatingRepository;
use App\Service\CoachService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\Form\FormError;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/coach')]
class CoachController extends AbstractController
{
    public function __construct(
        private CoachService $coachService,
        private EntityManagerInterface $entityManager,
        private CoachRepository $coachRepository,
        private RatingRepository $ratingRepository,
    ) {}

    // ── LIST ─────────────────────────────────────────────────────────────────
    #[Route('/', name: 'app_coach_index', methods: ['GET'])]
    public function index(): Response
    {
        $coaches = $this->coachRepository->findAllWithDomaine();
        
        // Fetch rating data for each coach
        $coachRatings = [];
        foreach ($coaches as $coach) {
            $coachRatings[$coach->getId()] = [
                'average' => $this->ratingRepository->getAverageRating($coach),
                'count' => $this->ratingRepository->getCountRatings($coach),
            ];
        }
        
        return $this->render('coach/index.html.twig', [
            'coaches' => $coaches,
            'coachRatings' => $coachRatings,
        ]);
    }

    // ── SEARCH (must be before /{id}) ────────────────────────────────────────
    #[Route('/search', name: 'app_coach_search', methods: ['GET'])]
    public function search(Request $request): Response
    {
        $domaine       = $request->query->get('domaine');
        $maxTarif      = $request->query->get('maxTarif');
        $onlyAvailable = $request->query->getBoolean('onlyAvailable', false);

        $coaches = $this->coachService->searchCoaches(
            $domaine,
            $maxTarif ? (float) $maxTarif : null,
            $onlyAvailable
        );

        return $this->render('coach/search.html.twig', [
            'coaches'       => $coaches,
            'domaine'       => $domaine,
            'maxTarif'      => $maxTarif,
            'onlyAvailable' => $onlyAvailable,
        ]);
    }

    // ── NEW ──────────────────────────────────────────────────────────────────
    #[Route('/new', name: 'app_coach_new', methods: ['GET', 'POST'])]
    public function new(Request $request): Response
    {
        $coach = new Coach();
        $form  = $this->createForm(CoachType::class, $coach);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            try {
                $this->coachService->save($coach);
                $this->addFlash('success', 'Coach « '.$coach->getFullName().' » créé avec succès !');
                return $this->redirectToRoute('app_coach_index');
            } catch (\Exception $e) {
                $this->addFlash('error', $e->getMessage());
            }
        }

        return $this->render('coach/new.html.twig', ['form' => $form, 'coach' => $coach]);
    }

    // ── SHOW (after /new and /search) ────────────────────────────────────────
    #[Route('/{id}', name: 'app_coach_show', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function show(Coach $coach): Response
    {
        return $this->render('coach/show.html.twig', ['coach' => $coach]);
    }

    // ── EDIT ─────────────────────────────────────────────────────────────────
    #[Route('/{id}/edit', name: 'app_coach_edit', methods: ['GET', 'POST'], requirements: ['id' => '\d+'])]
    public function edit(Request $request, Coach $coach): Response
    {
        $form = $this->createForm(CoachType::class, $coach);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            if ($this->coachService->isCoachDuplicateExcluding(
                $coach->getNom(),
                $coach->getPrenom(),
                $coach->getId()
            )) {
                $form->addError(new FormError('Un autre coach avec ce nom et prénom existe déjà.'));
            } else {
                $this->coachService->save($coach);
                $this->addFlash('success', 'Coach « '.$coach->getFullName().' » modifié avec succès !');
                return $this->redirectToRoute('app_coach_show', ['id' => $coach->getId()]);
            }
        }

        return $this->render('coach/edit.html.twig', ['form' => $form, 'coach' => $coach]);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    #[Route('/{id}/delete', name: 'app_coach_delete', methods: ['GET', 'POST'], requirements: ['id' => '\d+'])]
    public function delete(Request $request, Coach $coach): Response
    {
        if ($request->isMethod('POST')) {
            if (!$this->isCsrfTokenValid('delete'.$coach->getId(), $request->request->get('_token'))) {
                $this->addFlash('error', 'Token CSRF invalide. Suppression annulée.');
                return $this->redirectToRoute('app_coach_index');
            }
            $name = $coach->getFullName();
            $this->coachService->delete($coach);
            $this->addFlash('success', 'Coach « '.$name.' » supprimé avec succès.');
            return $this->redirectToRoute('app_coach_index');
        }

        return $this->render('coach/delete.html.twig', ['coach' => $coach]);
    }
}
