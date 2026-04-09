<?php

namespace App\Repository;

use App\Entity\Coach;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Coach>
 */
class CoachRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Coach::class);
    }

    /**
     * Alias used by AdminCoachController — domaine is a plain string column, no join needed.
     * Returns all coaches sorted by nom then prenom.
     */
    public function findAllWithDomaine(): array
    {
        return $this->createQueryBuilder('c')
            ->orderBy('c.nom', 'ASC')
            ->addOrderBy('c.prenom', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Find a coach by full name (case-insensitive)
     */
    public function findByFullName(string $nom, string $prenom, ?int $excludeId = null): ?Coach
    {
        $qb = $this->createQueryBuilder('c')
            ->where('LOWER(c.nom) = :nom')
            ->andWhere('LOWER(c.prenom) = :prenom')
            ->setParameter('nom', strtolower(trim($nom)))
            ->setParameter('prenom', strtolower(trim($prenom)));

        if ($excludeId) {
            $qb->andWhere('c.idCoach != :id')
                ->setParameter('id', $excludeId);
        }

        return $qb->getQuery()->getOneOrNullResult();
    }

    /**
     * Find all available coaches
     */
    public function findAvailable(): array
    {
        return $this->createQueryBuilder('c')
            ->where('c.disponibilite = :dispo')
            ->setParameter('dispo', 'Disponible')
            ->orderBy('c.noteMoyenne', 'DESC')
            ->addOrderBy('c.tarifHoraire', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Find coaches by domain
     */
    public function findByDomain(string $domaine): array
    {
        return $this->createQueryBuilder('c')
            ->where('c.domaine = :domaine')
            ->setParameter('domaine', $domaine)
            ->orderBy('c.noteMoyenne', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Search coaches with filters
     */
    public function searchCoaches(?string $domaine = null, ?float $maxTarif = null, bool $onlyAvailable = false): array
    {
        $qb = $this->createQueryBuilder('c');

        if ($domaine) {
            $qb->andWhere('c.domaine = :domaine')
                ->setParameter('domaine', $domaine);
        }

        if ($maxTarif !== null) {
            $qb->andWhere('c.tarifHoraire <= :maxTarif')
                ->setParameter('maxTarif', $maxTarif);
        }

        if ($onlyAvailable) {
            $qb->andWhere('c.disponibilite = :dispo')
                ->setParameter('dispo', 'Disponible');
        }

        return $qb
            ->orderBy('c.noteMoyenne', 'DESC')
            ->addOrderBy('c.tarifHoraire', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Get statistics by domain
     */
    public function getStatsByDomain(): array
    {
        return $this->createQueryBuilder('c')
            ->select('c.domaine, COUNT(c.idCoach) as total, AVG(c.noteMoyenne) as avgNote')
            ->groupBy('c.domaine')
            ->orderBy('total', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Search coaches with admin filters (database-level for performance)
     */
    public function searchWithFilters(string $q = '', string $domaine = '', string $dispo = ''): array
    {
        $qb = $this->createQueryBuilder('c')
            ->orderBy('c.nom', 'ASC');

        if ($q) {
            $qb->andWhere('c.nom LIKE :q OR c.prenom LIKE :q')
                ->setParameter('q', '%' . $q . '%');
        }
        if ($domaine) {
            $qb->andWhere('c.domaine = :domaine')
                ->setParameter('domaine', $domaine);
        }
        if ($dispo) {
            $qb->andWhere('c.disponibilite = :dispo')
                ->setParameter('dispo', $dispo);
        }

        return $qb->getQuery()->getResult();
    }
}
