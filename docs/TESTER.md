# Vérifier le TP avant le dépôt GitHub

Les commandes ci-dessous sont à lancer vous-même depuis la racine du projet. Aucun build, test, démarrage du serveur ou script SQL n'a été lancé lors de la préparation des fichiers. Les résultats indiqués sont attendus, et non observés.

## 1. Vérifier les outils

```powershell
java -version
mvn -version
```

Vérifier que Maven et Tomcat utilisent le JDK 11.

## 2. Lancer les tests isolés

```powershell
mvn clean test
```

Attendu : BUILD SUCCESS, 5 tests, aucune erreur ni échec. Consulter `target/surefire-reports`. Chaque test utilise une base H2 en mémoire indépendante, supprimée à la fermeture ; MySQL n'est pas utilisé.

Couverture : CRUD, association produit/catégorie après fermeture du contexte, recherches, pagination, validations, référence unique, rollback, héritage JOINED, orphanRemoval, clé composée et rapport mensuel (bornes des dates, statuts exclus, prix historique, catégorie sans vente).

H2 vérifie la logique et les mappings ; poursuivre avec MySQL pour vérifier le moteur demandé par le TP.

## 3. Préparer MySQL

Démarrer MySQL, puis ouvrir son client :

```powershell
mysql -u root -p
```

Dans le client MySQL, depuis un terminal ouvert à la racine du projet :

```sql
SOURCE sql/01-base.sql;
EXIT;
```

Le compte configuré doit avoir les droits de création et modification des tables sur cette base. Si la commande SOURCE ne trouve pas le fichier, utiliser son chemin absolu avec des barres obliques.

## 4. Construire et déployer

```powershell
mvn clean package
```

Attendu : BUILD SUCCESS et fichier `target/gestion-commerciale.war`. Les tests sont également lancés par cette commande.

Définir DB_URL, DB_USER et DB_PASSWORD comme indiqué dans le README, dans le processus qui lance Tomcat. Copier le WAR dans le dossier `webapps` de Tomcat 9 puis lancer Tomcat avec le JDK 11. Dans IntelliJ, une configuration Tomcat peut déployer le WAR depuis le même dossier.

Ouvrir :
- `http://localhost:8080/gestion-commerciale/`
- `http://localhost:8080/gestion-commerciale/rapports/ventes`

Adapter le port si nécessaire. La première visite du rapport peut présenter un tableau vide tant que la base n'a pas de catégories. Vérifier les journaux Tomcat : la factory doit démarrer et les tables doivent être créées. Aucun servlet n'insère de données automatiquement.

## 5. Ajouter les données de démonstration

Après le premier démarrage, ouvrir à nouveau MySQL sur une base de démonstration vide et lancer une seule fois :

```sql
SOURCE sql/02-demo.sql;
SHOW TABLES;
SELECT * FROM produits;
SELECT * FROM commande_produit;
```

Le script fixe des identifiants pour rendre les relations lisibles. Ne pas le relancer sur des données existantes ; employer une autre base vide pour recommencer.

Actualiser le rapport du mois précédent. Attendu :

| Catégorie | Chiffre d'affaires MAD |
| --- | ---: |
| Informatique | 1350.00 |
| Mobilier | 0.00 |
| Papeterie | 100.00 |
| Total | 1450.00 |

La commande annulée est exclue. Sélectionner le mois courant : seule la vente de papeterie à 25.00 MAD doit compter.

Vérifier le prix historique :

```sql
UPDATE produits SET prix = 999.00 WHERE reference = 'INF-001';
```

Actualiser le mois précédent : le total doit rester à 1450.00 MAD. Rétablir ensuite le prix :

```sql
UPDATE produits SET prix = 450.00 WHERE reference = 'INF-001';
```

Ouvrir `http://localhost:8080/gestion-commerciale/rapports/ventes?mois=invalide` : attendu HTTP 400. Arrêter Tomcat proprement pour fermer la factory.

## 6. Dépannage

| Symptôme | Vérification |
| --- | --- |
| Access denied | Variables DB_USER/DB_PASSWORD du processus Tomcat et droits MySQL |
| Unknown database | Exécuter 01-base.sql |
| Communications link failure | Service MySQL, hôte et port dans DB_URL |
| HTTP 404 | Tomcat 9, contexte gestion-commerciale, déploiement du WAR |
| javax.servlet introuvable au démarrage | Utiliser Tomcat 9 avec ce projet javax |
| Rapport incorrect ou vide | Dates, statuts et données du script 02-demo.sql |
| Échec de suppression d'un produit commandé | Contrainte de référence attendue, utiliser actif=false |

## 7. Préparer GitHub

Placer vos propres images dans `docs/screenshots/01.png` à `05.png`. La partie captures du README contient uniquement les emplacements des photos, sans descriptions. Ajouter ou retirer des liens selon le nombre de captures.

Avant le premier commit, inspecter les fichiers :

```powershell
git init
git status
git add pom.xml src sql docs README.md .gitignore
git diff --cached --stat
git diff --cached
```

Vérifier qu'aucun mot de passe réel, fichier .env, journal, dossier target ou configuration personnelle ne figure dans le contenu indexé. Le dossier initial `src/main/java/com/example`, s'il existe encore, est un exemple du squelette et peut être retiré manuellement avant le commit.

Après vérification :

```powershell
git commit -m "Réalisation du TP6 de gestion commerciale avec JPA"
git branch -M main
git remote add origin https://github.com/VOTRE_COMPTE/VOTRE_DEPOT.git
git push -u origin main
```

Créer auparavant un dépôt GitHub vide. Si origin existe déjà, vérifier `git remote -v` et conserver l'URL voulue au lieu de relancer `git remote add`.

