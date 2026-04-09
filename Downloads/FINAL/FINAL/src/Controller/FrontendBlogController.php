<?php

namespace App\Controller;

use App\Repository\ArticleRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class FrontendBlogController extends AbstractController
{
    #[Route('/blog', name: 'frontend_blog_index', methods: ['GET'])]
    public function index(Request $request, ArticleRepository $articleRepository): Response
    {
        $sortBy  = $request->query->get('sortBy', 'id_article');
        $sortDir = $request->query->get('sortDir', 'DESC');

        return $this->render('blog/index.html.twig', [
            'articles' => $articleRepository->findAllSorted($sortBy, $sortDir),
            'sortBy'   => $sortBy,
            'sortDir'  => $sortDir,
            'mode'     => 'user',
        ]);
    }

    #[Route('/blog/{id}', name: 'frontend_blog_show', methods: ['GET'])]
    public function show(int $id, ArticleRepository $articleRepository, EntityManagerInterface $entityManager): Response
    {
        $article = $articleRepository->find($id);

        if (!$article) {
            throw $this->createNotFoundException('Article introuvable.');
        }

        $article->setNombreVues($article->getNombreVues() + 1);
        $entityManager->flush();

        return $this->render('article/show.html.twig', [
            'article' => $article,
        ]);
    }
}
