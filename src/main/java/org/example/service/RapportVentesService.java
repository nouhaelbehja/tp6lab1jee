package org.example.service;
import org.example.dao.GenericDAO;
import org.example.entities.*;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class RapportVentesService {
    private final GenericDAO<Commande, Long> dao;
    public RapportVentesService() { dao = new GenericDAO<>(Commande.class); }
    public RapportVentesService(EntityManagerFactory emf) { dao = new GenericDAO<>(Commande.class, emf); }
    public List<VenteCategorie> moisPrecedent() { return pourMois(YearMonth.now().minusMonths(1)); }
    public List<VenteCategorie> pourMois(YearMonth mois) {
        Objects.requireNonNull(mois, "Mois obligatoire");
        return dao.read(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<VenteCategorie> query = cb.createQuery(VenteCategorie.class);
            Root<CommandeProduit> ligne = query.from(CommandeProduit.class);
            Join<CommandeProduit, Commande> commande = ligne.join("commande");
            Join<CommandeProduit, Produit> produit = ligne.join("produit");
            Join<Produit, Categorie> categorie = produit.join("categorie");
            Expression<BigDecimal> montant = cb.prod(ligne.<BigDecimal>get("prixUnitaire"), ligne.<Integer>get("quantite").as(BigDecimal.class));
            query.select(cb.construct(VenteCategorie.class, categorie.get("id"), categorie.get("nom"), cb.sum(montant)));
            query.where(cb.greaterThanOrEqualTo(commande.<LocalDate>get("dateCommande"), mois.atDay(1)),
                    cb.lessThan(commande.<LocalDate>get("dateCommande"), mois.plusMonths(1).atDay(1)),
                    commande.get("statut").in(StatutCommande.VALIDEE, StatutCommande.LIVREE));
            query.groupBy(categorie.get("id"), categorie.get("nom")).orderBy(cb.asc(categorie.get("nom")));
            List<VenteCategorie> ventes = em.createQuery(query).getResultList();
            Map<Long, VenteCategorie> parCategorie = new HashMap<>();
            for (VenteCategorie vente : ventes) parCategorie.put(vente.getCategorieId(), vente);
            List<VenteCategorie> result = new ArrayList<>();
            for (Categorie cat : em.createQuery("SELECT c FROM Categorie c ORDER BY c.nom", Categorie.class).getResultList())
                result.add(parCategorie.getOrDefault(cat.getId(), new VenteCategorie(cat.getId(), cat.getNom(), BigDecimal.ZERO)));
            return result;
        });
    }
}

