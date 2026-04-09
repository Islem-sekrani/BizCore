<?php

namespace App\Repository;

use App\Entity\User;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class UserRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, User::class);
    }

    public function countAll(): int
    {
        return $this->count([]);
    }

    public function countActive(): int
    {
        return $this->count(['statut' => 'actif']);
    }

    public function countInactive(): int
    {
        return $this->createQueryBuilder('u')
            ->select('COUNT(u.id)')
            ->where('u.statut != :statut')
            ->setParameter('statut', 'actif')
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function countRecentRegistrations(int $days): int
    {
        $date = new \DateTime("-{$days} days");
        return $this->createQueryBuilder('u')
            ->select('COUNT(u.id)')
            ->where('u.dateInscription >= :date')
            ->setParameter('date', $date)
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function countDormant(int $days): int
    {
        $date = new \DateTime("-{$days} days");
        return $this->createQueryBuilder('u')
            ->select('COUNT(u.id)')
            ->where('u.derniereConnexion IS NULL OR u.derniereConnexion < :date')
            ->setParameter('date', $date)
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function countWithoutRole(): int
    {
        return $this->createQueryBuilder('u')
            ->select('COUNT(u.id)')
            ->where('u.roleEntity IS NULL')
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function countIncompleteProfiles(): int
    {
        return $this->createQueryBuilder('u')
            ->select('COUNT(u.id)')
            ->where('u.telephone IS NULL OR u.adresse IS NULL')
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function findByIdWithRole(int $id): ?User
    {
        return $this->find($id);
    }

    public function countSearch(string $search): int
    {
        return $this->createQueryBuilder('u')
            ->select('COUNT(u.id)')
            ->where('u.nom LIKE :q OR u.prenom LIKE :q OR u.email LIKE :q')
            ->setParameter('q', "%{$search}%")
            ->getQuery()
            ->getSingleScalarResult();
    }

    public function searchPaginated(string $search, int $limit, int $offset, string $sort = 'id', string $dir = 'ASC'): array
    {
        $validSorts = ['id', 'nom', 'prenom', 'email', 'statut', 'dateInscription'];
        if (!in_array($sort, $validSorts)) {
            $sort = 'id';
        }
        $dir = strtoupper($dir) === 'DESC' ? 'DESC' : 'ASC';

        return $this->createQueryBuilder('u')
            ->where('u.nom LIKE :q OR u.prenom LIKE :q OR u.email LIKE :q')
            ->setParameter('q', "%{$search}%")
            ->orderBy("u.{$sort}", $dir)
            ->setFirstResult($offset)
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    public function emailExists(string $email): bool
    {
        return $this->count(['email' => $email]) > 0;
    }

    public function emailExistsExcept(string $email, int $userId): bool
    {
        return $this->createQueryBuilder('u')
            ->select('COUNT(u.id)')
            ->where('u.email = :email AND u.id != :id')
            ->setParameter('email', $email)
            ->setParameter('id', $userId)
            ->getQuery()
            ->getSingleScalarResult() > 0;
    }
}
