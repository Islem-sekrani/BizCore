<?php

namespace App\Service;

use App\Entity\Coach;
use App\Repository\CoachRepository;
use Doctrine\ORM\EntityManagerInterface;

class CoachService
{
    public function __construct(
        private EntityManagerInterface $entityManager,
        private CoachRepository $coachRepository,
    ) {}

    public function save(Coach $coach): Coach
    {
        if ($coach->getId() === null) {
            $existing = $this->coachRepository->findByFullName(
                $coach->getNom(),
                $coach->getPrenom()
            );
            if ($existing) {
                throw new \Exception('Ce coach existe déjà');
            }
        }

        $this->entityManager->persist($coach);
        $this->entityManager->flush();

        return $coach;
    }

    public function delete(Coach $coach): void
    {
        $this->entityManager->remove($coach);
        $this->entityManager->flush();
    }

    public function getAllCoaches(): array
    {
        return $this->coachRepository->findAll();
    }

    public function getAvailableCoaches(): array
    {
        return $this->coachRepository->findAvailable();
    }

    public function getCoachesByDomain(string $domaine): array
    {
        return $this->coachRepository->findByDomain($domaine);
    }

    public function searchCoaches(?string $domaine = null, ?float $maxTarif = null, bool $onlyAvailable = false): array
    {
        return $this->coachRepository->searchCoaches($domaine, $maxTarif, $onlyAvailable);
    }

    public function getStatsByDomain(): array
    {
        return $this->coachRepository->getStatsByDomain();
    }

    public function isCoachDuplicate(string $nom, string $prenom): bool
    {
        return $this->coachRepository->findByFullName($nom, $prenom) !== null;
    }

    public function isCoachDuplicateExcluding(string $nom, string $prenom, int $excludeId): bool
    {
        return $this->coachRepository->findByFullName($nom, $prenom, $excludeId) !== null;
    }

    public function getTotalCount(): int
    {
        return count($this->coachRepository->findAll());
    }

    public function getAverageRating(): ?float
    {
        $coaches = $this->coachRepository->findAll();
        if (empty($coaches)) {
            return null;
        }

        $total = 0;
        $count = 0;
        foreach ($coaches as $coach) {
            if ($coach->getNoteMoyenne() !== null) {
                $total += (float) $coach->getNoteMoyenne();
                $count++;
            }
        }

        return $count > 0 ? $total / $count : null;
    }
}
