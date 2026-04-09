<?php

class Article extends Model
{
    public function countAll()
    {
        $stmt = $this->db->query("SELECT COUNT(*) as total FROM article");
        return $stmt->fetch()['total'] ?? 0;
    }
}