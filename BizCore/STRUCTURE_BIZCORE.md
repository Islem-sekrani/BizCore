# Structure de l'Application BizCore

## 📋 Vue d'ensemble

BizCore est une plateforme complète de vente et gestion de services digitaux comprenant:
- **Espace Client**: Navigation publique pour découvrir et acheter des services
- **Espace Administration**: Tableau de bord avec gestion CRUD complète

---

## 🎨 Palette de Couleurs

- **#1A2332** - Navy (Couleur principale)
- **#17BB9C** - Turquoise (Accent)
- **#000000** - Noir
- **#333333, #666666, #999999** - Gris (foncé, moyen, clair)
- **#F5F5F5** - Gris de fond
- **#FFFFFF** - Blanc

---

## 📁 Structure des Pages

### 1. **Page d'Accueil** (`HomeView.fxml`)
**Objectif**: Présenter BizCore et orienter les utilisateurs

**Contenu**:
- ✅ Logo "BizCore" en grand
- ✅ Titre: "Plateforme de Vente et Gestion de Services Digitaux"
- ✅ Texte introductif court expliquant la plateforme
- ✅ Description des 5 catégories:
  - 📚 Formations en ligne
  - 💻 Logiciels sur mesure
  - 📱 Applications mobiles
  - 🌐 Sites web
  - ⚙️ Services IT
- ✅ 2 boutons d'action principaux:
  - **"Découvrir les offres"** → Redirige vers OffersView
  - **"Déposer un service"** → Redirige vers SellerFormView

**Contrôleur**: `HomeController.java`
- `handleDiscover()` → `mainController.showOffers()`
- `handleSubmit()` → `mainController.showSellerForm()`

---

### 2. **Page des Offres** (`OffersView.fxml`)
**Objectif**: Afficher UNIQUEMENT la liste des services disponibles (SANS formulaire)

**Contenu**:
- ✅ Titre: "Nos Meilleures Offres"
- ✅ Sous-titre descriptif
- ✅ Grille de cartes de produits (`FlowPane`)
- ✅ Chaque carte affiche:
  - Image ou placeholder
  - Titre du service
  - Description courte
  - Prix
  - Badge de catégorie

**Contrôleur**: `OffersController.java`
- Charge dynamiquement les produits depuis `DataStore`
- Utilise `ProductCard.fxml` pour chaque produit

---

### 3. **Page Formulaire Vendeur** (`SellerFormView.fxml`)
**Objectif**: Permettre aux utilisateurs de publier leurs services

**Contenu**:
- ✅ Formulaire en 2 colonnes
- ✅ Champs:
  - Type de Service (ComboBox)
  - Titre de l'offre
  - Prix (TND)
  - URL Image
  - Description détaillée (TextArea)
- ✅ Bouton "Publier l'Annonce"
- ✅ Design épuré sans icônes excessives

**Contrôleur**: `SellerFormController.java`
- `handlePublish()` → Ajoute le produit au DataStore

---

### 4. **Tableau de Bord Administration** (`AdminView.fxml`)
**Objectif**: Gérer tous les modules avec CRUD complet

**Structure**:
```
┌─────────────────────────────────────┐
│         HEADER (Logo + User)        │
├──────────┬──────────────────────────┤
│          │                          │
│ SIDEBAR  │    CONTENT AREA          │
│          │                          │
│ - Users  │  Tables + Forms          │
│ - Events │  CRUD Operations         │
│ - Coach  │                          │
│ - Blog   │                          │
│ - Prodts │                          │
│          │                          │
└──────────┴──────────────────────────┘
```

**Modules à implémenter**:

#### 4.1 **Gestion des Utilisateurs et Rôles**
- Table: Liste des utilisateurs
- Colonnes: ID, Nom, Email, Rôle, Statut
- Actions: Ajouter, Modifier, Supprimer
- Formulaire: Nom, Email, Mot de passe, Rôle (Admin/User/Seller)

#### 4.2 **Gestion des Événements**
- Table: Liste des événements
- Colonnes: ID, Titre, Date, Lieu, Participants
- Actions: Ajouter, Modifier, Supprimer
- Formulaire: Titre, Description, Date, Heure, Lieu, Capacité

#### 4.3 **Gestion du Coaching**
- Table: Sessions de coaching
- Colonnes: ID, Coach, Client, Date, Statut
- Actions: Ajouter, Modifier, Supprimer
- Formulaire: Coach, Client, Date, Durée, Type, Notes

#### 4.4 **Gestion du Blog**
- Table: Articles de blog
- Colonnes: ID, Titre, Auteur, Date, Statut
- Actions: Ajouter, Modifier, Supprimer
- Formulaire: Titre, Contenu, Auteur, Catégorie, Image

#### 4.5 **Gestion des Produits et Commandes**
- Table: Produits et commandes
- Colonnes: ID, Produit, Client, Prix, Statut
- Actions: Ajouter, Modifier, Supprimer
- Formulaire: Nom produit, Prix, Description, Stock

---

## 🎯 Navigation

### Espace Client
```
HomeView
  ├─ "Découvrir les offres" → OffersView
  └─ "Déposer un service" → SellerFormView
```

### Espace Administration
```
AdminView (Dashboard)
  ├─ Sidebar Navigation
  │   ├─ Utilisateurs → GenericTable (Users)
  │   ├─ Événements → GenericTable (Events)
  │   ├─ Coaching → GenericTable (Coaching)
  │   ├─ Blog → GenericTable (Blog)
  │   └─ Produits → GenericTable (Products)
  └─ Content Area (Tables + Forms CRUD)
```

---

## 📦 Composants Réutilisables

### `ProductCard.fxml`
- Carte de produit avec image, titre, prix, description
- Placeholder intelligent selon le type
- Badge de catégorie dynamique

### `GenericTable.fxml`
- Table générique pour tous les modules admin
- Colonnes configurables
- Boutons d'action (Add, Edit, Delete)

---

## 🔧 Contrôleurs Principaux

1. **MainController.java** - Navigation principale
2. **HomeController.java** - Page d'accueil
3. **OffersController.java** - Liste des offres
4. **SellerFormController.java** - Formulaire vendeur
5. **AdminController.java** - Dashboard admin
6. **GenericTableController.java** - Tables CRUD
7. **ProductCardController.java** - Cartes produits

---

## ✅ État Actuel

### Complété:
- ✅ Page d'accueil avec présentation BizCore
- ✅ Page des offres (liste uniquement)
- ✅ Formulaire vendeur (page séparée)
- ✅ Cartes de produits avec placeholders
- ✅ Design épuré avec palette de couleurs

### À Compléter:
- ⏳ Tableau de bord administration avec sidebar
- ⏳ 5 modules CRUD (Users, Events, Coaching, Blog, Products)
- ⏳ Formulaires pour chaque module
- ⏳ Connexion à la base de données MySQL

---

## 🎨 Principes de Design

1. **Minimaliste** - Pas d'éléments décoratifs excessifs
2. **Professionnel** - Couleurs sobres et cohérentes
3. **Responsive** - Adaptation à différentes tailles d'écran
4. **Intuitif** - Navigation claire et logique
5. **Moderne** - Ombres subtiles, bordures arrondies (6-8px)

---

## 📝 Notes Techniques

- Framework: **JavaFX 17**
- Build: **Maven**
- Base de données: **MySQL** (bizcore)
- Stockage temporaire: **DataStore** (singleton)
- CSS: **style.css** centralisé
