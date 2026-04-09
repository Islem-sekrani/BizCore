<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260328170000FixReservationTable extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Fix reservation table: add PRIMARY KEY, AUTO_INCREMENT, and foreign keys';
    }

    public function up(Schema $schema): void
    {
        // Drop the old broken table and recreate it properly
        $this->addSql('DROP TABLE IF EXISTS reservation');
        
        // Create table WITHOUT foreign key constraints first
        $this->addSql('CREATE TABLE reservation (
            id_reservation INT AUTO_INCREMENT NOT NULL,
            id_user INT NOT NULL,
            id_coach INT NOT NULL,
            date_reservation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            date_seance DATE DEFAULT NULL,
            statut VARCHAR(20) NOT NULL DEFAULT \'CONFIRMEE\',
            PRIMARY KEY (id_reservation),
            INDEX idx_reservation_user (id_user),
            INDEX idx_reservation_coach (id_coach)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE IF EXISTS reservation');
    }
}
