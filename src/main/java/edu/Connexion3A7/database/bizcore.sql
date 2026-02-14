-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : lun. 09 fév. 2026 à 22:30
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

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

-- --------------------------------------------------------

--
-- Structure de la table `categorie_evenement`
--

CREATE TABLE `categorie_evenement` (
  `id_categorie` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `coach`
--

CREATE TABLE `coach` (
  `id_coach` int(11) NOT NULL,
  `id_user` int(11) DEFAULT NULL,
  `id_domaine` int(11) DEFAULT NULL,
  `nom` varchar(20) NOT NULL,
  `prenom` varchar(20) NOT NULL,
  `biographie` text DEFAULT NULL,
  `experience_annees` int(11) DEFAULT NULL,
  `tarif_horaire` decimal(10,2) DEFAULT NULL,
  `disponibilite` varchar(20) DEFAULT NULL,
  `certification` varchar(500) DEFAULT NULL,
  `note_moyenne` decimal(3,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

-- --------------------------------------------------------

--
-- Structure de la table `domaine_coaching`
--

CREATE TABLE `domaine_coaching` (
  `id_domaine` int(11) NOT NULL,
  `nom_domaine` varchar(100) NOT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

-- --------------------------------------------------------

--
-- Structure de la table `inscription_evenement`
--

CREATE TABLE `inscription_evenement` (
  `id_inscription` int(11) NOT NULL,
  `id_evenement` int(11) DEFAULT NULL,
  `id_user` int(11) DEFAULT NULL,
  `date_inscription` timestamp NOT NULL DEFAULT current_timestamp(),
  `statut` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
-- Index pour la table `produit`
--
ALTER TABLE `produit`
  ADD PRIMARY KEY (`id_produit`);

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
  MODIFY `id_article` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `categorie_evenement`
--
ALTER TABLE `categorie_evenement`
  MODIFY `id_categorie` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `coach`
--
ALTER TABLE `coach`
  MODIFY `id_coach` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `commande`
--
ALTER TABLE `commande`
  MODIFY `id_commande` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `commentaire_blog`
--
ALTER TABLE `commentaire_blog`
  MODIFY `id_commentaire` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `commentaire_evenement`
--
ALTER TABLE `commentaire_evenement`
  MODIFY `id_commentaire` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `domaine_coaching`
--
ALTER TABLE `domaine_coaching`
  MODIFY `id_domaine` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `evenement`
--
ALTER TABLE `evenement`
  MODIFY `id_evenement` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `inscription_evenement`
--
ALTER TABLE `inscription_evenement`
  MODIFY `id_inscription` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `ligne_commande`
--
ALTER TABLE `ligne_commande`
  MODIFY `id_ligne` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `produit`
--
ALTER TABLE `produit`
  MODIFY `id_produit` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `role`
--
ALTER TABLE `role`
  MODIFY `id_role` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `sponsor`
--
ALTER TABLE `sponsor`
  MODIFY `id_sponsor` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT;

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
  ADD CONSTRAINT `fk_commande_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`);

--
-- Contraintes pour la table `commentaire_blog`
--
ALTER TABLE `commentaire_blog`
  ADD CONSTRAINT `fk_blog_article` FOREIGN KEY (`id_article`) REFERENCES `article` (`id_article`),
  ADD CONSTRAINT `fk_blog_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`);

--
-- Contraintes pour la table `commentaire_evenement`
--
ALTER TABLE `commentaire_evenement`
  ADD CONSTRAINT `fk_com_event` FOREIGN KEY (`id_evenement`) REFERENCES `evenement` (`id_evenement`),
  ADD CONSTRAINT `fk_com_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`);

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
  ADD CONSTRAINT `fk_ins_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`);

--
-- Contraintes pour la table `ligne_commande`
--
ALTER TABLE `ligne_commande`
  ADD CONSTRAINT `fk_ligne_cmd` FOREIGN KEY (`id_commande`) REFERENCES `commande` (`id_commande`),
  ADD CONSTRAINT `fk_ligne_prod` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`);

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
