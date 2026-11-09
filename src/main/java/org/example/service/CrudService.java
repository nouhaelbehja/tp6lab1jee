package org.example.service;
import org.example.dao.GenericDAO;
import org.example.entities.*;
import javax.persistence.EntityManagerFactory;
import java.io.Serializable;
import java.util.List;

public class CrudService<T, ID extends Serializable> {
    protected final GenericDAO<T, ID> dao;
    public CrudService(Class<T> type) { dao = new GenericDAO<>(type); }
    public CrudService(Class<T> type, EntityManagerFactory emf) { dao = new GenericDAO<>(type, emf); }
    public T getById(ID id) {
        if (id == null) throw new IllegalArgumentException("Identifiant obligatoire");
        return dao.find(id);
    }
    public List<T> getAll() { return dao.findAll(); }
    public void create(T entity) {
        validate(entity);
        if (entity instanceof Commande) {
            Commande commande = (Commande) entity;
            dao.transaction(em -> {
                for (CommandeProduit ligne : commande.getLignes()) {
                    // MapsId exige une association geree dans la transaction courante.
                    ligne.setProduit(em.getReference(Produit.class, ligne.getProduit().getId()));
                }
                if (commande.getClient() != null)
                    commande.setClient(em.getReference(Client.class, commande.getClient().getId()));
                em.persist(commande);
                return null;
            });
        } else {
            dao.create(entity);
        }
    }
    public T update(T entity) { validate(entity); return dao.update(entity); }
    public void delete(T entity) {
        if (entity == null) throw new IllegalArgumentException("Entité obligatoire");
        dao.delete(entity);
    }
    protected void validate(T entity) {
        if (entity == null) throw new IllegalArgumentException("Entité obligatoire");
        if (entity instanceof Categorie) requireText(((Categorie) entity).getNom(), 100, "Nom catégorie");
        if (entity instanceof Produit) {
            Produit p = (Produit) entity;
            requireText(p.getLibelle(), 150, "Libellé"); requireText(p.getReference(), 50, "Référence");
            if (p.getPrix() == null || p.getPrix().signum() < 0 || p.getPrix().scale() > 2)
                throw new IllegalArgumentException("Prix positif ou nul avec deux décimales maximum");
            if (p.getStock() < 0 || p.getCategorie() == null || p.getCategorie().getId() == null || p.getDateCreation() == null)
                throw new IllegalArgumentException("Stock, catégorie persistée ou date invalide");
        }
        if (entity instanceof User) {
            User u = (User) entity;
            requireText(u.getNom(), 100, "Nom"); requireText(u.getEmail(), 150, "Email");
            if (!u.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Email invalide");
        }
        if (entity instanceof Commande) {
            Commande c = (Commande) entity;
            requireText(c.getReference(), 50, "Référence commande");
            if (c.getDateCommande() == null || c.getStatut() == null || c.getLignes().isEmpty())
                throw new IllegalArgumentException("Date, statut et lignes obligatoires");
            for (CommandeProduit l : c.getLignes())
                if (l.getQuantite() <= 0 || l.getPrixUnitaire() == null || l.getPrixUnitaire().signum() < 0)
                    throw new IllegalArgumentException("Ligne invalide");
        }
    }
    private void requireText(String text, int max, String label) {
        if (text == null || text.trim().isEmpty() || text.length() > max)
            throw new IllegalArgumentException(label + " obligatoire, maximum " + max + " caractères");
    }
}

