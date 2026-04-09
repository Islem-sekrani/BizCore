<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260322175652 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Create coach table';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('CREATE TABLE IF NOT EXISTS coach (id_coach INT AUTO_INCREMENT NOT NULL, id_user INT DEFAULT NULL, nom VARCHAR(20) NOT NULL, prenom VARCHAR(20) NOT NULL, biographie LONGTEXT DEFAULT NULL, experience_annees INT DEFAULT NULL, tarif_horaire DECIMAL(10, 2) DEFAULT NULL, disponibilite VARCHAR(20) DEFAULT NULL, id_domaine INT DEFAULT NULL, domaine VARCHAR(50) DEFAULT NULL, num_tel VARCHAR(20) DEFAULT NULL, note_moyenne DECIMAL(3, 2) DEFAULT NULL, certification VARCHAR(500) DEFAULT NULL, PRIMARY KEY(id_coach)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE IF EXISTS coach');
    }
}
