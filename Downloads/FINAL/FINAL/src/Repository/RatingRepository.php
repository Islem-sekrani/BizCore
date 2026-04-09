<?php

namespace App\Repository;

use App\Entity\Coach;
use App\Entity\Rating;
use App\Entity\User;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Rating>
 */
class RatingRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Rating::class);
    }

    /**
     * Find rating by user and coach
     */
    public function findByUserAndCoach(User $user, Coach $coach): ?Rating
    {
        return $this->createQueryBuilder('r')
            ->where('r.user = :user')
            ->andWhere('r.coach = :coach')
            ->setParameter('user', $user)
            ->setParameter('coach', $coach)
            ->getQuery()
            ->getOneOrNullResult();
    }

    /**
     * Calculate average rating for a coach
     */
    public function getAverageRating(Coach $coach): ?float
    {
        $result = $this->createQueryBuilder('r')
            ->select('AVG(r.note) as average')
            ->where('r.coach = :coach')
            ->setParameter('coach', $coach)
            ->getQuery()
            ->getSingleScalarResult();

        return $result ? round((float)$result, 1) : null;
    }

    /**
     * Get count of ratings for a coach
     */
    public function getCountRatings(Coach $coach): int
    {
        return (int) $this->createQueryBuilder('r')
            ->select('COUNT(r.id)')
            ->where('r.coach = :coach')
            ->setParameter('coach', $coach)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Get all ratings for a coach
     */
    public function findByCoach(Coach $coach, int $limit = 50)
    {
        return $this->createQueryBuilder('r')
            ->where('r.coach = :coach')
            ->setParameter('coach', $coach)
            ->orderBy('r.dateCreation', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }
}
