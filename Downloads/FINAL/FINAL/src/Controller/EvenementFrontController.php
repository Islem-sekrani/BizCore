<?php

namespace App\Controller;

use App\Entity\InscriptionEvenement;
use App\Form\InscriptionEvenementType;
use App\Repository\EvenementRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class EvenementFrontController extends AbstractController
{
    #[Route('/events', name: 'evenement_front')]
    public function index(EvenementRepository $repo): Response
    {
        return $this->render('events/front_index.html.twig', [
            'evenements' => $repo->findAll(),
        ]);
    }

    #[Route('/events/{id}', name: 'evenement_show')]
    public function show(
        int $id,
        EvenementRepository $repo,
        Request $request,
        EntityManagerInterface $em
    ): Response {
        $evenement = $repo->find($id);

        if (!$evenement) {
            throw $this->createNotFoundException('Événement introuvable.');
        }

        $reservation = new InscriptionEvenement();
        $reservation->setStatut('confirmé');
        $reservation->setDateReservation(new \DateTime());
        $reservation->setEvenement($evenement);

        if ($this->getUser()) {
            $reservation->setUser($this->getUser());
            $reservation->setNom($this->getUser()->getFullName());
            $reservation->setEmail($this->getUser()->getEmail());
        }

        $form = $this->createForm(InscriptionEvenementType::class, $reservation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($reservation);
            $em->flush();

            $this->addFlash('success', '✅ Votre réservation est confirmée !');

            return $this->redirectToRoute('evenement_show', ['id' => $id]);
        }

        // isSubmitted but NOT isValid → fall through and re-render
        // Symfony keeps errors attached to form fields automatically
        return $this->render('events/show.html.twig', [
        'evenement' => $evenement,
        'form'      => $form,
    ]);
    }
}