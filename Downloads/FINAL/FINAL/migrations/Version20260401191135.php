<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260401191135 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // Drop evenement first to avoid ALTER issues with corrupt/incompatible engine
        $this->addSql('DROP TABLE IF EXISTS evenement');

        // Recreate evenement with the new schema your Entity expects
        $this->addSql('CREATE TABLE evenement (
            id INT AUTO_INCREMENT NOT NULL,
            title VARCHAR(255) NOT NULL,
            description LONGTEXT NOT NULL,
            price DOUBLE PRECISION NOT NULL,
            date DATETIME NOT NULL,
            capacity INT NOT NULL,
            image VARCHAR(255) DEFAULT NULL,
            PRIMARY KEY (id)
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        // Messenger messages table (Symfony internal)
        $this->addSql('CREATE TABLE IF NOT EXISTS messenger_messages (
            id BIGINT AUTO_INCREMENT NOT NULL,
            body LONGTEXT NOT NULL,
            headers LONGTEXT NOT NULL,
            queue_name VARCHAR(190) NOT NULL,
            created_at DATETIME NOT NULL,
            available_at DATETIME NOT NULL,
            delivered_at DATETIME DEFAULT NULL,
            INDEX IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750 (queue_name, available_at, delivered_at, id),
            PRIMARY KEY (id)
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        // Drop old unused tables safely
        $this->addSql('DROP TABLE IF EXISTS article');
        $this->addSql('DROP TABLE IF EXISTS categorie_evenement');
        $this->addSql('DROP TABLE IF EXISTS coach');
        $this->addSql('DROP TABLE IF EXISTS commande');
        $this->addSql('DROP TABLE IF EXISTS commentaire_blog');
        $this->addSql('DROP TABLE IF EXISTS commentaire_evenement');
        $this->addSql('DROP TABLE IF EXISTS domaine_coaching');
        $this->addSql('DROP TABLE IF EXISTS inscription_evenement');
        $this->addSql('DROP TABLE IF EXISTS ligne_commande');
        $this->addSql('DROP TABLE IF EXISTS produit');
        $this->addSql('DROP TABLE IF EXISTS role');
        $this->addSql('DROP TABLE IF EXISTS session_coaching');
        $this->addSql('DROP TABLE IF EXISTS sponsor');
        $this->addSql('DROP TABLE IF EXISTS sponsoring');
        $this->addSql('DROP TABLE IF EXISTS users');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE IF EXISTS evenement');
        $this->addSql('DROP TABLE IF EXISTS messenger_messages');
    }
}