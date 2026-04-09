<?php

namespace App\Controller;

use App\Repository\ArticleRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class HomeController extends AbstractController
{
    // Homepage (choice page or redirect if logged in)
    #[Route('/', name: 'home', methods: ['GET'])]
    public function home(): Response
    {
        if ($this->getUser()) {
            if ($this->isGranted('ROLE_ADMIN')) {
                return $this->redirectToRoute('dashboard');
            }
            return $this->redirectToRoute('app_coach_index');
        }

        return $this->render('home/choice.html.twig');
    }

}