<?php

class Model
{
    protected $db;

    public function __construct()
    {
        require_once BASE_PATH . '/config/database.php';
        $this->db = Database::connect();
    }
}