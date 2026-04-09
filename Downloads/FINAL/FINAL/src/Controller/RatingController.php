<?php

namespace App\Controller;

use App\Entity\Coach;
use App\Entity\Rating;
use App\Repository\RatingRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Psr\Log\LoggerInterface;

#[Route('/api/rating')]
class RatingController extends AbstractController
{
    public function __construct(
        private EntityManagerInterface $em,
        private RatingRepository $ratingRepo,
        private LoggerInterface $logger,
    ) {}

    /**
     * Get rating stats for a coach
     */
    #[Route('/coach/{id}/stats', name: 'api_coach_rating_stats', methods: ['GET'])]
    public function getCoachStats(Coach $coach): JsonResponse
    {
        try {
            $average = $this->ratingRepo->getAverageRating($coach);
            $count = $this->ratingRepo->getCountRatings($coach);
            $userNote = null;

            if ($this->getUser()) {
                $userRating = $this->ratingRepo->findByUserAndCoach($this->getUser(), $coach);
                $userNote = $userRating?->getNote();
            }

            return $this->json([
                'success' => true,
                'coach_id' => $coach->getId(),
                'average' => $average,
                'count' => $count,
                'userNote' => $userNote,
            ]);
        } catch (\Exception $e) {
            $this->logger->error('Error getting rating stats:', ['error' => $e->getMessage()]);
            return $this->json([
                'success' => false,
                'message' => 'Erreur lors du chargement des évaluations'
            ], 500);
        }
    }

    /**
     * Create or update a rating
     */
    #[Route('/coach/{id}/rate', name: 'api_coach_rate', methods: ['POST'])]
    #[IsGranted('IS_AUTHENTICATED_FULLY')]
    public function rateCoach(Coach $coach, Request $request): JsonResponse
    {
        try {
            // Check authentication
            if (!$this->getUser()) {
                return $this->json([
                    'success' => false,
                    'message' => 'Vous devez être connecté pour laisser une évaluation'
                ], 401);
            }

            $data = json_decode($request->getContent(), true);

            if ($data === null) {
                return $this->json([
                    'success' => false,
                    'message' => 'JSON invalide'
                ], 400);
            }

            $note = $data['note'] ?? null;
            $commentaire = $data['commentaire'] ?? null;

            // Validate note
            if ($note === null || !is_int($note) || $note < 1 || $note > 5) {
                return $this->json([
                    'success' => false,
                    'message' => 'La note doit être un nombre entier entre 1 et 5'
                ], 400);
            }

            // Find or create rating
            $rating = $this->ratingRepo->findByUserAndCoach($this->getUser(), $coach);

            if (!$rating) {
                $rating = new Rating();
                $rating->setUser($this->getUser());
                $rating->setCoach($coach);
            }

            // Update rating
            $rating->setNote($note);
            if (!empty($commentaire)) {
                $rating->setCommentaire($commentaire);
            }
            $rating->setDateModification(new \DateTime());

            // Persist and flush
            $this->em->persist($rating);
            $this->em->flush();

            // Recompute updated stats from all coach ratings
            $newAverage = $this->ratingRepo->getAverageRating($coach);
            $newCount = $this->ratingRepo->getCountRatings($coach);

            // Sync denormalized average stored on coach table
            $coach->setNoteMoyenne($newAverage);
            $this->em->flush();

            return $this->json([
                'success' => true,
                'message' => 'Merci pour votre évaluation !',
                'rating' => [
                    'id' => $rating->getId(),
                    'note' => $rating->getNote(),
                    'commentaire' => $rating->getCommentaire(),
                ],
                'stats' => [
                    'average' => $newAverage,
                    'count' => $newCount,
                ]
            ], 201);

        } catch (\Exception $e) {
            $this->logger->error('Error creating/updating rating:', ['error' => $e->getMessage()]);
            return $this->json([
                'success' => false,
                'message' => 'Erreur lors de l\'enregistrement de l\'évaluation'
            ], 500);
        }
    }

    /**
     * Get all ratings for a coach
     */
    #[Route('/coach/{id}/all', name: 'api_coach_ratings_list', methods: ['GET'])]
    public function getCoachRatings(Coach $coach): JsonResponse
    {
        try {
            $ratingsList = $this->ratingRepo->findByCoach($coach, 100);
            
            $data = array_map(fn(Rating $r) => [
                'id' => $r->getId(),
                'note' => $r->getNote(),
                'commentaire' => $r->getCommentaire(),
                'userName' => $r->getUser()?->getFullName(),
                'dateCreation' => $r->getDateCreation()?->format('Y-m-d H:i:s'),
            ], $ratingsList);

            return $this->json([
                'success' => true,
                'coach_id' => $coach->getId(),
                'ratings' => $data,
            ]);
        } catch (\Exception $e) {
            $this->logger->error('Error getting coach ratings:', ['error' => $e->getMessage()]);
            return $this->json([
                'success' => false,
                'message' => 'Erreur lors du chargement des évaluations'
            ], 500);
        }
    }
}
