package org.example;

import org.example.dao.GenericDAO;
import org.example.entities.*;
import org.example.service.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

public class PersistenceTest {
    private SessionFactory factory;
    private CrudService<Categorie, Long> categories;
    private ProduitService produits;

    @Before public void init() {
        factory = new Configuration().configure()
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL")
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "false")
                .buildSessionFactory();
        categories = new CrudService<>(Categorie.class, factory);
        produits = new ProduitService(factory);
    }
    @After public void close() { if (factory != null) factory.close(); }
    private Produit produit(String reference, String prix, Categorie cat) {
        Produit p = new Produit("Clavier " + reference, reference, new BigDecimal(prix), cat);
        p.setStock(10); produits.create(p); return p;
    }
    @Test public void crudRecherchesEtPagination() {
        Categorie cat = new Categorie("Informatique"); cat.setDescription("Matériel"); categories.create(cat);
        Produit p = produit("INF-1", "50.00", cat);
        produit("INF-2", "100.00", cat);
        assertNotNull(p.getId());
        assertEquals("Informatique", produits.getById(p.getId()).getCategorie().getNom());
        assertEquals(2, produits.findByCategorie(cat.getId()).size());
        assertEquals(2, produits.searchByLibelle("CLAVIER").size());
        assertEquals(1, produits.search("clavier", new BigDecimal("40"), new BigDecimal("60")).size());
        assertEquals(1, produits.findPage(1, 1).size());
        assertTrue(produits.findPage(3, 1).isEmpty());
        p.setStock(8); produits.update(p); assertEquals(8, produits.getById(p.getId()).getStock());
        produits.delete(p); assertNull(produits.getById(p.getId()));
    }
    private void commande(Produit produit, String reference, LocalDate date, StatutCommande statut, int quantite) {
        Commande c = new Commande(); c.setReference(reference); c.setDateCommande(date); c.setStatut(statut);
        c.addProduit(produit, quantite);
        new CrudService<Commande, Long>(Commande.class, factory).create(c);
        assertNotNull(c.getId());
    }
    @Test public void rapportRespecteDatesStatutsEtPrixHistorique() {
        Categorie cat = new Categorie("Informatique"); categories.create(cat);
        categories.create(new Categorie("Sans vente"));
        Produit p = produit("INF-1", "50.00", cat);
        YearMonth mois = YearMonth.of(2026, 10);
        commande(p, "DEBUT", mois.atDay(1), StatutCommande.VALIDEE, 2);
        commande(p, "FIN", mois.atEndOfMonth(), StatutCommande.LIVREE, 1);
        commande(p, "AVANT", mois.atDay(1).minusDays(1), StatutCommande.VALIDEE, 10);
        commande(p, "APRES", mois.plusMonths(1).atDay(1), StatutCommande.VALIDEE, 10);
        commande(p, "ANNULEE", mois.atDay(5), StatutCommande.ANNULEE, 10);
        commande(p, "BROUILLON", mois.atDay(5), StatutCommande.BROUILLON, 10);
        p.setPrix(new BigDecimal("200.00")); produits.update(p);
        List<VenteCategorie> rapport = new RapportVentesService(factory).pourMois(mois);
        assertEquals(2, rapport.size());
        assertEquals(0, new BigDecimal("150.00").compareTo(rapport.get(0).getChiffreAffaires()));
        assertEquals(0, BigDecimal.ZERO.compareTo(rapport.get(1).getChiffreAffaires()));
    }
    @Test public void transactionAnnuleToutesLesOperations() {
        GenericDAO<Categorie, Long> dao = new GenericDAO<>(Categorie.class, factory);
        try {
            dao.transaction(em -> { em.persist(new Categorie("Annulée")); em.flush(); throw new IllegalStateException("Erreur simulée"); });
            fail("Exception attendue");
        } catch (IllegalStateException expected) {
            assertEquals("Erreur simulée", expected.getMessage());
        }
        assertTrue(categories.getAll().isEmpty());
    }
    @Test public void heritageJoinedEtSuppressionOrpheline() {
        Client client = new Client(); client.setNom("Amal"); client.setEmail("amal@example.test");
        new CrudService<Client, Long>(Client.class, factory).create(client);
        Fournisseur fournisseur = new Fournisseur(); fournisseur.setNom("Atlas"); fournisseur.setEmail("atlas@example.test");
        new CrudService<Fournisseur, Long>(Fournisseur.class, factory).create(fournisseur);
        List<User> users = new CrudService<User, Long>(User.class, factory).getAll();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u instanceof Client));
        assertTrue(users.stream().anyMatch(u -> u instanceof Fournisseur));
        Categorie cat = new Categorie("Papeterie"); categories.create(cat);
        Produit p = produit("PAP-1", "5.00", cat);
        new GenericDAO<Categorie, Long>(Categorie.class, factory).transaction(em -> {
            Categorie managed = em.find(Categorie.class, cat.getId());
            managed.removeProduit(managed.getProduits().get(0)); return null;
        });
        assertNull(produits.getById(p.getId()));
    }
    @Test public void validationsEtDoublons() {
        Categorie cat = new Categorie("Test"); categories.create(cat);
        Produit p = new Produit("Test", "NEG", new BigDecimal("-1"), cat);
        assertThrows(IllegalArgumentException.class, () -> produits.create(p));
        assertThrows(IllegalArgumentException.class, () -> produits.findPage(0, 10));
        assertThrows(IllegalArgumentException.class, () -> produits.search(null, BigDecimal.TEN, BigDecimal.ONE));
        Produit ok = produit("OK", "1.00", cat);
        Commande commande = new Commande(); commande.addProduit(ok, 1);
        assertThrows(IllegalArgumentException.class, () -> commande.addProduit(ok, 2));
        assertThrows(IllegalArgumentException.class, () -> new Commande().addProduit(ok, 0));
        assertThrows(RuntimeException.class, () -> produit("OK", "2.00", cat));
        assertEquals(1, produits.getAll().size());
    }
}

