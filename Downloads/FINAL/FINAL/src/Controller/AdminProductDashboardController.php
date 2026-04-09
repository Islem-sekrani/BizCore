<?php

namespace App\Controller;

use App\Entity\Product;
use App\Service\ProductService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin')]
class AdminProductDashboardController extends AbstractController
{
    public function __construct(
        private ProductService $productService,
        private string $uploadsDir = ''
    ) {}

    #[Route('', name: 'admin_home')]
    #[Route('/dashboard', name: 'admin_dashboard')]
    public function index(): Response
    {
        $products = $this->productService->getAllProducts();
        $stats    = $this->productService->getStats($products);

        return $this->render('admin/product_dashboard.html.twig', [
            'products'        => $products,
            'stats'           => $stats,
            'sidebarInitial'  => 'A',
            'adminNameLabel'  => 'Administrateur',
            'adminRoleLabel'  => 'Gestion Produits',
            'profileInitials' => 'A',
            'breadcrumbLabel' => 'GESTION PRODUITS',
            'totalProdLabel'  => $stats['total'],
            'totalStockLabel' => $stats['stock'],
            'totalCatLabel'   => $stats['categories'],
        ]);
    }

    #[Route('/stats', name: 'admin_stats')]
    public function stats(): JsonResponse
    {
        $products       = $this->productService->getAllProducts();
        $categoryCounts = [];
        $statusCounts   = [];

        foreach ($products as $p) {
            $cat  = $p->getCategorie() ?? 'Autre';
            $stat = $p->getStatut()    ?? 'Inconnu';
            $categoryCounts[$cat]  = ($categoryCounts[$cat] ?? 0) + 1;
            $statusCounts[$stat]   = ($statusCounts[$stat] ?? 0) + 1;
        }

        $s = $this->productService->getStats($products);
        return new JsonResponse([
            'totalProduits'  => $s['total'],
            'totalStock'     => $s['stock'],
            'totalCats'      => $s['categories'],
            'categoryCounts' => $categoryCounts,
            'statusCounts'   => $statusCounts,
        ]);
    }

    #[Route('/products/add', name: 'product_add', methods: ['GET', 'POST'])]
    public function add(Request $request): Response
    {
        if ($request->isMethod('POST')) {
            $product = new Product();
            $errors  = $this->fillFromRequest($product, $request);

            if (!empty($errors)) {
                $this->addFlash('error', implode(' | ', $errors));
            } elseif ($this->productService->addProduct($product)) {
                $this->addFlash('success', "Produit '{$product->getNomProduit()}' ajouté avec succès !");
                return $this->redirectToRoute('admin_dashboard');
            } else {
                $this->addFlash('error', "Erreur BD : impossible d'ajouter le produit.");
            }
        }

        return $this->render('admin/product_form.html.twig', [
            'product' => null,
            'mode'    => 'add',
            'cats'    => $this->productService->getCategories(),
            'statuts' => $this->productService->getStatuts(),
        ]);
    }

    #[Route('/products/{id}/edit', name: 'product_edit', methods: ['GET', 'POST'], requirements: ['id' => '\d+'])]
    public function edit(int $id, Request $request): Response
    {
        $product = $this->productService->getProductById($id);
        if (!$product) {
            $this->addFlash('error', 'Produit introuvable.');
            return $this->redirectToRoute('admin_dashboard');
        }

        if ($request->isMethod('POST')) {
            $errors = $this->fillFromRequest($product, $request);
            if (!empty($errors)) {
                $this->addFlash('error', implode(' | ', $errors));
            } elseif ($this->productService->updateProduct($product)) {
                $this->addFlash('success', "Produit '{$product->getNomProduit()}' modifié avec succès !");
                return $this->redirectToRoute('admin_dashboard');
            } else {
                $this->addFlash('error', "Erreur BD : impossible de modifier le produit.");
            }
        }

        return $this->render('admin/product_form.html.twig', [
            'product' => $product,
            'mode'    => 'edit',
            'cats'    => $this->productService->getCategories(),
            'statuts' => $this->productService->getStatuts(),
        ]);
    }

