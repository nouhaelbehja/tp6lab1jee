package org.example.service;
import org.example.entities.Produit;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.*;

public class ProduitService extends CrudService<Produit, Long> {
    public ProduitService() { super(Produit.class); }
    public ProduitService(EntityManagerFactory emf) { super(Produit.class, emf); }
    @Override public Produit getById(Long id) {
        if (id == null) throw new IllegalArgumentException("Identifiant obligatoire");
        return dao.read(em -> em.createQuery("SELECT p FROM Produit p JOIN FETCH p.categorie WHERE p.id = :id", Produit.class)
                .setParameter("id", id).getResultStream().findFirst().orElse(null));
    }
    @Override public List<Produit> getAll() {
        return dao.read(em -> em.createNamedQuery("Produit.findAll", Produit.class).getResultList());
    }
    public List<Produit> findByCategorie(Long catId) {
        if (catId == null) throw new IllegalArgumentException("Catégorie obligatoire");
        return dao.read(em -> em.createNamedQuery("Produit.findByCategorie", Produit.class).setParameter("catId", catId).getResultList());
    }
    public List<Produit> searchByLibelle(String term) {
        if (term == null) throw new IllegalArgumentException("Terme obligatoire");
        return dao.read(em -> em.createNamedQuery("Produit.searchByLibelle", Produit.class).setParameter("term", term).getResultList());
    }
    public List<Produit> search(String term, BigDecimal minPrix, BigDecimal maxPrix) {
        if ((minPrix != null && minPrix.signum() < 0) || (maxPrix != null && maxPrix.signum() < 0)
                || (minPrix != null && maxPrix != null && minPrix.compareTo(maxPrix) > 0))
            throw new IllegalArgumentException("Intervalle de prix invalide");
        return dao.read(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Produit> query = cb.createQuery(Produit.class);
            Root<Produit> root = query.from(Produit.class);
            root.fetch("categorie");
            List<Predicate> filters = new ArrayList<>();
            if (term != null && !term.trim().isEmpty())
                filters.add(cb.like(cb.lower(root.get("libelle")), "%" + term.trim().toLowerCase(Locale.ROOT) + "%"));
            if (minPrix != null) filters.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("prix"), minPrix));
            if (maxPrix != null) filters.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("prix"), maxPrix));
            query.select(root).where(filters.toArray(new Predicate[0])).orderBy(cb.asc(root.get("id")));
            return em.createQuery(query).getResultList();
        });
    }
    public List<Produit> findPage(int pageNumber, int pageSize) {
        if (pageNumber < 1 || pageSize < 1 || pageSize > 100) throw new IllegalArgumentException("Page >= 1, taille entre 1 et 100");
        int offset = Math.multiplyExact(pageNumber - 1, pageSize);
        return dao.read(em -> em.createQuery("SELECT p FROM Produit p JOIN FETCH p.categorie ORDER BY p.id", Produit.class)
                .setFirstResult(offset).setMaxResults(pageSize).getResultList());
    }
}

