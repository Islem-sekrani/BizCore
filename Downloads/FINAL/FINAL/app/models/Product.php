<?php

class Product extends Model
{
    public function countAll()
    {
        $stmt = $this->db->query("SELECT COUNT(*) as total FROM produit");
        return $stmt->fetch()['total'] ?? 0;
    }
}