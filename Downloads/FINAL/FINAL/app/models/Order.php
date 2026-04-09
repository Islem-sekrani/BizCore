<?php

class Order extends Model
{
    public function countAll()
    {
        $stmt = $this->db->query("SELECT COUNT(*) as total FROM commande");
        return $stmt->fetch()['total'] ?? 0;
    }
}