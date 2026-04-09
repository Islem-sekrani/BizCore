<?php

namespace App\Service;

use App\Entity\Product;
use App\Repository\ProductRepository;
use Doctrine\DBAL\Connection;

/**
 * ProductService — gestion CRUD des produits (miroir de prod)
 */
class ProductService
{
    public function __construct(
        private ProductRepository $productRepository,
        private Connection $connection
    ) {}

    public function getAllProducts(): array
    {
        return $this->productRepository->findAll();
    }

    public function getAvailableProducts(): array
    {
        return $this->productRepository->findAvailableProducts();
    }

    public function addProduct(Product $product): bool
    {
        try {
            $this->productRepository->save($product, true);
            return true;
        } catch (\Exception) {
            return false;
        }
    }

    public function updateProduct(Product $product): bool
    {
        try {
            $this->productRepository->save($product, true);
            return true;
        } catch (\Exception) {
            return false;
        }
    }

    public function deleteProduct(int $id): bool
    {
        $product = $this->productRepository->find($id);
        if (!$product) return false;

        // Supprimer d'abord les références dans ligne_commande (contrainte FK)
        $this->connection->executeStatement(
            'DELETE FROM ligne_commande WHERE id_produit = ?',
            [$id]
        );

        $this->productRepository->remove($product, true);
        return true;
    }

    public function getProductById(int $id): ?Product
    {
        try {
            return $this->productRepository->find($id);
        } catch (\Exception) {
            return null;
        }
    }

    public function getStats(array $products = []): array
    {
        if (empty($products)) $products = $this->getAllProducts();
        $total      = count($products);
        $stock      = array_sum(array_map(fn($p) => $p->getStockDisponible(), $products));
        $categories = count(array_unique(array_filter(
            array_map(fn($p) => $p->getCategorie(), $products)
        )));
        return ['total' => $total, 'stock' => $stock, 'categories' => $categories];
    }

    public function getCategories(): array
    {
        return ['Formation', 'Livre', 'Abonnement', 'Logiciel', 'Service'];
    }

    public function getStatuts(): array
    {
        return ['Disponible', 'Rupture', 'En commande', 'Bientôt disponible'];
    }
}
