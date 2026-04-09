<?php

namespace App\Controller;

use App\Service\ProductService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\RequestStack;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/marketplace', name: 'marketplace_')]
class MarketplaceController extends AbstractController
{
    public function __construct(
        private ProductService $productService,
        private RequestStack   $requestStack,
    ) {}

    #[Route('', name: 'index')]
    public function index(): Response
    {
        $products   = $this->productService->getAllProducts();
        $categories = $this->productService->getCategories();

        $session   = $this->requestStack->getSession();
        $cart      = $session->get('cart', []);
        $cartCount = array_sum(array_column($cart, 'quantity'));

        return $this->render('marketplace/index.html.twig', [
            'products'   => $products,
            'categories' => $categories,
            'cart_count' => $cartCount,
        ]);
    }
}
