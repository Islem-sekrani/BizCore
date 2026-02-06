# 🎯 Admin Panel - Gestion Platform

Application JavaFX moderne pour la gestion multi-modules avec une interface élégante et professionnelle.

## 📋 Fonctionnalités

### 5 Modules de Gestion :
1. **👤 GESTION UTILISATEURS** - Gérer les utilisateurs, profils et permissions
2. **📅 GESTION ÉVÉNEMENTS** - Planifier et organiser des événements
3. **🎯 GESTION COACHING** - Suivre les sessions de coaching
4. **📝 GESTION BLOG** - Créer et gérer les articles de blog
5. **🛍 GESTION PRODUITS/COMMANDES** - Gérer les produits et commandes

### Fonctionnalités Principales :
- ✅ Interface moderne et responsive
- ✅ Tableaux de données dynamiques
- ✅ Boutons Ajouter, Modifier, Supprimer pour chaque module
- ✅ Recherche et filtrage en temps réel
- ✅ Pagination personnalisable
- ✅ Actions rapides (Voir, Éditer, Supprimer) sur chaque ligne
- ✅ Export de données
- ✅ Design cohérent avec palette de couleurs personnalisée

## 🎨 Palette de Couleurs

- **Vert Principal** : `#17BB9C`
- **Bleu Nuit** : `#1a2332`
- **Noir** : `#000000`
- **Blanc** : `#FFFFFF`

## 🚀 Installation et Exécution

### Prérequis
- Java JDK 17 ou supérieur
- Maven 3.6 ou supérieur
- JavaFX SDK (inclus via Maven)

### Option 1 : Avec Maven
```bash
# Compiler le projet
mvn clean compile

# Exécuter l'application
mvn javafx:run

# Créer un JAR exécutable
mvn clean package
```

### Option 2 : Avec IDE (IntelliJ IDEA / Eclipse)
1. Ouvrir le projet dans votre IDE
2. Importer en tant que projet Maven
3. Laisser Maven télécharger les dépendances
4. Exécuter la classe `Main.java`

### Option 3 : Avec JAR
```bash
# Après avoir créé le JAR avec Maven
java --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml -jar target/GestionPlatform-1.0.0.jar
```

## 📁 Structure du Projet

```
GestionPlatform/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── module-info.java
│   │   │   └── com/gestion/
│   │   │       ├── Main.java
│   │   │       ├── controllers/
│   │   │       │   ├── MainDashboardController.java
│   │   │       │   └── ModuleViewController.java
│   │   │       ├── models/
│   │   │       │   ├── User.java
│   │   │       │   ├── Event.java
│   │   │       │   ├── Coaching.java
│   │   │       │   ├── Blog.java
│   │   │       │   └── Product.java
│   │   │       └── utils/
│   │   └── resources/
│   │       ├── fxml/
│   │       │   ├── MainDashboard.fxml
│   │       │   └── ModuleView.fxml
│   │       └── css/
│   │           └── styles.css
│   └── test/
├── pom.xml
└── README.md
```

## 🎯 Utilisation

### Navigation
- Cliquez sur les boutons du menu latéral pour accéder aux différents modules
- Le module actif est mis en évidence avec la couleur primaire (#17BB9C)

### Gestion des Données
- **Ajouter** : Cliquez sur "ADD NEW" pour créer un nouvel élément
- **Rechercher** : Utilisez la barre de recherche pour filtrer les données
- **Modifier** : Cliquez sur l'icône ✎ (crayon) dans la colonne Actions
- **Voir** : Cliquez sur l'icône 👁 (œil) pour voir les détails
- **Supprimer** : Cliquez sur l'icône 🗑 (poubelle) pour supprimer
- **Export** : Cliquez sur "EXPORT" pour exporter les données

### Pagination
- Ajustez le nombre de résultats par page avec le menu déroulant
- Naviguez entre les pages avec les boutons de pagination en bas

## 🛠 Configuration

### Personnalisation des Couleurs
Les couleurs peuvent être modifiées dans le fichier `styles.css` :
```css
* {
    -fx-primary-color: #17BB9C;      /* Couleur principale */
    -fx-dark-navy: #1a2332;          /* Couleur sidebar */
}
```

### Ajout de Nouveaux Modules
1. Ajouter un nouveau modèle dans `models/`
2. Ajouter les colonnes correspondantes dans `ModuleViewController`
3. Ajouter le bouton dans `MainDashboard.fxml`
4. Implémenter le handler dans `MainDashboardController`

## 📦 Dépendances

- **JavaFX Controls** : 21.0.1
- **JavaFX FXML** : 21.0.1
- **JavaFX Graphics** : 21.0.1

## 🐛 Débogage

Si vous rencontrez des problèmes :

1. **Erreur de module** : Vérifiez que `module-info.java` est correctement configuré
2. **FXML non trouvé** : Vérifiez les chemins dans les contrôleurs
3. **CSS non appliqué** : Vérifiez le chemin du stylesheet dans `Main.java`

## 📝 Notes de Développement

- Le projet utilise JavaFX 21 et Java 17
- Les données sont actuellement stockées en mémoire (ObservableList)
- Prêt pour l'intégration avec une base de données (MySQL, PostgreSQL, etc.)
- Architecture MVC respectée pour une maintenabilité optimale

## 🔄 Améliorations Futures

- [ ] Connexion à une base de données
- [ ] Authentification et autorisation
- [ ] Génération de rapports PDF
- [ ] Import de données depuis Excel/CSV
- [ ] Notifications en temps réel
- [ ] Mode sombre / clair
- [ ] Graphiques et statistiques

## 📄 Licence

Ce projet est fourni à des fins éducatives et de démonstration.

## 👨‍💻 Auteur

Développé avec ❤️ en JavaFX

---

**Version** : 1.0.0  
**Date** : Février 2024
