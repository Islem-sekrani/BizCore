<?php

namespace App\Controller;

use App\Entity\Role;
use App\Entity\User;
use App\Repository\RoleRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;

class SecurityController extends AbstractController
{
    #[Route('/auth/login', name: 'app_login')]
    public function login(AuthenticationUtils $authenticationUtils): Response
    {
        if ($this->getUser()) {
            return $this->redirectToRoute('dashboard');
        }

        return $this->render('security/login.html.twig', [
            'last_username' => $authenticationUtils->getLastUsername(),
            'error' => $authenticationUtils->getLastAuthenticationError(),
        ]);
    }

    #[Route('/auth/register', name: 'app_register', methods: ['GET', 'POST'])]
    public function register(
        Request $request,
        EntityManagerInterface $em,
        UserPasswordHasherInterface $passwordHasher,
        RoleRepository $roleRepository,
        UserRepository $userRepository
    ): Response {
        if ($this->getUser()) {
            return $this->redirectToRoute('dashboard');
        }

        $error = '';
        $old = [
            'nom' => '',
            'prenom' => '',
            'email' => '',
            'telephone' => '',
            'adresse' => '',
        ];

        if ($request->isMethod('POST')) {
            $nom = trim((string) $request->request->get('nom', ''));
            $prenom = trim((string) $request->request->get('prenom', ''));
            $email = trim((string) $request->request->get('email', ''));
            $password = (string) $request->request->get('password', '');
            $telephone = trim((string) $request->request->get('telephone', ''));
            $adresse = trim((string) $request->request->get('adresse', ''));

            $old = compact('nom', 'prenom', 'email', 'telephone', 'adresse');

            if ($nom === '' || $prenom === '' || $email === '' || $password === '') {
                $error = 'Please fill in all required fields.';
            } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
                $error = 'Please enter a valid email address.';
            } elseif ($userRepository->emailExists($email)) {
                $error = 'This email is already registered.';
            } else {
                $user = new User();
                $user->setNom($nom);
                $user->setPrenom($prenom);
                $user->setEmail($email);
                $user->setPassword($passwordHasher->hashPassword($user, $password));
                $user->setTelephone($telephone !== '' ? $telephone : null);
                $user->setAdresse($adresse !== '' ? $adresse : null);
                $user->setStatut('actif');

                $defaultRole = $roleRepository->findOneBy(['nomRole' => 'User']);
                if (!$defaultRole instanceof Role) {
                    $defaultRole = $roleRepository->find(2);
                }
                $user->setRoleEntity($defaultRole);

                $em->persist($user);
                $em->flush();

                return $this->redirectToRoute('dashboard');
            }
        }

        return $this->render('security/register.html.twig', [
            'error' => $error,
            'old' => $old,
        ]);
    }

    #[Route('/auth/logout', name: 'app_logout')]
    public function logout(): void
    {
        throw new \LogicException('This should never be reached.');
    }
}
