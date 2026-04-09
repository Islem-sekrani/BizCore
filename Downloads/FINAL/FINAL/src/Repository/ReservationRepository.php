<?php

namespace App\Repository;

use App\Entity\Coach;
use App\Entity\Reservation;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class ReservationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Reservation::class);
    }

    // Réservations d'un user spécifique
    public function findByUser(int $userId): array
    {
        return $this->createQueryBuilder('r')
            ->leftJoin('r.coach', 'c')
            ->addSelect('c')
            ->where('r.user = :userId')
            ->setParameter('userId', $userId)
            ->orderBy('r.dateReservation', 'DESC')
            ->getQuery()
            ->getResult();
    }

    // Réservations pour un coach (pour les stats)
    public function countByCoach(int $coachId): int
    {
        return (int) $this->createQueryBuilder('r')
            ->select('COUNT(r.id)')
            ->where('r.coach = :coachId')
            ->andWhere('r.statut = :statut')
            ->setParameter('coachId', $coachId)
            ->setParameter('statut', 'CONFIRMEE')
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Compte les réservations confirmées d'un coach
     */
    public function countConfirmedReservationsByCoach(Coach $coach): int
    {
        return (int) $this->createQueryBuilder('r')
            ->select('COUNT(r.id)')
            ->where('r.coach = :coach')
            ->andWhere('r.statut = :statut')
            ->setParameter('coach', $coach)
            ->setParameter('statut', 'CONFIRMEE')
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Récupère toutes les réservations confirmées d'un coach
     */
    public function findConfirmedByCoach(Coach $coach): array
    {
        return $this->createQueryBuilder('r')
            ->where('r.coach = :coach')
            ->andWhere('r.statut = :statut')
            ->setParameter('coach', $coach)
            ->setParameter('statut', 'CONFIRMEE')
            ->orderBy('r.dateReservation', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Compte les réservations confirmées d'un coach dans une période donnée
     */
    public function countConfirmedReservationsByCoachInPeriod(
        Coach $coach,
        \DateTime $startDate,
        \DateTime $endDate
    ): int {
        return (int) $this->createQueryBuilder('r')
            ->select('COUNT(r.id)')
            ->where('r.coach = :coach')
            ->andWhere('r.statut = :statut')
            ->andWhere('r.dateReservation >= :startDate')
            ->andWhere('r.dateReservation <= :endDate')
            ->setParameter('coach', $coach)
            ->setParameter('statut', 'CONFIRMEE')
            ->setParameter('startDate', $startDate)
            ->setParameter('endDate', $endDate)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Compte les réservations confirmées d'un coach selon la date de séance dans une période donnée
     */
    public function countConfirmedReservationsByCoachInPeriodBySeance(
        Coach $coach,
        \DateTime $startDate,
        \DateTime $endDate
    ): int {
        return (int) $this->createQueryBuilder('r')
            ->select('COUNT(r.id)')
            ->where('r.coach = :coach')
            ->andWhere('r.statut = :statut')
            ->andWhere('r.dateSeance >= :startDate')
            ->andWhere('r.dateSeance <= :endDate')
            ->setParameter('coach', $coach)
            ->setParameter('statut', 'CONFIRMEE')
            ->setParameter('startDate', $startDate)
            ->setParameter('endDate', $endDate)
            ->getQuery()
            ->getSingleScalarResult();
    }
}
