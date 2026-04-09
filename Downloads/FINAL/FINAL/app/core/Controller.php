<?php

class Controller
{
    protected function startSession()
    {
        if (session_status() !== PHP_SESSION_ACTIVE) {
            session_start();
        }
    }

    protected function noCache()
    {
        if (!headers_sent()) {
            header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
            header('Pragma: no-cache');
            header('Expires: 0');
        }
    }

    protected function requireAuth(bool $ajax = false)
    {
        $this->startSession();

        if (empty($_SESSION['user'])) {
            if ($ajax) {
                http_response_code(401);
                echo 'AUTH_REQUIRED';
                exit;
            }

            header('Location: ' . BASE_URL . '/auth/login');
            exit;
        }
    }

    protected function authUser()
    {
        $this->startSession();
        return $_SESSION['user'] ?? null;
    }

    public function view($view, $data = [])
    {
        $file = BASE_PATH . '/app/views/' . $view . '.php';

        if (file_exists($file)) {
            require $file;
        } else {
            die('View does not exist');
        }
    }

    public function model($model)
    {
        require_once BASE_PATH . '/app/models/' . $model . '.php';
        return new $model();
    }
}