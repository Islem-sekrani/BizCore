# 🏢 BizCore — Plateforme de Gestion d'Entreprise

> Une application de bureau complète développée en **JavaFX**, permettant la gestion intégrée des produits, des utilisateurs, des coachs et bien plus encore.

---

## 📋 Table des Matières

- [Aperçu du Projet](#-aperçu-du-projet)
- [Technologies Utilisées](#-technologies-utilisées)
- [Architecture du Projet](#-architecture-du-projet)
- [Fonctionnalités par Module](#-fonctionnalités-par-module)
  - [🛒 Gestion des Produits — Ma Contribution](#-gestion-des-produits--ma-contribution)
  - [👤 Gestion des Utilisateurs](#-gestion-des-utilisateurs)
  - [🏋️ Gestion des Coachs](#-gestion-des-coachs)
- [Installation & Lancement](#-installation--lancement)
- [Contributeurs](#-contributeurs)

---

## 🌐 Aperçu du Projet

**BizCore** est une application multi-modules développée dans le cadre d'un projet de groupe d'ingénierie logicielle. Elle offre une interface d'administration complète ainsi qu'un espace utilisateur dédié, avec une architecture orientée services et une connexion directe à une base de données MySQL.

L'application distingue deux types de profils :
- 🔴 **Administrateur** — accès complet au tableau de bord de gestion
- 🟢 **Utilisateur** — accès au marketplace et aux fonctionnalités client

---

## 🛠️ Technologies Utilisées

| Technologie | Rôle |
|---|---|
| ☕ **Java 17+** | Langage principal |
| 🖥️ **JavaFX + FXML** | Interface graphique desktop |
| 🗄️ **MySQL** | Base de données relationnelle |
| 🔗 **JDBC** | Connexion et requêtes SQL |
| 🤖 **Hugging Face API** | Classification IA des produits |
| 🌍 **MyMemory API** | Traduction multilingue (FR→EN→AR) |
| 💳 **Stripe API** | Paiement en ligne sécurisé |
| 📱 **Twilio API** | Envoi de SMS de confirmation |
| 📦 **OkHttp** | Client HTTP pour appels API |
| 🏗️ **Maven** | Gestion des dépendances |
| 🎨 **FXML + CSS** | Design des interfaces |

---

## 🏗️ Architecture du Projet

```
BizCore/
├── 📁 src/main/java/edu/Connexion3A7/
│   ├── 🎮 Controller/
│   │   ├── ProductController.java              # Formulaire ajout/modif produit
│   │   ├── ProductViewController.java          # Vue principale produits (admin)
│   │   ├── AdminProductDashboardController.java
│   │   ├── ProductCardController.java          # Carte produit (marketplace)
│   │   └── PaymentController.java              # Interface de paiement
│   ├── 📦 entities/
│   │   └── Product.java                        # Modèle entité Produit
│   ├── 🔧 services/
│   │   ├── ProductService.java                 # CRUD produits (JDBC)
│   │   ├── FrontProductService.java            # Service côté utilisateur
│   │   ├── HuggingFaceService.java             # IA : classification + traduction
│   │   ├── StripePaymentService.java           # Paiement via Stripe API
│   │   ├── OkHttpSmsService.java               # SMS via Twilio API (OkHttp)
│   │   └── SmsService.java                     # Service SMS d'ordre supérieur
│   ├── 🔌 interfaces/
│   │   └── IProductService.java                # Interface de contrat CRUD
│   └── 🛡️ utils/
│       └── ProductValidator.java               # Validation des champs
├── 📁 src/main/resources/
│   └── 🖼️ Controller/
│       ├── ProductView.fxml                    # Interface admin produits
│       ├── PaymentView.fxml                    # Interface de paiement
│       ├── Marketplace.fxml                   # Vue utilisateur
│       └── styles/payment.css                 # Styles dédiés au paiement
└── 📄 pom.xml
```

---

## ✨ Fonctionnalités par Module

---

### 🛒 Gestion des Produits — Ma Contribution

> ⭐ **Module développé intégralement par moi** — de la base de données jusqu'à l'interface utilisateur finale, incluant les APIs de paiement et SMS.

J'ai pris en charge l'ensemble du cycle de développement du module produits, couvrant : modélisation, accès aux données, validation, logique métier, intégration de 4 APIs externes, et interface graphique complète.

---

#### 📐 Modélisation de l'Entité `Product`

L'entité `Product` représente un produit en base de données avec les attributs suivants :

| Champ | Type | Description |
|---|---|---|
| `idProduit` | `int` | Identifiant unique auto-généré |
| `nomProduit` | `String` | Nom du produit |
| `description` | `String` | Description détaillée |
| `prix` | `String` | Prix au format `XX.XX` |
| `stockDisponible` | `int` | Quantité en stock |
| `categorie` | `String` | Catégorie (Formation, Livre, etc.) |
| `imageUrl` | `String` | Chemin vers l'image du produit |
| `statut` | `String` | Statut (Actif, Inactif, etc.) |

---

#### 🔧 Backend — Couche Service & Base de Données

**Fichiers concernés :** `ProductService.java`, `IProductService.java`, `MyConnection.java`

La couche service implémente l'interface `IProductService` et utilise le **pattern Singleton** via `MyConnection` pour partager la connexion JDBC à travers tout le projet.

##### ✅ Opérations CRUD complètes

- ➕ **CREATE** — `addProduct(Product product)`
  - Requête SQL paramétrée `INSERT INTO produit (...) VALUES (?, ?, ?, ?, ?, ?, ?)`
  - `PreparedStatement` avec `RETURN_GENERATED_KEYS` → récupération de l'ID auto-généré
  - Assignation automatique de l'ID au produit nouvellement créé

- 📖 **READ** — `getAllProducts()` & `getProductById(int id)`
  - Récupération de toute la liste via `SELECT * FROM produit`
  - Récupération d'un produit précis par son ID via requête paramétrée
  - Mapping complet du `ResultSet` vers l'objet `Product`

- ✏️ **UPDATE** — `updateProduct(Product product)`
  - Requête `UPDATE produit SET ... WHERE id_produit=?`
  - Mise à jour de tous les champs en une seule requête

- 🗑️ **DELETE** — `deleteProduct(int idProduit)`
  - Suppression **transactionnelle** pour garantir l'intégrité référentielle :
    1. Suppression préalable des lignes de commande liées (`ligne_commande`)
    2. Suppression du produit
    3. **Commit** si succès / **Rollback** automatique en cas d'erreur
  - `setAutoCommit(false)` / `commit()` / `rollback()` gérés manuellement

---

#### 🛡️ Validation des Données — `ProductValidator.java`

Avant toute insertion ou modification, chaque champ passe par une validation rigoureuse côté backend :

| Champ | Règle de validation |
|---|---|
| **Nom** | Commence par une majuscule, reste en minuscules (Regex : `^[A-Z][a-z\s]+$`) |
| **Description** | Obligatoire, minimum **20 caractères** |
| **Prix** | Format `XX.XX` (Regex : `^\d+\.\d+$`), supérieur à 0 |
| **Stock** | Chiffres uniquement, valeur ≥ 0 (Regex : `^\d+$`) |
| **Catégorie** | Sélection obligatoire |
| **Image** | Chemin obligatoire |
| **Statut** | Sélection obligatoire |

- `validateAllFields(...)` regroupe toutes les validations → retourne une liste d'erreurs formatées
- Chaque validation retourne un objet `ValidationResult` avec `isValid` et un message d'erreur lisible

---

#### 🤖 API 1 — Hugging Face Inference API (`HuggingFaceService.java`)

| Détail | Valeur |
|---|---|
| 🌐 URL de base | `https://api-inference.huggingface.co/models/` |
| 🤖 Modèle utilisé | `facebook/bart-large-mnli` |
| 🔐 Auth | `Authorization: Bearer hf_...` |
| 📡 Méthode | `POST` avec body JSON |
| ⏱️ Timeout | 45 secondes |
| 🔁 Retry | Jusqu'à **3 tentatives**, délai de 8s entre chacune |

**Fonctionnement :**
- Le nom du produit est classifié parmi : `Formation`, `Livre`, `Abonnement`, `Logiciel`, `Service`
- L'API retourne des `labels` et `scores` de confiance → catégorie avec le score le plus élevé suggérée
- Gestion des modèles en chargement avec attente dynamique via `estimated_time`

```json
{
  "inputs": "Formation Python avancé",
  "parameters": {
    "candidate_labels": ["Formation", "Livre", "Abonnement", "Logiciel", "Service"]
  }
}
```

---

#### 🌍 API 2 — MyMemory Translation API (`HuggingFaceService.java`)

| Détail | Valeur |
|---|---|
| 🌐 URL | `https://api.mymemory.translated.net/get` |
| 📡 Méthode | `GET` avec paramètres URL |
| 🔐 Auth | Paramètre `de=email` → limite 10k mots/jour |
| ⏱️ Timeout | 10 secondes |
| 📏 Limite | 490 caractères par requête |

**Chaîne de traduction automatique :**
```
Nom FR  ──→ [MyMemory] ──→ Nom EN  ──→ [MyMemory] ──→ Nom AR
Desc FR ──→ [MyMemory] ──→ Desc EN ──→ [MyMemory] ──→ Desc AR
```

**Résultat retourné via `AIResult` :**
```
✅ suggestedCategory   → Catégorie IA suggérée
✅ categoryConfidence  → Score de confiance (0.0 à 1.0)
✅ translatedFR        → Nom original (FR)
✅ translatedEN        → Nom traduit en anglais
✅ translatedAR        → Nom traduit en arabe
✅ translatedDescEN    → Description traduite en AI
✅ translatedDescAR    → Description traduite en arabe
```

---

#### 💳 API 3 — Stripe Payment API (`StripePaymentService.java`)

> Intégrée dans le **frontend** (espace utilisateur) pour permettre le paiement des produits au moment du passage en caisse.

| Détail | Valeur |
|---|---|
| 🌐 URL | `https://api.stripe.com/v1/charges` |
| 🔐 Auth | `Authorization: Basic Base64(sk_test_...  :)` |
| 📡 Méthode | `POST` — `application/x-www-form-urlencoded` |
| 💱 Conversion | TND → EUR (`× 0.30`) avant envoi à Stripe |
| 🔖 Version API | `Stripe-Version: 2023-10-16` |
| ⏱️ Timeout | 15 secondes |

**Cartes de test supportées :**

| Numéro de carte | Réseau | Résultat |
|---|---|---|
| `4242 4242 4242 4242` | VISA | ✅ Acceptée |
| `5555 5555 5555 4444` | Mastercard | ✅ Acceptée |
| `3782 822463 10005` | Amex | ✅ Acceptée |
| `4000 0000 0000 0002` | VISA | ❌ Carte refusée |
| `4000 0000 0000 9995` | VISA | ❌ Fonds insuffisants |
| `4000 0000 0000 0069` | VISA | ❌ Carte expirée |
| `4000 0000 0000 0127` | VISA | ❌ CVV incorrect |

**Flux de paiement :**
1. 🔍 Détection automatique du réseau (VISA / Mastercard / Amex / Discover) via le préfixe du numéro
2. 🔄 Résolution du token de test Stripe (`tok_visa`, `tok_mastercard`, etc.)
3. 📤 Envoi de la charge à l'API Stripe (`/charges`)
4. ✅ Si `"paid": true` → création de la commande en base + envoi SMS
5. ❌ Si erreur → message d'erreur précis affiché à l'utilisateur

**Résultat retourné via `PaymentResult` :**
```
✅ success         → Paiement réussi ou non
✅ message         → Message de confirmation ou d'erreur
✅ transactionId   → Identifiant unique de la transaction Stripe
```

---

#### 📱 API 4 — Twilio SMS API (`OkHttpSmsService.java`)

> Envoi automatique d'un **SMS de confirmation** après chaque paiement réussi, ainsi qu'aux coachs lors d'une réservation de séance.

| Détail | Valeur |
|---|---|
| 🌐 URL | `https://api.twilio.com/2010-04-01/Accounts/{SID}/Messages.json` |
| 🔐 Auth | `Authorization: Basic Credentials(accountSid, authToken)` |
| 📡 Méthode | `POST` via **OkHttp** (client HTTP asynchrone) |
| 📏 Limite | 160 caractères par SMS (1 segment) |
| 🔧 Config | Chargée depuis `config.properties` (accountSid, authToken, fromNumber) |
| 🧪 Mode test | `sms.test.mode=true` → simulation sans appel réel à Twilio |

**Normalisation automatique des numéros tunisiens :**
```
58410216      →  +21658410216   (local 8 chiffres → E.164)
21658410216   →  +21658410216   (manque le +)
+21658410216  →  +21658410216   (déjà correct)
```

**Envoi asynchrone (non-bloquant) :**
- Utilise `OkHttpClient.newCall().enqueue(Callback)` → n'interrompt pas l'interface
- Callback `onSuccess` / `onFailure` géré en arrière-plan

**Contextes d'envoi :**

| Déclencheur | Destinataire | Contenu du SMS |
|---|---|---|
| ✅ Paiement confirmé | Client | N° commande, montant, ref transaction |
| 📅 Réservation coach | Coach | Nom du client, date de la séance |

**Singleton** → `OkHttpSmsService.getInstance()` — les credentials ne sont chargés qu'une seule fois par session.

---

#### 🎨 Frontend — Interface Utilisateur JavaFX

**Fichiers concernés :** `ProductViewController.java`, `ProductController.java`, `PaymentController.java`, `ProductView.fxml`, `PaymentView.fxml`

##### 🖥️ Vue Admin (Tableau de bord)
- 📋 **TableView** responsive : Nom, Prix, Stock, Catégorie, Statut, Image
- 🔍 Chargement dynamique au démarrage via `ProductService.getAllProducts()`
- ✏️ Bouton **Modifier** par ligne → dialog pré-rempli avec les données du produit
- 🗑️ Bouton **Supprimer** par ligne → suppression transactionnelle sécurisée
- ➕ Bouton **Ajouter** → formulaire modal de création

##### 📝 Formulaire Ajout / Modification (Dialog Modal)
- Champs : Nom, Description, Prix, Stock, Catégorie (ComboBox), Statut (ComboBox), Image
- 🖼️ **Upload d'image** via `FileChooser` → prévisualisation en temps réel
- ✅ Validation complète par `ProductValidator` avant toute soumission
- 🤖 Bouton **"Analyser avec IA"** → appel asynchrone à `HuggingFaceService` :
  - Catégorie suggérée automatiquement
  - Nom et description traduits EN + AR
  - Barre de progression pendant le traitement

##### 💳 Interface de Paiement (`PaymentController.java` + `PaymentView.fxml`)
- **3 méthodes de paiement** disponibles : Carte bancaire · PayPal · Apple Pay
- **Formulaire carte interactif** avec :
  - 🎴 Prévisualisation de la carte en temps réel (numéro, titulaire, expiration)
  - 🔍 Détection automatique du réseau (VISA / Mastercard / AMEX / Discover)
  - ✍️ Formatage automatique `XXXX XXXX XXXX XXXX` et expiration `MM/AA`
  - ✅ Validation des champs avant soumission (numéro 13-19 chiffres, CVV 3-4 chiffres)
- ⏳ **Barre de progression** pendant le traitement du paiement
- 🧵 Paiement exécuté dans un **thread séparé** → interface non bloquée
- ✅ **Après paiement réussi** :
  1. Création de la commande en base de données
  2. Insertion des lignes de commande (`LigneCommandeService`)
  3. Envoi d'un **SMS de confirmation** via `SmsService` si numéro renseigné
  4. Affichage d'un dialog de succès avec numéro de commande et référence transaction
  5. Vidage automatique du panier (`Cart.getInstance().clearCart()`)

##### 🛍️ Vue Marketplace (Espace Utilisateur)
- Affichage des produits sous forme de **cartes** (`ProductCardController`)
- Bouton "Ajouter au panier" → gestion du panier (`Cart` singleton)
- Interface épurée et responsive pour l'expérience client

---

### 👤 Gestion des Utilisateurs

- 🔐 Authentification avec redirection par rôle (Admin → Dashboard / User → Marketplace)
- 👥 Liste et gestion des comptes utilisateurs
- 🛍️ Marketplace dédié à l'espace utilisateur

---

### 🏋️ Gestion des Coachs

- 📋 Affichage de la liste des coachs enregistrés
- ➕ Formulaire d'ajout d'un nouveau profil coach
- ✏️ Modification et suppression des profils existants
- 📱 Envoi automatique d'un SMS au coach lors d'une nouvelle réservation
- 🔗 Intégration dans le tableau de bord administrateur

---

## 🚀 Installation & Lancement

### Prérequis

- ✅ Java JDK **17 ou supérieur**
- ✅ **Maven** installé
- ✅ **MySQL** en cours d'exécution
- ✅ Clé API **Hugging Face** (classification + traduction)
- ✅ Compte **Stripe** (paiement, mode test disponible)
- ✅ Compte **Twilio** (SMS, mode test disponible)

### Étapes

```bash
# 1. Cloner le dépôt
git clone https://github.com/votre-utilisateur/BizCore.git

# 2. Importer le projet dans IntelliJ IDEA ou Eclipse

# 3. Configurer la base de données
# → Créer une base de données MySQL
# → Importer le fichier SQL fourni (schema.sql)

# 4. Configurer MyConnection.java
# → Mettre à jour host, user, password

# 5. Configurer config.properties
# → twilio.accountSid = ACxxxx...
# → twilio.authToken  = your_token
# → twilio.fromNumber = +1XXXXXXXXXX
# → sms.test.mode     = true  (pour simuler sans envoyer de vrais SMS)

# 6. Lancer l'application
mvn javafx:run
```

---

## 👥 Contributeurs

| Nom | Module(s) développé(s) |
|-----|------------------------|
| **[Votre Nom]** | 🛒 Produits · 💳 Paiement Stripe · 📱 SMS Twilio · 🤖 IA |
| **[Coéquipier 2]** | 👤 Gestion des Utilisateurs |
| **[Coéquipier 3]** | 🏋️ Gestion des Coachs |
| **[Coéquipier 4]** | 🖥️ Dashboard & Navigation |

---

## 📄 Licence

Ce projet est développé dans un cadre académique. Tous droits réservés © 2026.

---

<div align="center">

**Fait avec ❤️ dans le cadre d'un projet d'ingénierie logicielle**

</div>
