<?php

class Role extends Model
{
    public function getAll(): array
    {
        $stmt = $this->db->query("SELECT id_role, nom_role FROM role ORDER BY id_role ASC");
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function exists(int $id): bool
    {
        $stmt = $this->db->prepare("SELECT COUNT(*) FROM role WHERE id_role = :id");
        $stmt->execute(['id' => $id]);

        return (int) $stmt->fetchColumn() > 0;
    }
}