    #[Route('/products/{id}/delete', name: 'product_delete', methods: ['POST'], requirements: ['id' => '\d+'])]
    public function delete(int $id, Request $request): Response
    {
        if (!$this->isCsrfTokenValid('delete_' . $id, $request->request->get('_token'))) {
            $this->addFlash('error', 'Token de sécurité invalide.');
            return $this->redirectToRoute('admin_dashboard');
        }

        try {
            if ($this->productService->deleteProduct($id)) {
                $this->addFlash('success', 'Produit supprimé avec succès !');
            } else {
                $this->addFlash('error', 'Produit introuvable.');
            }
        } catch (\Exception $e) {
            // Clé étrangère ou autre erreur BD
            $msg = $e->getPrevious()?->getMessage() ?? $e->getMessage();
            if (str_contains($msg, 'foreign key') || str_contains($msg, 'constraint')) {
                $this->addFlash('error', 'Impossible de supprimer : ce produit est référencé dans d\'autres données (commandes, panier…). Videz d\'abord ces références.');
            } else {
                $this->addFlash('error', 'Erreur BD : ' . substr($msg, 0, 200));
            }
        }
        return $this->redirectToRoute('admin_dashboard');
    }

    #[Route('/export-pdf', name: 'admin_export_pdf')]
    public function exportPdf(): Response
    {
        $products = $this->productService->getAllProducts();
        return $this->render('admin/product_export_pdf.html.twig', ['products' => $products]);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private function fillFromRequest(Product $product, Request $request): array
    {
        $errors = [];

        $nomProduit  = trim($request->request->get('nomProduit', ''));
        $description = trim($request->request->get('description', ''));
        $prix        = trim($request->request->get('prix', ''));
        $stock       = $request->request->get('stockDisponible', '0');
        $categorie   = $request->request->get('categorie', '');
        $statut      = $request->request->get('statut', '');

        if (!preg_match('/^[A-ZÀÂÇÉÈÊËÎÏÔÙÛÜŸÆŒ]/', $nomProduit) || strlen($nomProduit) < 2)
            $errors[] = 'Le nom doit commencer par une majuscule (ex : Produit exemple).';
        if (strlen($description) < 25)
            $errors[] = 'La description doit contenir au moins 25 caractères (actuellement ' . strlen($description) . ').';
        if (!preg_match('/^\d+\.\d{2}$/', $prix) || (float)$prix <= 0)
            $errors[] = 'Le prix doit être au format XX.XX (ex: 29.99).';
        if (!is_numeric($stock) || (int)$stock < 0)
            $errors[] = 'Le stock doit être un entier >= 0.';
        if (empty($categorie))
            $errors[] = 'La catégorie est obligatoire.';
        if (empty($statut))
            $errors[] = 'Le statut est obligatoire.';

        $imageFile       = $request->files->get('imageFile');
        $hasImage        = $imageFile && $imageFile->isValid();
        $hasExistingImg  = !empty($product->getImageUrl());

        if (!$hasImage && !$hasExistingImg)
            $errors[] = "L'image du produit est obligatoire.";

        if (empty($errors)) {
            $product->setNomProduit($nomProduit);
            $product->setDescription($description);
            $product->setPrix($prix);
            $product->setStockDisponible((int)$stock);
            $product->setCategorie($categorie);
            $product->setStatut($statut);

            if ($hasImage) {
                $uploadsDir = $this->uploadsDir ?: (__DIR__ . '/../../public/uploads');
                $filename   = uniqid('img_') . '.' . $imageFile->getClientOriginalExtension();
                $imageFile->move($uploadsDir, $filename);
                $product->setImageUrl('/uploads/' . $filename);
            }
        }

        return $errors;
    }
}
