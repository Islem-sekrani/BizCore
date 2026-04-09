<?php

namespace App\Controller;

use App\Entity\User;
use App\Repository\RoleRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

class UserController extends AbstractController
{
    #[Route('/user/store', name: 'user_store', methods: ['POST'])]
    #[IsGranted('ROLE_USER')]
    public function store(
        Request $request,
        EntityManagerInterface $em,
        RoleRepository $roleRepository,
        UserRepository $userRepository,
        UserPasswordHasherInterface $passwordHasher
    ): RedirectResponse {
        $nom = trim((string) $request->request->get('nom', ''));
        $prenom = trim((string) $request->request->get('prenom', ''));
        $email = trim((string) $request->request->get('email', ''));
        $password = (string) $request->request->get('password', '');
        $telephone = trim((string) $request->request->get('telephone', ''));
        $adresse = trim((string) $request->request->get('adresse', ''));
        $statut = trim((string) $request->request->get('statut', 'actif'));
        $idRole = trim((string) $request->request->get('id_role', ''));

        if ($nom === '' || $prenom === '' || $email === '' || $password === '') {
            return $this->redirectToRoute('dashboard');
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            return $this->redirectToRoute('dashboard');
        }

        if ($userRepository->emailExists($email)) {
            return $this->redirectToRoute('dashboard');
        }

        $user = new User();
        $user->setNom($nom);
        $user->setPrenom($prenom);
        $user->setEmail($email);
        $user->setPassword($passwordHasher->hashPassword($user, $password));
        $user->setTelephone($telephone !== '' ? $telephone : null);
        $user->setAdresse($adresse !== '' ? $adresse : null);
        $user->setStatut($statut !== '' ? $statut : 'actif');

        if ($idRole !== '' && ctype_digit($idRole)) {
            $role = $roleRepository->find((int) $idRole);
            if ($role) {
                $user->setRoleEntity($role);
            }
        }

        $em->persist($user);
        $em->flush();

        return $this->redirectToRoute('dashboard');
    }

    #[Route('/user/update', name: 'user_update', methods: ['POST'])]
    #[IsGranted('ROLE_USER')]
    public function update(
        Request $request,
        EntityManagerInterface $em,
        RoleRepository $roleRepository,
        UserRepository $userRepository,
        UserPasswordHasherInterface $passwordHasher
    ): RedirectResponse {
        $id = (int) $request->request->get('id_user', 0);
        $nom = trim((string) $request->request->get('nom', ''));
        $prenom = trim((string) $request->request->get('prenom', ''));
        $email = trim((string) $request->request->get('email', ''));
        $password = (string) $request->request->get('password', '');
        $telephone = trim((string) $request->request->get('telephone', ''));
        $adresse = trim((string) $request->request->get('adresse', ''));
        $statut = trim((string) $request->request->get('statut', 'actif'));
        $idRole = trim((string) $request->request->get('id_role', ''));

        if ($id <= 0 || $nom === '' || $prenom === '' || $email === '') {
            return $this->redirectToRoute('dashboard');
        }

        $user = $userRepository->find($id);
        if (!$user instanceof User) {
            return $this->redirectToRoute('dashboard');
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            return $this->redirectToRoute('dashboard');
        }

        if ($email !== $user->getEmail() && $userRepository->emailExistsExcept($email, $id)) {
            return $this->redirectToRoute('dashboard');
        }

        $user->setNom($nom);
        $user->setPrenom($prenom);
        $user->setEmail($email);
        $user->setTelephone($telephone !== '' ? $telephone : null);
        $user->setAdresse($adresse !== '' ? $adresse : null);
        $user->setStatut($statut !== '' ? $statut : 'actif');

        if ($password !== '') {
            if (strlen($password) < 8) {
                return $this->redirectToRoute('dashboard');
            }

            $user->setPassword($passwordHasher->hashPassword($user, $password));
        }

        if ($idRole !== '' && ctype_digit($idRole)) {
            $role = $roleRepository->find((int) $idRole);
            $user->setRoleEntity($role ?: null);
        } else {
            $user->setRoleEntity(null);
        }

        $em->flush();

        return $this->redirectToRoute('dashboard');
    }

    #[Route('/user/delete/{id}', name: 'user_delete', requirements: ['id' => '\d+'], methods: ['GET'])]
    #[IsGranted('ROLE_USER')]
    public function delete(int $id, EntityManagerInterface $em, UserRepository $userRepository): RedirectResponse
    {
        $user = $userRepository->find($id);

        if ($user instanceof User) {
            $em->remove($user);
            $em->flush();
        }

        return $this->redirectToRoute('dashboard');
    }
}