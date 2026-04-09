<?php

namespace App\Repository;

use App\Entity\Role;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class RoleRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Role::class);
    }

    public function getAllForSelect(): array
    {
        return $this->createQueryBuilder('r')
            ->select('r.id', 'r.nomRole')
            ->orderBy('r.id', 'ASC')
            ->getQuery()
            ->getArrayResult();
    }

    public function exists(int $id): bool
    {
        return (bool) $this->createQueryBuilder('r')
            ->select('1')
            ->andWhere('r.id = :id')
            ->setParameter('id', $id)
            ->getQuery()
            ->getOneOrNullResult();
    }
}