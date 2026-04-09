<?php

class AuthController extends Controller
{
    public function login()
    {
        $this->noCache();
        $this->startSession();

        if (!empty($_SESSION['user'])) {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $data = [
            'error' => '',
            'old' => [
                'email' => '',
            ],
        ];

        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $email = trim($_POST['email'] ?? '');
            $password = $_POST['password'] ?? '';

            $data['old']['email'] = $email;

            if ($email === '' || $password === '') {
                $data['error'] = 'Please fill in all fields.';
                $this->view('auth/login', $data);
                return;
            }

            $userModel = $this->model('User');
            $user = $userModel->findByEmailWithRole($email);

            if (!$user) {
                $data['error'] = 'Invalid email or password.';
                $this->view('auth/login', $data);
                return;
            }

            $storedPassword = (string) $user['password'];
            $passwordOk = password_verify($password, $storedPassword) || hash_equals($storedPassword, $password);

            if (!$passwordOk) {
                $data['error'] = 'Invalid email or password.';
                $this->view('auth/login', $data);
                return;
            }

            if (!password_verify($password, $storedPassword)) {
                $userModel->updatePassword((int) $user['id_user'], password_hash($password, PASSWORD_DEFAULT));
            }

            session_regenerate_id(true);

            $_SESSION['user'] = [
                'id' => (int) $user['id_user'],
                'nom' => $user['nom'],
                'prenom' => $user['prenom'],
                'email' => $user['email'],
                'telephone' => $user['telephone'],
                'adresse' => $user['adresse'],
                'statut' => $user['statut'],
                'role_id' => $user['id_role'],
                'role' => $user['nom_role'] ?? 'User',
            ];

            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $this->view('auth/login', $data);
    }

    public function register()
    {
        $this->noCache();
        $this->startSession();

        if (!empty($_SESSION['user'])) {
            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $data = [
            'error' => '',
            'success' => '',
            'old' => [
                'nom' => '',
                'prenom' => '',
                'email' => '',
                'telephone' => '',
                'adresse' => '',
            ],
        ];

        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $nom = trim($_POST['nom'] ?? '');
            $prenom = trim($_POST['prenom'] ?? '');
            $email = trim($_POST['email'] ?? '');
            $password = $_POST['password'] ?? '';
            $telephone = trim($_POST['telephone'] ?? '');
            $adresse = trim($_POST['adresse'] ?? '');
            $idRole = 2; // normal user by default

            $data['old'] = compact('nom', 'prenom', 'email', 'telephone', 'adresse');

            if ($nom === '' || $prenom === '' || $email === '' || $password === '') {
                $data['error'] = 'Please fill in all required fields.';
                $this->view('auth/register', $data);
                return;
            }

            if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
                $data['error'] = 'Please enter a valid email address.';
                $this->view('auth/register', $data);
                return;
            }

            $userModel = $this->model('User');

            if ($userModel->emailExists($email)) {
                $data['error'] = 'This email is already registered.';
                $this->view('auth/register', $data);
                return;
            }

            $newUserId = $userModel->create([
                'nom' => $nom,
                'prenom' => $prenom,
                'email' => $email,
                'password' => password_hash($password, PASSWORD_DEFAULT),
                'telephone' => $telephone,
                'adresse' => $adresse,
                'statut' => 'actif',
                'id_role' => $idRole,
            ]);

            $createdUser = $userModel->findByIdWithRole($newUserId);

            session_regenerate_id(true);

            $_SESSION['user'] = [
                'id' => (int) $createdUser['id_user'],
                'nom' => $createdUser['nom'],
                'prenom' => $createdUser['prenom'],
                'email' => $createdUser['email'],
                'telephone' => $createdUser['telephone'],
                'adresse' => $createdUser['adresse'],
                'statut' => $createdUser['statut'],
                'role_id' => $createdUser['id_role'],
                'role' => $createdUser['nom_role'] ?? 'User',
            ];

            header('Location: ' . BASE_URL . '/dashboard');
            exit;
        }

        $this->view('auth/register', $data);
    }

    public function logout()
    {
        $this->noCache();
        $this->startSession();

        $_SESSION = [];

        if (ini_get('session.use_cookies')) {
            $params = session_get_cookie_params();
            setcookie(
                session_name(),
                '',
                time() - 42000,
                $params['path'],
                $params['domain'],
                $params['secure'],
                $params['httponly']
            );
        }

        session_destroy();

        header('Location: ' . BASE_URL . '/auth/login');
        exit;
    }
}