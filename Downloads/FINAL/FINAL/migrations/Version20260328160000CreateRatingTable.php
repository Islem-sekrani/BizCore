<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260328160000CreateRatingTable extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Create rating table with proper foreign key references to users and coach tables';
    }

    public function up(Schema $schema): void
    {
        // Create rating table without foreign keys first
        $this->addSql('CREATE TABLE IF NOT EXISTS rating (
            id_rating INT AUTO_INCREMENT NOT NULL,
            note INT NOT NULL,
            commentaire LONGTEXT DEFAULT NULL,
            date_creation DATETIME NOT NULL,
            date_modification DATETIME NOT NULL,
            id_user INT NOT NULL,
            id_coach INT NOT NULL,
            PRIMARY KEY (id_rating),
            UNIQUE INDEX UNIQ_RATING_USER_COACH (id_user, id_coach),
            INDEX idx_rating_user (id_user),
            INDEX idx_rating_coach (id_coach)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE IF EXISTS rating');
    }
}
