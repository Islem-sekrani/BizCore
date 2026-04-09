<?php

namespace App\Controller;

use App\Entity\User;
use App\Repository\RoleRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

class ProfileController extends AbstractController
{
    #[Route('/profile', name: 'profile_index', methods: ['GET'])]
    #[IsGranted('ROLE_USER')]
    public function index(UserRepository $userRepository): Response
    {
        $authUser = $this->getUser();

        if (!$authUser instanceof User) {
            return $this->redirectToRoute('app_logout');
        }

        $profile = $userRepository->findByIdWithRole($authUser->getId());

        if (!$profile) {
            return $this->redirectToRoute('app_logout');
        }

        return $this->render('profile/index.html.twig', [
            'profile' => $profile,
        ]);
    }

    #[Route('/profile/update', name: 'profile_update', methods: ['POST'])]
    #[IsGranted('ROLE_USER')]
    public function update(
        Request $request,
        EntityManagerInterface $em,
        UserRepository $userRepository,
        RoleRepository $roleRepository,
        UserPasswordHasherInterface $passwordHasher
    ): Response {
        $authUser = $this->getUser();

        if (!$authUser instanceof User) {
            return $this->redirectToRoute('app_logout');
        }

        $current = $userRepository->findByIdWithRole($authUser->getId());

        if (!$current instanceof User) {
            return $this->redirectToRoute('app_logout');
        }

        $nom = trim((string) $request->request->get('nom', ''));
        $prenom = trim((string) $request->request->get('prenom', ''));
        $email = trim((string) $request->request->get('email', ''));
        $telephone = trim((string) $request->request->get('telephone', ''));
        $adresse = trim((string) $request->request->get('adresse', ''));
        $currentPassword = (string) $request->request->get('current_password', '');
        $newPassword = (string) $request->request->get('new_password', '');
        $confirmPassword = (string) $request->request->get('confirm_password', '');

        if ($nom === '' || $prenom === '' || $email === '') {
            return $this->redirectToRoute('dashboard');
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            return $this->redirectToRoute('dashboard');
        }

        if ($email !== $current->getEmail() && $userRepository->emailExistsExcept($email, $current->getId())) {
            return $this->redirectToRoute('dashboard');
        }

        $currentDbPassword = $current->getPassword();
        $passwordToSave = $currentDbPassword;

        if ($currentPassword !== '' || $newPassword !== '' || $confirmPassword !== '') {
            $passwordOk = $passwordHasher->isPasswordValid($current, $currentPassword) || hash_equals($currentDbPassword, $currentPassword);

            if (!$passwordOk) {
                return $this->redirectToRoute('dashboard');
            }

            if ($newPassword === '' || $newPassword !== $confirmPassword || strlen($newPassword) < 8) {
                return $this->redirectToRoute('dashboard');
            }

            $passwordToSave = $passwordHasher->hashPassword($current, $newPassword);
        }

        $current->setNom($nom);
        $current->setPrenom($prenom);
        $current->setEmail($email);
        $current->setTelephone($telephone !== '' ? $telephone : null);
        $current->setAdresse($adresse !== '' ? $adresse : null);
        $current->setPassword($passwordToSave);

        if ($current->getRoleEntity() === null) {
            $defaultRole = $roleRepository->find(2);
            if ($defaultRole) {
                $current->setRoleEntity($defaultRole);
            }
        }

        $em->flush();

        return $this->redirectToRoute('dashboard');
    }
}