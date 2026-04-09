<?php

class ProfileController extends Controller
{
    public function index()
    {
        $this->requireAuth();
        $this->noCache();

        $userModel = $this->model('User');
        $authUser = $this->authUser();
        $profile = $userModel->findByIdWithRole((int) $authUser['id']);

        if (!$profile) {
            header('Location: ' . BASE_URL . '/auth/logout');
            exit;
        }

        $this->view('profile/index', ['profile' => $profile]);
    }

    public function update()
    {
        $this->requireAuth();
        $this->noCache();

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $authUser = $this->authUser();
        $userModel = $this->model('User');
        $current = $userModel->findByIdWithRole((int) $authUser['id']);

        $nom = trim($_POST['nom'] ?? '');
        $prenom = trim($_POST['prenom'] ?? '');
        $email = trim($_POST['email'] ?? '');
        $telephone = trim($_POST['telephone'] ?? '');
        $adresse = trim($_POST['adresse'] ?? '');
        $currentPassword = $_POST['current_password'] ?? '';
        $newPassword = $_POST['new_password'] ?? '';
        $confirmPassword = $_POST['confirm_password'] ?? '';

        if ($nom === '' || $prenom === '' || $email === '') {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $passwordToSave = $current['password'];

        if ($newPassword !== '' || $confirmPassword !== '' || $currentPassword !== '') {
            if (!password_verify($currentPassword, $current['password']) && !hash_equals($current['password'], $currentPassword)) {
                header('Location: ' . BASE_URL . '/dashboard');
                exit;
            }

            if ($newPassword !== $confirmPassword || strlen($newPassword) < 8) {
                header('Location: ' . BASE_URL . '/dashboard');
                exit;
            }

            $passwordToSave = password_hash($newPassword, PASSWORD_DEFAULT);
        }

        $userModel->updateUser((int) $authUser['id'], [
            'nom' => $nom,
            'prenom' => $prenom,
            'email' => $email,
            'telephone' => $telephone,
            'adresse' => $adresse,
            'statut' => $current['statut'] ?? 'actif',
            'id_role' => $current['id_role'] ?? 2,
            'password' => $passwordToSave,
        ]);

        $_SESSION['user']['nom'] = $nom;
        $_SESSION['user']['prenom'] = $prenom;
        $_SESSION['user']['email'] = $email;
        $_SESSION['user']['telephone'] = $telephone;
        $_SESSION['user']['adresse'] = $adresse;

        header('Location: ' . BASE_URL . '/dashboard');
        exit;
    }
}