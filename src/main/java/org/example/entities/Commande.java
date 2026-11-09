package org.example.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "commandes")
public class Commande {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_commande", nullable = false)
    private LocalDate dateCommande = LocalDate.now();

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private StatutCommande statut = StatutCommande.BROUILLON;

    @Column(length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeProduit> lignes = new ArrayList<>();

    public Commande() {}
    public Long getId() { return id; }

    public LocalDate getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDate dateCommande) { this.dateCommande = dateCommande; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public List<CommandeProduit> getLignes() { return Collections.unmodifiableList(lignes); }
    public void addProduit(Produit produit, int quantite) {
        if (produit == null || produit.getId() == null) throw new IllegalArgumentException("Le produit doit être persisté");
        if (quantite <= 0) throw new IllegalArgumentException("Quantité strictement positive");
        for (CommandeProduit ligne : lignes)
            if (ligne.getProduit().getId().equals(produit.getId()))
                throw new IllegalArgumentException("Produit déjà présent dans la commande");
        lignes.add(new CommandeProduit(this, produit, quantite));
    }
    public void removeProduit(Produit produit) {
        lignes.removeIf(l -> l.getProduit().getId().equals(produit.getId()));
    }

}

