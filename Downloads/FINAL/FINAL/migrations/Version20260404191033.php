<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Creates the notification table for the in-app notification system.
 * INTENTIONALLY SCOPED: Only creates the notification table.
 * Does NOT modify or drop any other table — safe for shared monorepo DB.
 */
final class Version20260404191033 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Create notification table for in-app notification system';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('
            CREATE TABLE IF NOT EXISTS notification (
                id_notification INT AUTO_INCREMENT NOT NULL,
                id_user         INT NOT NULL,
                message         LONGTEXT NOT NULL,
                type            VARCHAR(30) NOT NULL DEFAULT \'reservation\',
                is_read         TINYINT(1) NOT NULL DEFAULT 0,
                created_at      DATETIME NOT NULL,
                INDEX IDX_BF5476CA6B3CA4B (id_user),
                PRIMARY KEY (id_notification)
            ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB
        ');

        $this->addSql('
            ALTER TABLE notification
                ADD CONSTRAINT FK_notification_user
                FOREIGN KEY (id_user)
                REFERENCES users (id_user)
                ON DELETE CASCADE
        ');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE notification DROP FOREIGN KEY FK_notification_user');
        $this->addSql('DROP TABLE IF EXISTS notification');
    }
}
