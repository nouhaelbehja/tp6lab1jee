-- À exécuter après le premier démarrage, une seule fois sur une base de démonstration vide.
USE gestion_commerciale;
START TRANSACTION;
INSERT INTO categories (id, nom, description) VALUES
(1, 'Informatique', 'Accessoires et matériel informatique'),
(2, 'Papeterie', 'Fournitures de bureau'), (3, 'Mobilier', 'Catégorie sans vente');
INSERT INTO produits (id, libelle, reference, prix, stock, actif, date_creation, categorie_id) VALUES
(1, 'Clavier mécanique', 'INF-001', 450.00, 20, TRUE, CURRENT_DATE, 1),
(2, 'Souris sans fil', 'INF-002', 150.00, 35, TRUE, CURRENT_DATE, 1),
(3, 'Carnet A5', 'PAP-001', 25.00, 100, TRUE, CURRENT_DATE, 2);
INSERT INTO users (id, nom, email, telephone) VALUES (1, 'Amal Demo', 'amal@example.test', '0600000000'), (2, 'Atlas Demo', 'atlas@example.test', '0500000000');
INSERT INTO clients (id, date_inscription, adresse_livraison) VALUES (1, CURRENT_DATE, 'Casablanca');
INSERT INTO fournisseurs (id, adresse, raison_sociale) VALUES (2, 'Rabat', 'Atlas Fournitures');
SET @mois_precedent = DATE_SUB(DATE_SUB(CURRENT_DATE, INTERVAL DAYOFMONTH(CURRENT_DATE)-1 DAY), INTERVAL 1 MONTH);
INSERT INTO commandes (id, reference, date_commande, statut, note, client_id) VALUES
(1, 'CMD-DEMO-001', @mois_precedent, 'VALIDEE', 'Commande de démonstration', 1),
(2, 'CMD-DEMO-002', @mois_precedent, 'ANNULEE', 'Exclue du rapport', 1),
(3, 'CMD-DEMO-003', CURRENT_DATE, 'LIVREE', 'Mois courant', 1);
INSERT INTO commande_produit (commande_id, produit_id, quantite, prix_unitaire) VALUES
(1, 1, 2, 450.00), (1, 2, 3, 150.00), (1, 3, 4, 25.00),
(2, 1, 1, 450.00), (3, 3, 1, 25.00);
COMMIT;

