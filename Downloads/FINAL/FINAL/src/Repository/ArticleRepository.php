<?php

namespace App\Repository;

use App\Entity\Article;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Article>
 */
class ArticleRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Article::class);
    }

    /**
     * Returns articles sorted by an allowed field.
     * Allowed sortBy: titre, nombre_vues, categorie
     * Default: id_article DESC
     */
    public function findAllSorted(string $sortBy = 'id_article', string $sortDir = 'DESC'): array
    {
        $allowedFields = ['titre', 'nombre_vues', 'categorie', 'id_article'];
        $allowedDirs   = ['ASC', 'DESC'];

        if (!in_array($sortBy, $allowedFields, true)) {
            $sortBy = 'id_article';
        }
        if (!in_array(strtoupper($sortDir), $allowedDirs, true)) {
            $sortDir = 'DESC';
        }

        $fieldMap = [
            'id_article'  => 'a.id_article',
            'titre'       => 'a.titre',
            'nombre_vues' => 'a.nombre_vues',
            'categorie'   => 'a.categorie',
        ];

        $orderField = $fieldMap[$sortBy] ?? 'a.id_article';

        return $this->createQueryBuilder('a')
            ->orderBy($orderField, strtoupper($sortDir))
            ->getQuery()
            ->getResult();
    }

    public function countAll(): int
    {
        return (int) $this->createQueryBuilder('a')
            ->select('COUNT(a.id_article)')
            ->getQuery()
            ->getSingleScalarResult();
    }
}