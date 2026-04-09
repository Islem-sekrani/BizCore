-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : jeu. 09 avr. 2026 à 18:20
-- Version du serveur : 10.4.28-MariaDB
-- Version de PHP : 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `bizcore`
--

-- --------------------------------------------------------

--
-- Structure de la table `article`
--

CREATE TABLE `article` (
  `id_article` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `contenu` text NOT NULL,
  `image_principale` varchar(500) DEFAULT NULL,
  `categorie` varchar(100) DEFAULT NULL,
  `statut` varchar(20) DEFAULT NULL,
  `nombre_vues` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `article`
--

INSERT INTO `article` (`id_article`, `titre`, `contenu`, `image_principale`, `categorie`, `statut`, `nombre_vues`) VALUES
(1, 'How to start training smart', 'Training consistency beats random effort.', 'a1.jpg', 'Fitness', 'published', 145),
(2, 'The basics of coaching', 'A good coach focuses on goals and progress.', 'a2.jpg', 'Coaching', 'published', 98),
(3, 'Tech skills for beginners', 'Start with logic, practice, and small projects.', 'a3.jpg', 'Tech', 'draft', 31),
(4, 'Why routines matter', 'Simple routines create strong results over time.', 'a4.jpg', 'Lifestyle', 'published', 70);

-- --------------------------------------------------------

--
-- Structure de la table `categorie_evenement`
--

CREATE TABLE `categorie_evenement` (
  `id_categorie` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `categorie_evenement`
--

INSERT INTO `categorie_evenement` (`id_categorie`, `nom`, `description`) VALUES
(1, 'Conference', 'Talks and panels'),
(2, 'Workshop', 'Hands-on sessions'),
(3, 'Competition', 'Challenge-based event');

-- --------------------------------------------------------

--
-- Structure de la table `coach`
--

CREATE TABLE `coach` (
  `id_coach` int(11) NOT NULL,
  `id_user` int(11) DEFAULT NULL,
  `id_domaine` int(11) DEFAULT NULL,
  `domaine` varchar(50) DEFAULT NULL,
  `nom` varchar(20) NOT NULL,
  `prenom` varchar(20) NOT NULL,
  `biographie` text DEFAULT NULL,
  `experience_annees` int(11) DEFAULT NULL,
  `tarif_horaire` decimal(10,2) DEFAULT NULL,
  `disponibilite` varchar(20) DEFAULT NULL,
  `note_moyenne` decimal(3,2) DEFAULT NULL,
  `num_tel` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `coach`
--

INSERT INTO `coach` (`id_coach`, `id_user`, `id_domaine`, `domaine`, `nom`, `prenom`, `biographie`, `experience_annees`, `tarif_horaire`, `disponibilite`, `note_moyenne`, `num_tel`) VALUES
(1, 3, 1, 'Financement', 'Haddad', 'abelkader', 'Fitness coach with a focus on strength and conditioning.', 6, 45.00, 'Disponible', 4.70, '+21622569987'),
(4, NULL, 1, 'Branding', 'Ben Ali', 'Sami', 'Expert en branding avec 10 ans dans le marketing digital.', 10, 85.00, 'Disponible', 4.50, '+21650123456'),
(5, NULL, 2, 'E-Commerce', 'Trabelsi', 'Amira', 'Consultante e-commerce specialisee en marketplace.', 7, 70.00, 'Disponible', 4.20, '+21651234567'),
(6, NULL, 3, 'Leadership', 'Bouazizi', 'Karim', 'Coach en leadership et gestion des equipes.', 12, 95.00, 'Disponible', 4.80, '+21652345678'),
(7, NULL, 4, 'Finance', 'Chahed', 'Leila', 'Experte en finance et gestion de portefeuille.', 15, 120.00, 'Disponible', 4.90, '+21653456789'),
(8, NULL, 5, 'Financement', 'Jebali', 'Mehdi', 'Specialiste en financement de startups et levee de fonds.', 8, 90.00, 'Indisponible', 3.80, '+21654567890'),
(9, NULL, 1, 'Branding', 'Gharbi', 'Nour', 'Strategiste de marque personnelle et corporate.', 6, 65.00, 'Disponible', 4.10, '+21655678901'),
(10, NULL, 2, 'E-Commerce', 'Maalej', 'Youssef', 'Developpeur e-commerce et expert Shopify/WooCommerce.', 5, 60.00, 'Disponible', 3.90, '+21656789012'),
(11, NULL, 3, 'Leadership', 'Khelifi', 'Ines', 'Formatrice en soft skills et communication.', 9, 80.00, 'Disponible', 4.60, '+21657890123'),
(12, NULL, 4, 'Finance', 'Hammami', 'Omar', 'Analyste financier et conseiller en investissements.', 11, 110.00, 'Indisponible', 4.30, '+21658901234'),
(13, NULL, 5, 'Financement', 'Saidi', 'Fatma', 'Coach en crowdfunding et subventions.', 4, 55.00, 'Disponible', 3.70, '+21659012345'),
(14, NULL, 1, 'Branding', 'Riahi', 'Amine', 'Expert en identite visuelle et design thinking.', 8, 75.00, 'Disponible', 4.40, '+21660123456'),
(15, NULL, 2, 'E-Commerce', 'Ferchichi', 'Salma', 'Consultante Amazon FBA et logistique e-commerce.', 6, 68.00, 'Disponible', 4.00, '+21661234567'),
(16, NULL, 3, 'Leadership', 'Tej', 'Bilel', 'Coach executif pour dirigeants et managers.', 14, 130.00, 'Disponible', 4.95, '+21662345678'),
(17, NULL, 4, 'Finance', 'Zarrouk', 'Hiba', 'Experte en comptabilite et optimisation fiscale.', 10, 100.00, 'Indisponible', 4.50, '+21663456789'),
(18, NULL, 5, 'Financement', 'Brahmi', 'Rami', 'Specialiste en business angels et capital risque.', 7, 85.00, 'Disponible', 4.20, '+21664567890'),
(19, NULL, 1, 'Branding', 'Mechri', 'Yasmine', 'Coach en storytelling et content marketing.', 5, 58.00, 'Disponible', 3.60, '+21665678901'),
(20, NULL, 2, 'E-Commerce', 'Daly', 'Anis', 'Expert en SEO et conversion e-commerce.', 9, 88.00, 'Disponible', 4.70, '+21666789012'),
(21, NULL, 3, 'Leadership', 'Selmi', 'Mariam', 'Coach en intelligence emotionnelle et resilience.', 11, 105.00, 'Disponible', 4.85, '+21667890123'),
(22, NULL, 4, 'Finance', 'Lakhdhar', 'Tarek', 'Consultant en gestion de tresorerie PME.', 13, 115.00, 'Disponible', 4.60, '+21668901234'),
(23, NULL, 5, 'Financement', 'Neji', 'Sara', 'Experte en microfinance et inclusion financiere.', 6, 62.00, 'Indisponible', 4.10, '+21669012345');

-- --------------------------------------------------------

--
-- Structure de la table `commande`
--

CREATE TABLE `commande` (
  `id_commande` int(11) NOT NULL,
  `date_commande` date NOT NULL,
  `montant_total` decimal(10,2) NOT NULL,
  `statut` varchar(20) DEFAULT NULL,
  `mode_paiement` varchar(50) DEFAULT NULL,
  `id_user` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `commande`
--

INSERT INTO `commande` (`id_commande`, `date_commande`, `montant_total`, `statut`, `mode_paiement`, `id_user`) VALUES
(4, '2026-04-08', 150.00, 'validee', 'carte', 1),
(5, '2026-04-08', 320.50, 'en_attente', 'especes', 2);

-- --------------------------------------------------------

--
-- Structure de la table `commentaire_blog`
--

CREATE TABLE `commentaire_blog` (
  `id_commentaire` int(11) NOT NULL,
  `contenu` text NOT NULL,
  `statut` varchar(20) DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp(),
  `date_modification` timestamp NULL DEFAULT NULL,
  `id_article` int(11) DEFAULT NULL,
  `id_user` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `commentaire_blog`
--

INSERT INTO `commentaire_blog` (`id_commentaire`, `contenu`, `statut`, `date_creation`, `date_modification`, `id_article`, `id_user`) VALUES
(1, 'Very clear and useful article.', 'approved', '2026-04-06 23:55:11', NULL, 1, 2),
(2, 'Great coaching explanation.', 'approved', '2026-04-06 23:55:11', NULL, 2, 5),
(4, 'Nice structure and examples.', 'approved', '2026-04-06 23:55:11', NULL, 4, 2);

-- --------------------------------------------------------

--
-- Structure de la table `commentaire_evenement`
--

CREATE TABLE `commentaire_evenement` (
  `id_commentaire` int(11) NOT NULL,
  `contenu` text NOT NULL,
  `note` int(11) DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp(),
  `id_evenement` int(11) DEFAULT NULL,
  `id_user` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `commentaire_evenement`
--

INSERT INTO `commentaire_evenement` (`id_commentaire`, `contenu`, `note`, `date_creation`, `id_evenement`, `id_user`) VALUES
(1, 'Amazing energy and good organization.', 5, '2026-04-06 23:55:11', 1, 2),
(2, 'Very helpful business workshop.', 4, '2026-04-06 23:55:11', 2, 5),
(4, 'Good speakers and clear content.', 4, '2026-04-06 23:55:11', 4, 2);

-- --------------------------------------------------------

--
-- Structure de la table `domaine_coaching`
--

CREATE TABLE `domaine_coaching` (
  `id_domaine` int(11) NOT NULL,
  `nom_domaine` varchar(100) NOT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `domaine_coaching`
--

INSERT INTO `domaine_coaching` (`id_domaine`, `nom_domaine`, `description`) VALUES
(1, 'Branding', 'Coaching en image de marque et identité visuelle'),
(2, 'E-Commerce', 'Coaching en vente en ligne et commerce digital'),
(3, 'Leadership', 'Coaching en management et développement du leadership'),
(4, 'Finance', 'Coaching en gestion financière personnelle et professionnelle'),
(5, 'Financement', 'Coaching en levée de fonds et financement de projets');

-- --------------------------------------------------------

--
-- Structure de la table `evenement`
--

CREATE TABLE `evenement` (
  `id_evenement` int(11) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `lieu` varchar(255) DEFAULT NULL,
  `capacite_max` int(11) DEFAULT NULL,
  `prix` decimal(10,2) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `statut` varchar(20) DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp(),
  `id_organisateur` int(11) DEFAULT NULL,
  `id_categorie` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `evenement`
--

INSERT INTO `evenement` (`id_evenement`, `titre`, `description`, `date_debut`, `date_fin`, `lieu`, `capacite_max`, `prix`, `image_url`, `statut`, `date_creation`, `id_organisateur`, `id_categorie`) VALUES
(1, 'Fitness Bootcamp', 'High intensity group training session.', '2026-04-20 09:00:00', '2026-04-20 12:00:00', 'Tunis', 30, 25.00, 'e1.jpg', 'active', '2026-04-06 23:55:11', 1, 1),
(2, 'Startup Workshop', 'Business workshop for young founders.', '2026-04-25 14:00:00', '2026-04-25 17:00:00', 'Sfax', 40, 40.00, 'e2.jpg', 'active', '2026-04-06 23:55:11', 2, 2),
(3, 'Tech Challenge', 'Competition for beginner developers.', '2026-05-02 10:00:00', '2026-05-02 18:00:00', 'Sousse', 50, 15.00, 'e3.jpg', 'active', '2026-04-06 23:55:11', 1, 3),
(4, 'Wellness Conference', 'Talks about health and productivity.', '2026-05-10 09:00:00', '2026-05-10 13:00:00', 'Ariana', 100, 10.00, 'e4.jpg', 'active', '2026-04-06 23:55:11', 2, 1);

-- --------------------------------------------------------

--
-- Structure de la table `inscription_evenement`
--

CREATE TABLE `inscription_evenement` (
  `id_inscription` int(11) NOT NULL,
  `id_evenement` int(11) DEFAULT NULL,
  `id_user` int(11) DEFAULT NULL,
  `date_inscription` timestamp NOT NULL DEFAULT current_timestamp(),
  `statut` varchar(20) DEFAULT NULL,
  `nom` varchar(100) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `nombre_places` int(11) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `inscription_evenement`
--

INSERT INTO `inscription_evenement` (`id_inscription`, `id_evenement`, `id_user`, `date_inscription`, `statut`, `nom`, `email`, `telephone`, `nombre_places`) VALUES
(1, 1, 2, '2026-04-06 23:55:11', 'confirmed', NULL, NULL, NULL, 1),
(2, 1, 5, '2026-04-06 23:55:11', 'confirmed', NULL, NULL, NULL, 1),
(4, 3, 2, '2026-04-06 23:55:11', 'pending', NULL, NULL, NULL, 1),
(5, 4, 5, '2026-04-06 23:55:11', 'confirmed', NULL, NULL, NULL, 1);

-- --------------------------------------------------------

--
-- Structure de la table `ligne_commande`
--

CREATE TABLE `ligne_commande` (
  `id_ligne` int(11) NOT NULL,
  `id_commande` int(11) DEFAULT NULL,
  `id_produit` int(11) DEFAULT NULL,
  `quantite` int(11) NOT NULL,
  `prix_unitaire` decimal(10,2) NOT NULL,
  `sous_total` decimal(10,2) NOT NULL,
  `taxe` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `notification`
--

CREATE TABLE `notification` (
  `id_notification` int(11) NOT NULL,
  `id_user` int(11) NOT NULL,
  `message` longtext NOT NULL,
  `type` varchar(30) NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `notification`
--

INSERT INTO `notification` (`id_notification`, `id_user`, `message`, `type`, `is_read`, `created_at`) VALUES
(1, 19, 'Votre réservation avec Youssef Haddad pour le 09/04/2026 a bien été confirmée.', 'reservation', 1, '2026-04-09 03:54:56');

-- --------------------------------------------------------

--
-- Structure de la table `produit`
--

CREATE TABLE `produit` (
  `id_produit` int(11) NOT NULL,
  `nom_produit` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `prix` decimal(10,2) NOT NULL,
  `stock_disponible` int(11) DEFAULT NULL,
  `categorie` varchar(100) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `statut` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `produit`
--

INSERT INTO `produit` (`id_produit`, `nom_produit`, `description`, `prix`, `stock_disponible`, `categorie`, `image_url`, `statut`) VALUES
(7, 'Formation Compléte ??? Lancer votre Business en 30 Jours', 'Passez de l id??e au revenu en 30 jours chrono. Cette formation 100 en ligne couvre 12 modules vid??o : validation de votre id??e, cr??ation de votre offre irr??sistible, strat??gie de prix, acquisition clients digitale et automatisation des ventes. Acc??s ?? une communaut?? priv??e d entrepreneurs tunisiens. Plus de 500 alumni ont lanc?? leur activit??.', 199.00, 100, 'Formation', '/uploads/img_69d7cb736a4eb.jpeg', 'Disponible');

-- --------------------------------------------------------

--
-- Structure de la table `rating`
--

CREATE TABLE `rating` (
  `id_rating` int(11) NOT NULL,
  `id_user` int(11) NOT NULL,
  `id_coach` int(11) NOT NULL,
  `note` int(11) NOT NULL,
  `commentaire` longtext DEFAULT NULL,
  `date_creation` datetime NOT NULL,
  `date_modification` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `reservation`
--

CREATE TABLE `reservation` (
  `id_reservation` int(11) NOT NULL,
  `id_user` int(11) NOT NULL,
  `id_coach` int(11) NOT NULL,
  `date_reservation` datetime NOT NULL,
  `date_seance` date DEFAULT NULL,
  `statut` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `reservation`
--

INSERT INTO `reservation` (`id_reservation`, `id_user`, `id_coach`, `date_reservation`, `date_seance`, `statut`) VALUES
(1, 19, 1, '2026-04-09 03:54:56', '2026-04-09', 'CONFIRMEE');

-- --------------------------------------------------------

--
-- Structure de la table `role`
--

CREATE TABLE `role` (
  `id_role` int(11) NOT NULL,
  `nom_role` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `role`
--

INSERT INTO `role` (`id_role`, `nom_role`, `description`, `date_creation`) VALUES
(1, 'Admin', 'Full access', '2026-04-06 23:55:10'),
(2, 'User', 'Standard account', '2026-04-06 23:55:10'),
(3, 'Coach', 'Coach account', '2026-04-06 23:55:10');

-- --------------------------------------------------------

--
-- Structure de la table `sponsor`
--

CREATE TABLE `sponsor` (
  `id_sponsor` int(11) NOT NULL,
  `nom_entreprise` varchar(200) NOT NULL,
  `logo_url` varchar(500) DEFAULT NULL,
  `contact_email` varchar(150) DEFAULT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `site_web` varchar(255) DEFAULT NULL,
  `secteur_activite` varchar(100) DEFAULT NULL,
  `type_sponsor` varchar(50) DEFAULT NULL,
  `is_actif` tinyint(1) DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `sponsor`
--

INSERT INTO `sponsor` (`id_sponsor`, `nom_entreprise`, `logo_url`, `contact_email`, `telephone`, `site_web`, `secteur_activite`, `type_sponsor`, `is_actif`, `date_creation`) VALUES
(1, 'HealthPlus', 's1.png', 'contact@healthplus.test', '700000001', 'www.healthplus.test', 'Fitness', 'Gold', 1, '2026-04-06 23:55:11'),
(2, 'CodeWave', 's2.png', 'hello@codewave.test', '700000002', 'www.codewave.test', 'Tech', 'Silver', 1, '2026-04-06 23:55:11'),
(3, 'BizMentor', 's3.png', 'info@bizmentor.test', '700000003', 'www.bizmentor.test', 'Business', 'Bronze', 1, '2026-04-06 23:55:11');

-- --------------------------------------------------------

--
-- Structure de la table `sponsoring`
--

CREATE TABLE `sponsoring` (
  `id_evenement` int(11) NOT NULL,
  `id_sponsor` int(11) NOT NULL,
  `montant_sponsoring` decimal(10,2) DEFAULT NULL,
  `niveau_sponsoring` varchar(20) DEFAULT NULL,
  `date_signature` datetime DEFAULT NULL,
  `avantages_offerts` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `sponsoring`
--

INSERT INTO `sponsoring` (`id_evenement`, `id_sponsor`, `montant_sponsoring`, `niveau_sponsoring`, `date_signature`, `avantages_offerts`) VALUES
(1, 1, 5000.00, 'Gold', '2026-04-01 10:00:00', 'Banner, stand, logo placement'),
(2, 3, 2500.00, 'Silver', '2026-04-03 11:30:00', 'Logo on poster and social media mention'),
(3, 2, 4000.00, 'Gold', '2026-04-04 09:15:00', 'Branding, booth, workshop mention'),
(4, 1, 1500.00, 'Bronze', '2026-04-05 14:00:00', 'Logo on event page');

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `id_user` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `adresse` varchar(500) DEFAULT NULL,
  `statut` varchar(20) DEFAULT NULL,
  `date_inscription` timestamp NOT NULL DEFAULT current_timestamp(),
  `derniere_connexion` timestamp NULL DEFAULT NULL,
  `id_role` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id_user`, `nom`, `prenom`, `email`, `password`, `telephone`, `adresse`, `statut`, `date_inscription`, `derniere_connexion`, `id_role`) VALUES
(1, 'Hasnaoui', 'Omar', 'omar@bizcore.test', '$2y$13$WK9VewszmNStTKO3nxd5NeUPz4XnIWiTPeJXoak0aBvi8KuF8bFKC', '53 242 854', 'Tunis', 'actif', '2026-04-06 23:55:10', NULL, 1),
(2, 'Mansour', 'Sara', 'sara@bizcore.test', 'password123', '200000002', 'Sfax', 'actif', '2026-04-06 23:55:10', NULL, 2),
(3, 'Haddad', 'Youssef', 'youssef@bizcore.test', 'password123', '200000003', 'Sousse', 'actif', '2026-04-06 23:55:10', NULL, 3),
(4, 'Trabelsi', 'Nour', 'nour@bizcore.test', 'password123', '200000004', 'Bizerte', 'actif', '2026-04-06 23:55:10', NULL, 3),
(5, 'Khelifi', 'Meriem', 'meriem@bizcore.test', 'password123', '200000005', 'Ariana', 'actif', '2026-04-06 23:55:10', NULL, 3),
(8, 'Smith', 'John', 'john1@test.com', '$2y$13$REALHASH123456789ABC', '12345678', 'Tunis', 'actif', '2026-04-08 11:02:10', NULL, 1),
(9, 'Doe', 'Jane', 'jane2@test.com', '$2y$13$REALHASH123456789ABC', '22345678', 'Sousse', 'actif', '2026-04-08 11:02:10', NULL, 1),
(10, 'Ali', 'Karim', 'karim3@test.com', '$2y$13$REALHASH123456789ABC', '32345678', 'Sfax', 'actif', '2026-04-08 11:02:10', NULL, 2),
(11, 'Ben', 'Youssef', 'youssef4@test.com', '$2y$13$REALHASH123456789ABC', '42345678', 'Nabeul', 'actif', '2026-04-08 11:02:10', NULL, 2),
(12, 'Trabelsi', 'Amine', 'amine5@test.com', '$2y$13$REALHASH123456789ABC', '52345678', 'Monastir', 'actif', '2026-04-08 11:02:10', NULL, 1),
(13, 'Gharbi', 'Sami', 'sami6@test.com', '$2y$13$REALHASH123456789ABC', '62345678', 'Gabes', 'actif', '2026-04-08 11:02:10', NULL, 2),
(14, 'Haddad', 'Rania', 'rania7@test.com', '$2y$13$REALHASH123456789ABC', '72345678', 'Bizerte', 'actif', '2026-04-08 11:02:10', NULL, 1),
(15, 'Mejri', 'Omar', 'omar8@test.com', '$2y$13$REALHASH123456789ABC', '82345678', 'Kairouan', 'actif', '2026-04-08 11:02:10', NULL, 2),
(16, 'Khaled', 'Nour', 'nour9@test.com', '$2y$13$REALHASH123456789ABC', '92345678', 'Zaghouan', 'actif', '2026-04-08 11:02:10', NULL, 1),
(17, 'Hamdi', 'Firas', 'firas10@test.com', '$2y$13$REALHASH123456789ABC', '10345678', 'Ariana', 'actif', '2026-04-08 11:02:10', NULL, 2),
(18, 'Admin', 'Admin', 'admin@bizcore.tn', '$2y$10$wHNc8yk0m6CgfRWXQK89Gej7vkYOBbSRYHe86fk3ZoNBHI4exp2ru', NULL, NULL, 'actif', '2026-04-08 22:12:57', NULL, 1),
(19, 'User', 'User', 'user@bizcore.tn', '$2y$10$Fz/P0X9H0ek0wrRitoQ/Vu0NXMEBVmmgyWrO.FpiqJuUontqnslBK', NULL, NULL, 'actif', '2026-04-08 22:12:57', NULL, 2);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `article`
--
ALTER TABLE `article`
  ADD PRIMARY KEY (`id_article`);

--
-- Index pour la table `categorie_evenement`
--
ALTER TABLE `categorie_evenement`
  ADD PRIMARY KEY (`id_categorie`);

--
-- Index pour la table `coach`
--
ALTER TABLE `coach`
  ADD PRIMARY KEY (`id_coach`),
  ADD UNIQUE KEY `id_user` (`id_user`),
  ADD KEY `fk_coach_domaine` (`id_domaine`);

--
-- Index pour la table `commande`
--
ALTER TABLE `commande`
  ADD PRIMARY KEY (`id_commande`),
  ADD KEY `fk_commande_user` (`id_user`);

--
-- Index pour la table `commentaire_blog`
--
ALTER TABLE `commentaire_blog`
  ADD PRIMARY KEY (`id_commentaire`),
  ADD KEY `fk_blog_article` (`id_article`),
  ADD KEY `fk_blog_user` (`id_user`);

--
-- Index pour la table `commentaire_evenement`
--
ALTER TABLE `commentaire_evenement`
  ADD PRIMARY KEY (`id_commentaire`),
  ADD KEY `fk_com_event` (`id_evenement`),
  ADD KEY `fk_com_user` (`id_user`);

--
-- Index pour la table `domaine_coaching`
--
ALTER TABLE `domaine_coaching`
  ADD PRIMARY KEY (`id_domaine`),
  ADD UNIQUE KEY `nom_domaine` (`nom_domaine`);

--
-- Index pour la table `evenement`
--
ALTER TABLE `evenement`
  ADD PRIMARY KEY (`id_evenement`),
  ADD KEY `fk_event_user` (`id_organisateur`),
  ADD KEY `fk_event_categorie` (`id_categorie`);

--
-- Index pour la table `inscription_evenement`
--
ALTER TABLE `inscription_evenement`
  ADD PRIMARY KEY (`id_inscription`),
  ADD KEY `fk_ins_event` (`id_evenement`),
  ADD KEY `fk_ins_user` (`id_user`);

--
-- Index pour la table `ligne_commande`
--
ALTER TABLE `ligne_commande`
  ADD PRIMARY KEY (`id_ligne`),
  ADD KEY `fk_ligne_cmd` (`id_commande`),
  ADD KEY `fk_ligne_prod` (`id_produit`);

--
-- Index pour la table `notification`
--
ALTER TABLE `notification`
  ADD PRIMARY KEY (`id_notification`),
  ADD KEY `IDX_BF5476CA6B3CA4B` (`id_user`);

--
-- Index pour la table `produit`
--
ALTER TABLE `produit`
  ADD PRIMARY KEY (`id_produit`);

--
-- Index pour la table `rating`
--
ALTER TABLE `rating`
  ADD PRIMARY KEY (`id_rating`),
  ADD UNIQUE KEY `UNIQ_D88926226B3CA4B_D1DC2CFC` (`id_user`,`id_coach`),
  ADD KEY `IDX_D88926226B3CA4B` (`id_user`),
  ADD KEY `IDX_D8892622D1DC2CFC` (`id_coach`);

--
-- Index pour la table `reservation`
--
ALTER TABLE `reservation`
  ADD PRIMARY KEY (`id_reservation`),
  ADD KEY `IDX_42C849556B3CA4B` (`id_user`),
  ADD KEY `IDX_42C84955D1DC2CFC` (`id_coach`);

--
-- Index pour la table `role`
--
ALTER TABLE `role`
  ADD PRIMARY KEY (`id_role`),
  ADD UNIQUE KEY `nom_role` (`nom_role`);

--
-- Index pour la table `sponsor`
--
ALTER TABLE `sponsor`
  ADD PRIMARY KEY (`id_sponsor`);

--
-- Index pour la table `sponsoring`
--
ALTER TABLE `sponsoring`
  ADD PRIMARY KEY (`id_evenement`,`id_sponsor`),
  ADD KEY `fk_spons_sponsor` (`id_sponsor`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `fk_user_role` (`id_role`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `article`
--
ALTER TABLE `article`
  MODIFY `id_article` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `categorie_evenement`
--
ALTER TABLE `categorie_evenement`
  MODIFY `id_categorie` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `coach`
--
ALTER TABLE `coach`
  MODIFY `id_coach` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT pour la table `commande`
--
ALTER TABLE `commande`
  MODIFY `id_commande` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `commentaire_blog`
--
ALTER TABLE `commentaire_blog`
  MODIFY `id_commentaire` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `commentaire_evenement`
--
ALTER TABLE `commentaire_evenement`
  MODIFY `id_commentaire` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `domaine_coaching`
--
ALTER TABLE `domaine_coaching`
  MODIFY `id_domaine` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `evenement`
--
ALTER TABLE `evenement`
  MODIFY `id_evenement` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `inscription_evenement`
--
ALTER TABLE `inscription_evenement`
  MODIFY `id_inscription` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `ligne_commande`
--
ALTER TABLE `ligne_commande`
  MODIFY `id_ligne` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `notification`
--
ALTER TABLE `notification`
  MODIFY `id_notification` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `produit`
--
ALTER TABLE `produit`
  MODIFY `id_produit` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT pour la table `rating`
--
ALTER TABLE `rating`
  MODIFY `id_rating` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `reservation`
--
ALTER TABLE `reservation`
  MODIFY `id_reservation` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `role`
--
ALTER TABLE `role`
  MODIFY `id_role` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `sponsor`
--
ALTER TABLE `sponsor`
  MODIFY `id_sponsor` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `coach`
--
ALTER TABLE `coach`
  ADD CONSTRAINT `fk_coach_domaine` FOREIGN KEY (`id_domaine`) REFERENCES `domaine_coaching` (`id_domaine`),
  ADD CONSTRAINT `fk_coach_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`);

--
-- Contraintes pour la table `commande`
--
ALTER TABLE `commande`
  ADD CONSTRAINT `fk_commande_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`) ON DELETE CASCADE;

--
-- Contraintes pour la table `commentaire_blog`
--
ALTER TABLE `commentaire_blog`
  ADD CONSTRAINT `fk_blog_article` FOREIGN KEY (`id_article`) REFERENCES `article` (`id_article`),
  ADD CONSTRAINT `fk_blog_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`) ON DELETE CASCADE;

--
-- Contraintes pour la table `commentaire_evenement`
--
ALTER TABLE `commentaire_evenement`
  ADD CONSTRAINT `fk_com_event` FOREIGN KEY (`id_evenement`) REFERENCES `evenement` (`id_evenement`),
  ADD CONSTRAINT `fk_com_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`) ON DELETE CASCADE;

--
-- Contraintes pour la table `evenement`
--
ALTER TABLE `evenement`
  ADD CONSTRAINT `fk_event_categorie` FOREIGN KEY (`id_categorie`) REFERENCES `categorie_evenement` (`id_categorie`),
  ADD CONSTRAINT `fk_event_user` FOREIGN KEY (`id_organisateur`) REFERENCES `users` (`id_user`);

--
-- Contraintes pour la table `inscription_evenement`
--
ALTER TABLE `inscription_evenement`
  ADD CONSTRAINT `fk_ins_event` FOREIGN KEY (`id_evenement`) REFERENCES `evenement` (`id_evenement`),
  ADD CONSTRAINT `fk_ins_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`) ON DELETE CASCADE;

--
-- Contraintes pour la table `ligne_commande`
--
ALTER TABLE `ligne_commande`
  ADD CONSTRAINT `fk_ligne_cmd` FOREIGN KEY (`id_commande`) REFERENCES `commande` (`id_commande`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_ligne_prod` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`);

--
-- Contraintes pour la table `notification`
--
ALTER TABLE `notification`
  ADD CONSTRAINT `FK_BF5476CA6B3CA4B` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`) ON DELETE CASCADE;

--
-- Contraintes pour la table `rating`
--
ALTER TABLE `rating`
  ADD CONSTRAINT `FK_D88926226B3CA4B` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_D8892622D1DC2CFC` FOREIGN KEY (`id_coach`) REFERENCES `coach` (`id_coach`) ON DELETE CASCADE;

--
-- Contraintes pour la table `reservation`
--
ALTER TABLE `reservation`
  ADD CONSTRAINT `FK_42C849556B3CA4B` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  ADD CONSTRAINT `FK_42C84955D1DC2CFC` FOREIGN KEY (`id_coach`) REFERENCES `coach` (`id_coach`);

--
-- Contraintes pour la table `sponsoring`
--
ALTER TABLE `sponsoring`
  ADD CONSTRAINT `fk_spons_event` FOREIGN KEY (`id_evenement`) REFERENCES `evenement` (`id_evenement`),
  ADD CONSTRAINT `fk_spons_sponsor` FOREIGN KEY (`id_sponsor`) REFERENCES `sponsor` (`id_sponsor`);

--
-- Contraintes pour la table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `fk_user_role` FOREIGN KEY (`id_role`) REFERENCES `role` (`id_role`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
