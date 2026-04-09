<?php

class User extends Model
{
    public function countAll()
    {
        $stmt = $this->db->query("SELECT COUNT(*) AS total FROM users");
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        return (int) ($row['total'] ?? 0);
    }

    public function getAll()
    {
        $stmt = $this->db->query("SELECT * FROM users ORDER BY id_user ASC");
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function searchPaginated($search, $limit, $offset)
    {
        $sql = "SELECT * FROM users
                WHERE nom LIKE :search
                   OR prenom LIKE :search
                   OR email LIKE :search
                ORDER BY id_user ASC
                LIMIT :limit OFFSET :offset";

        $stmt = $this->db->prepare($sql);
        $stmt->bindValue(':search', '%' . $search . '%', PDO::PARAM_STR);
        $stmt->bindValue(':limit', (int) $limit, PDO::PARAM_INT);
        $stmt->bindValue(':offset', (int) $offset, PDO::PARAM_INT);
        $stmt->execute();

        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function countSearch($search)
    {
        $stmt = $this->db->prepare("
            SELECT COUNT(*)
            FROM users
            WHERE nom LIKE :search
               OR prenom LIKE :search
               OR email LIKE :search
        ");

        $stmt->execute(['search' => '%' . $search . '%']);
        return (int) $stmt->fetchColumn();
    }

    public function emailExists(string $email): bool
    {
        $stmt = $this->db->prepare("SELECT COUNT(*) FROM users WHERE email = :email");
        $stmt->execute(['email' => $email]);
        return (int) $stmt->fetchColumn() > 0;
    }

    public function emailExistsExcept(string $email, int $exceptId): bool
    {
        $stmt = $this->db->prepare("
            SELECT COUNT(*)
            FROM users
            WHERE email = :email
              AND id_user <> :id
        ");
        $stmt->execute([
            'email' => $email,
            'id' => $exceptId,
        ]);

        return (int) $stmt->fetchColumn() > 0;
    }

    public function findByEmailWithRole(string $email): ?array
    {
        $stmt = $this->db->prepare("
            SELECT u.*, r.nom_role
            FROM users u
            LEFT JOIN role r ON u.id_role = r.id_role
            WHERE u.email = :email
            LIMIT 1
        ");

        $stmt->execute(['email' => $email]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        return $row ?: null;
    }

    public function create(array $data): int
    {
        $stmt = $this->db->prepare("
        INSERT INTO users
            (nom, prenom, email, password, telephone, adresse, statut, id_role)
        VALUES
            (:nom, :prenom, :email, :password, :telephone, :adresse, :statut, :id_role)
    ");

        $stmt->execute([
            'nom' => $data['nom'],
            'prenom' => $data['prenom'],
            'email' => $data['email'],
            'password' => $data['password'],
            'telephone' => $data['telephone'] ?? null,
            'adresse' => $data['adresse'] ?? null,
            'statut' => $data['statut'] ?? 'actif',
            'id_role' => $data['id_role'] ?? 2,
        ]);

        return (int) $this->db->lastInsertId();
    }

    public function updatePassword(int $id, string $hash): bool
    {
        $stmt = $this->db->prepare("
            UPDATE users
            SET password = :password
            WHERE id_user = :id
        ");

        return $stmt->execute([
            'password' => $hash,
            'id' => $id,
        ]);
    }

    public function findByIdWithRole(int $id): ?array
    {
        $stmt = $this->db->prepare("
            SELECT u.*, r.nom_role
            FROM users u
            LEFT JOIN role r ON u.id_role = r.id_role
            WHERE u.id_user = :id
            LIMIT 1
        ");
        $stmt->execute(['id' => $id]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        return $row ?: null;
    }

    public function updateUser(int $id, array $data): bool
    {
        $stmt = $this->db->prepare("
            UPDATE users
            SET nom = :nom,
                prenom = :prenom,
                email = :email,
                telephone = :telephone,
                adresse = :adresse,
                statut = :statut,
                id_role = :id_role,
                password = :password
            WHERE id_user = :id
        ");

        return $stmt->execute([
            'nom' => $data['nom'],
            'prenom' => $data['prenom'],
            'email' => $data['email'],
            'telephone' => $data['telephone'],
            'adresse' => $data['adresse'],
            'statut' => $data['statut'],
            'id_role' => $data['id_role'],
            'password' => $data['password'],
            'id' => $id,
        ]);
    }

    public function update(int $id, array $data): bool
    {
        return $this->updateUser($id, $data);
    }

    public function delete(int $id): bool
    {
        $stmt = $this->db->prepare("DELETE FROM users WHERE id_user = :id");
        return $stmt->execute(['id' => $id]);
    }

    public function findById(int $id): ?array
    {
        $stmt = $this->db->prepare("SELECT * FROM users WHERE id_user = :id LIMIT 1");
        $stmt->execute(['id' => $id]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        return $row ?: null;
    }

    public function countActive(): int
    {
        $stmt = $this->db->query("SELECT COUNT(*) FROM users WHERE statut = 'actif'");
        return (int) $stmt->fetchColumn();
    }

    public function countInactive(): int
    {
        $stmt = $this->db->query("SELECT COUNT(*) FROM users WHERE statut <> 'actif' OR statut IS NULL");
        return (int) $stmt->fetchColumn();
    }

    public function countWithoutRole(): int
    {
        $stmt = $this->db->query("SELECT COUNT(*) FROM users WHERE id_role IS NULL");
        return (int) $stmt->fetchColumn();
    }

    public function countRecentRegistrations(int $days = 7): int
    {
        $days = max(1, (int) $days);
        $stmt = $this->db->query("
        SELECT COUNT(*)
        FROM users
        WHERE date_inscription >= DATE_SUB(NOW(), INTERVAL {$days} DAY)
    ");
        return (int) $stmt->fetchColumn();
    }

    public function countDormant(int $days = 30): int
    {
        $days = max(1, (int) $days);
        $stmt = $this->db->query("
        SELECT COUNT(*)
        FROM users
        WHERE derniere_connexion IS NULL
           OR derniere_connexion < DATE_SUB(NOW(), INTERVAL {$days} DAY)
    ");
        return (int) $stmt->fetchColumn();
    }

    public function countIncompleteProfiles(): int
    {
        $stmt = $this->db->query("
        SELECT COUNT(*)
        FROM users
        WHERE nom = ''
           OR prenom = ''
           OR email = ''
           OR telephone IS NULL
           OR telephone = ''
           OR adresse IS NULL
           OR adresse = ''
           OR id_role IS NULL
    ");
        return (int) $stmt->fetchColumn();
    }

    public function getLatestUsers(int $limit = 5): array
    {
        $limit = max(1, (int) $limit);
        $stmt = $this->db->query("
        SELECT id_user, nom, prenom, email, statut, date_inscription, derniere_connexion
        FROM users
        ORDER BY date_inscription DESC
        LIMIT {$limit}
    ");
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function getUsersNeedingAttention(int $limit = 5): array
    {
        $limit = max(1, (int) $limit);
        $stmt = $this->db->query("
        SELECT id_user, nom, prenom, email, statut, date_inscription, derniere_connexion, id_role
        FROM users
        WHERE id_role IS NULL
           OR telephone IS NULL OR telephone = ''
           OR adresse IS NULL OR adresse = ''
           OR derniere_connexion IS NULL
        ORDER BY date_inscription DESC
        LIMIT {$limit}
    ");
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function profileCompletenessPercent(array $user): int
    {
        $fields = [
            !empty($user['nom']),
            !empty($user['prenom']),
            !empty($user['email']),
            !empty($user['telephone']),
            !empty($user['adresse']),
            !empty($user['id_role']),
            !empty($user['derniere_connexion']),
        ];

        $score = 0;
        foreach ($fields as $ok) {
            $score += $ok ? 1 : 0;
        }

        return (int) round(($score / count($fields)) * 100);
    }
}