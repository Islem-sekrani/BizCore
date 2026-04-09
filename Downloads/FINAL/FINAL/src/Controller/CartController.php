<?php

namespace App\Controller;

use App\Service\ProductService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/cart', name: 'cart_')]
class CartController extends AbstractController
{
    public function __construct(private ProductService $productService) {}

    /**
     * Affiche le panier
     */
    #[Route('', name: 'index')]
    public function index(Request $request): Response
    {
        $session  = $request->getSession();
        $cart     = $session->get('cart', []);
        $items    = $this->buildCartItems($cart);
        $subtotal = array_sum(array_map(fn($i) => $i['prix'] * $i['quantity'], $items));

        return $this->render('cart/index.html.twig', [
            'items'    => $items,
            'subtotal' => $subtotal,
        ]);
    }

    /**
     * Ajouter un produit au panier
     */
    #[Route('/add/{id}', name: 'add', methods: ['POST'], requirements: ['id' => '\d+'])]
    public function add(int $id, Request $request): Response
    {
        if (!$this->isCsrfTokenValid('cart_add_' . $id, $request->request->get('_token'))) {
            $this->addFlash('error', 'Action non autorisée.');
            return $this->redirectToRoute('marketplace_index');
        }

        $product = $this->productService->getProductById($id);
        if (!$product) {
            $this->addFlash('error', 'Produit introuvable.');
            return $this->redirectToRoute('marketplace_index');
        }

        $session = $request->getSession();
        $cart    = $session->get('cart', []);

        if (isset($cart[$id])) {
            $cart[$id]['quantity']++;
        } else {
            $cart[$id] = [
                'productId'   => $id,
                'nomProduit'  => $product->getNomProduit(),
                'prix'        => (float)$product->getPrix(),
                'categorie'   => $product->getCategorie(),
                'imageUrl'    => $product->getImageUrl(),
                'quantity'    => 1,
            ];
        }

        $session->set('cart', $cart);
        $this->addFlash('success', "« {$product->getNomProduit()} » ajouté au panier !");
        return $this->redirectToRoute('marketplace_index');
    }

    /**
     * Mettre à jour la quantité d'un article
     */
    #[Route('/update/{id}', name: 'update', methods: ['POST'], requirements: ['id' => '\d+'])]
    public function update(int $id, Request $request): Response
    {
        if (!$this->isCsrfTokenValid('cart_update_' . $id, $request->request->get('_token'))) {
            $this->addFlash('error', 'Action non autorisée.');
            return $this->redirectToRoute('cart_index');
        }

        $session = $request->getSession();
        $cart    = $session->get('cart', []);
        $qty     = (int)$request->request->get('qty', 1);

        if (isset($cart[$id])) {
            if ($qty <= 0) {
                unset($cart[$id]);
            } else {
                $cart[$id]['quantity'] = $qty;
            }
        }

        $session->set('cart', $cart);
        return $this->redirectToRoute('cart_index');
    }

    /**
     * Supprimer un article du panier
     */
    #[Route('/remove/{id}', name: 'remove', methods: ['POST'], requirements: ['id' => '\d+'])]
    public function remove(int $id, Request $request): Response
    {
        if (!$this->isCsrfTokenValid('cart_remove_' . $id, $request->request->get('_token'))) {
            $this->addFlash('error', 'Action non autorisée.');
            return $this->redirectToRoute('cart_index');
        }

        $session = $request->getSession();
        $cart    = $session->get('cart', []);
        unset($cart[$id]);
        $session->set('cart', $cart);

        $this->addFlash('success', 'Article retiré du panier.');
        return $this->redirectToRoute('cart_index');
    }

    /**
     * Vider le panier
     */
    #[Route('/clear', name: 'clear', methods: ['POST'])]
    public function clear(Request $request): Response
    {
        if (!$this->isCsrfTokenValid('cart_clear', $request->request->get('_token'))) {
            $this->addFlash('error', 'Action non autorisée.');
            return $this->redirectToRoute('cart_index');
        }

        $request->getSession()->set('cart', []);
        $this->addFlash('success', 'Panier vidé.');
        return $this->redirectToRoute('marketplace_index');
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private function buildCartItems(array $cart): array
    {
        $items = [];
        foreach ($cart as $id => $data) {
            $items[] = [
                'productId'  => $id,
                'nomProduit' => $data['nomProduit'],
                'prix'       => $data['prix'],
                'categorie'  => $data['categorie'] ?? null,
                'imageUrl'   => $data['imageUrl']   ?? null,
                'quantity'   => $data['quantity'],
            ];
        }
        return $items;
    }
}
