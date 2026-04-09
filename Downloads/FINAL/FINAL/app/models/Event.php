<?php

class Event extends Model
{
    public function countAll()
    {
        $stmt = $this->db->query("SELECT COUNT(*) as total FROM evenement");
        return $stmt->fetch()['total'] ?? 0;
    }
}