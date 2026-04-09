<?php

class UserController extends Controller
{
    public function store()
    {
        $this->requireAuth();

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $nom = trim($_POST['nom'] ?? '');
        $prenom = trim($_POST['prenom'] ?? '');
        $email = trim($_POST['email'] ?? '');
        $password = $_POST['password'] ?? '';
        $telephone = trim($_POST['telephone'] ?? '');
        $adresse = trim($_POST['adresse'] ?? '');
        $statut = trim($_POST['statut'] ?? 'actif');
        $idRole = trim($_POST['id_role'] ?? '');

        if ($nom === '' || $prenom === '' || $email === '' || $password === '') {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $userModel = $this->model('User');
        $roleModel = $this->model('Role');

        if ($userModel->emailExists($email)) {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $roleId = null;
        if ($idRole !== '' && ctype_digit((string) $idRole) && $roleModel->exists((int) $idRole)) {
            $roleId = (int) $idRole;
        }

        $userModel->create([
            'nom' => $nom,
            'prenom' => $prenom,
            'email' => $email,
            'password' => password_hash($password, PASSWORD_DEFAULT),
            'telephone' => $telephone !== '' ? $telephone : null,
            'adresse' => $adresse !== '' ? $adresse : null,
            'statut' => $statut,
            'id_role' => $roleId,
        ]);

        header('Location: ' . BASE_URL . '/dashboard');
        exit;
    }

    public function update()
    {
        $this->requireAuth();

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $id = (int) ($_POST['id_user'] ?? 0);
        $nom = trim($_POST['nom'] ?? '');
        $prenom = trim($_POST['prenom'] ?? '');
        $email = trim($_POST['email'] ?? '');
        $password = $_POST['password'] ?? '';
        $telephone = trim($_POST['telephone'] ?? '');
        $adresse = trim($_POST['adresse'] ?? '');
        $statut = trim($_POST['statut'] ?? 'actif');
        $idRole = trim($_POST['id_role'] ?? '');

        if ($id <= 0 || $nom === '' || $prenom === '' || $email === '') {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $userModel = $this->model('User');
        $roleModel = $this->model('Role');
        $existing = $userModel->findById($id);

        if (!$existing) {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        if ($email !== $existing['email'] && $userModel->emailExistsExcept($email, $id)) {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $finalPassword = $existing['password'];
        if ($password !== '') {
            if (strlen($password) < 8) {
                header('Location: ' . BASE_URL . '/dashboard');
                exit;
            }
            $finalPassword = password_hash($password, PASSWORD_DEFAULT);
        }

        $roleId = $existing['id_role'] ?? null;
        if ($idRole !== '' && ctype_digit((string) $idRole) && $roleModel->exists((int) $idRole)) {
            $roleId = (int) $idRole;
        }

        $userModel->updateUser($id, [
            'nom' => $nom,
            'prenom' => $prenom,
            'email' => $email,
            'telephone' => $telephone !== '' ? $telephone : null,
            'adresse' => $adresse !== '' ? $adresse : null,
            'statut' => $statut,
            'id_role' => $roleId,
            'password' => $finalPassword,
        ]);

        header('Location: ' . BASE_URL . '/dashboard');
        exit;
    }

    public function delete($id = null)
    {
        $this->requireAuth();

        $id = (int) $id;
        if ($id <= 0) {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $userModel = $this->model('User');
        $userModel->delete($id);

        header('Location: ' . BASE_URL . '/dashboard');
        exit;
    }
}