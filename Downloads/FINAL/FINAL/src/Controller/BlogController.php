<?php

namespace App\Controller;

use App\Repository\ArticleRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class BlogController extends AbstractController
{
    #[Route('/blog', name: 'frontend_blog_index')]
public function index(ArticleRepository $articleRepository): Response
{
    $articles = $articleRepository->findAll();

    return $this->render('blog/index.html.twig', [
        'articles' => $articles
    ]);
}
}